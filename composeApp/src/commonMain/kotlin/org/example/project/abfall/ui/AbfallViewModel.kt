package org.example.project.abfall.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.abfall.model.Hausnummer
import org.example.project.abfall.model.Kommune
import org.example.project.abfall.model.Ort
import org.example.project.abfall.model.Strasse
import org.example.project.abfall.model.WatchedAddress
import org.example.project.abfall.notification.AbfallNotificationScheduler
import org.example.project.abfall.repo.AbfallRepository
import org.example.project.abfall.repo.FavoritesRepository
import org.example.project.abfall.repo.WatchedAddressRepository

class AbfallViewModel(
    private val repository: AbfallRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchedRepository: WatchedAddressRepository,
    private val notificationScheduler: AbfallNotificationScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AbfallUiState(
            favorites = favoritesRepository.favorites(scopeFor(Step.KommuneAuswahl)),
            isWatched = false,
        )
    )
    val state: StateFlow<AbfallUiState> = _state.asStateFlow()

    private fun setStep(step: Step) {
        _state.update {
            it.copy(
                step = step,
                favorites = favoritesRepository.favorites(scopeFor(step)),
                isWatched = isStepWatched(step),
            )
        }
    }

    private fun isStepWatched(step: Step): Boolean {
        val key = watchedKeyFor(step) ?: return false
        return watchedRepository.isWatched(key)
    }

    private fun watchedKeyFor(step: Step): String? = when (step) {
        is Step.TermineAnzeige -> watchedAddressFor(step).storageKey
        else -> null
    }

    private fun watchedAddressFor(step: Step.TermineAnzeige): WatchedAddress {
        val isPseudo = step.hausnummer.nr == "—"
        return WatchedAddress(
            region = step.kommune.regionCode,
            regionDisplay = step.kommune.displayName,
            ortName = step.ort.name,
            strasseName = step.strasse.name,
            hausnummerId = if (isPseudo) null else step.hausnummer.id,
            hausnummerNr = if (isPseudo) null else step.hausnummer.nr,
            strasseId = if (isPseudo) step.strasse.id else null,
        )
    }

    fun toggleWatch() {
        val step = _state.value.step
        if (step !is Step.TermineAnzeige) return
        val address = watchedAddressFor(step)
        val updated = if (watchedRepository.isWatched(address.storageKey)) {
            watchedRepository.remove(address.storageKey)
        } else {
            watchedRepository.add(address)
        }
        _state.update { it.copy(isWatched = updated.any { w -> w.storageKey == address.storageKey }) }
        if (updated.isEmpty()) notificationScheduler.cancel() else notificationScheduler.scheduleDaily()
    }

    fun selectKommune(kommune: Kommune) {
        setStep(Step.OrtAuswahl(kommune))
        _state.update { it.copy(orte = ListLoadState(isLoading = true)) }
        viewModelScope.launch {
            runCatching { repository.loadOrte(kommune.regionCode) }
                .onSuccess { items -> _state.update { it.copy(orte = ListLoadState(items = items)) } }
                .onFailure { e -> _state.update { it.copy(orte = ListLoadState(error = e.message ?: "Fehler")) } }
        }
    }

    fun selectOrt(ort: Ort) {
        val current = _state.value.step
        if (current !is Step.OrtAuswahl) return
        val k = current.kommune
        setStep(Step.StrasseAuswahl(k, ort))
        _state.update { it.copy(strassen = ListLoadState(isLoading = true)) }
        viewModelScope.launch {
            runCatching { repository.loadStrassen(k.regionCode, ort.id) }
                .onSuccess { items -> _state.update { it.copy(strassen = ListLoadState(items = items)) } }
                .onFailure { e -> _state.update { it.copy(strassen = ListLoadState(error = e.message ?: "Fehler")) } }
        }
    }

    fun selectStrasse(strasse: Strasse) {
        val current = _state.value.step
        if (current !is Step.StrasseAuswahl) return
        setStep(Step.HausnummerAuswahl(current.kommune, current.ort, strasse))
        _state.update { it.copy(hausnummern = ListLoadState(isLoading = true)) }
        viewModelScope.launch {
            runCatching { repository.loadHausnummern(current.kommune.regionCode, strasse.id) }
                .onSuccess { items ->
                    if (items.isEmpty()) {
                        loadTermineForStrasseAsHausnummer(current.kommune, current.ort, strasse)
                    } else {
                        _state.update { it.copy(hausnummern = ListLoadState(items = items)) }
                    }
                }
                .onFailure { e -> _state.update { it.copy(hausnummern = ListLoadState(error = e.message ?: "Fehler")) } }
        }
    }

    private fun loadTermineForStrasseAsHausnummer(kommune: Kommune, ort: Ort, strasse: Strasse) {
        val pseudo = Hausnummer(id = strasse.id, nr = "—")
        setStep(Step.TermineAnzeige(kommune, ort, strasse, pseudo))
        _state.update { it.copy(termine = ListLoadState(isLoading = true)) }
        viewModelScope.launch {
            runCatching { repository.loadTermineForStrasse(kommune.regionCode, strasse.id) }
                .onSuccess { items -> _state.update { it.copy(termine = ListLoadState(items = items)) } }
                .onFailure { e -> _state.update { it.copy(termine = ListLoadState(error = e.message ?: "Fehler")) } }
        }
    }

    fun selectHausnummer(hausnummer: Hausnummer) {
        val current = _state.value.step
        if (current !is Step.HausnummerAuswahl) return
        setStep(Step.TermineAnzeige(current.kommune, current.ort, current.strasse, hausnummer))
        _state.update { it.copy(termine = ListLoadState(isLoading = true)) }
        viewModelScope.launch {
            runCatching { repository.loadTermine(current.kommune.regionCode, hausnummer.id) }
                .onSuccess { items -> _state.update { it.copy(termine = ListLoadState(items = items)) } }
                .onFailure { e -> _state.update { it.copy(termine = ListLoadState(error = e.message ?: "Fehler")) } }
        }
    }

    fun toggleFavorite(id: String) {
        val scope = scopeFor(_state.value.step)
        val updated = favoritesRepository.toggle(scope, id)
        _state.update { it.copy(favorites = updated) }
    }

    fun back() {
        val newStep = when (val s = _state.value.step) {
            Step.KommuneAuswahl -> Step.KommuneAuswahl
            is Step.OrtAuswahl -> Step.KommuneAuswahl
            is Step.StrasseAuswahl -> Step.OrtAuswahl(s.kommune)
            is Step.HausnummerAuswahl -> Step.StrasseAuswahl(s.kommune, s.ort)
            is Step.TermineAnzeige -> Step.HausnummerAuswahl(s.kommune, s.ort, s.strasse)
        }
        setStep(newStep)
    }

    companion object {
        fun scopeFor(step: Step): String = when (step) {
            Step.KommuneAuswahl -> "kommune"
            is Step.OrtAuswahl -> "ort.${step.kommune.regionCode}"
            is Step.StrasseAuswahl -> "strasse.${step.kommune.regionCode}.${step.ort.id}"
            is Step.HausnummerAuswahl ->
                "hausnummer.${step.kommune.regionCode}.${step.ort.id}.${step.strasse.id}"
            is Step.TermineAnzeige -> "termine.disabled"
        }
    }
}
