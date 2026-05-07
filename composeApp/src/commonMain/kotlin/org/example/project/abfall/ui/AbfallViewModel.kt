package org.example.project.abfall.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.abfall.data.FavoritesStore
import org.example.project.abfall.model.Hausnummer
import org.example.project.abfall.model.Kommune
import org.example.project.abfall.model.Ort
import org.example.project.abfall.model.Strasse
import org.example.project.abfall.repo.AbfallRepository

class AbfallViewModel(
    private val repository: AbfallRepository,
    private val favoritesStore: FavoritesStore,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AbfallUiState(favorites = favoritesStore.get(scopeFor(Step.KommuneAuswahl)))
    )
    val state: StateFlow<AbfallUiState> = _state.asStateFlow()

    private fun setStep(step: Step) {
        _state.update { it.copy(step = step, favorites = favoritesStore.get(scopeFor(step))) }
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
        val updated = favoritesStore.toggle(scope, id)
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
