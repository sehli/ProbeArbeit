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
import org.example.project.abfall.repo.AbfallRepository

class AbfallViewModel(
    private val repository: AbfallRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AbfallUiState())
    val state: StateFlow<AbfallUiState> = _state.asStateFlow()

    fun selectKommune(kommune: Kommune) {
        _state.update { it.copy(step = Step.OrtAuswahl(kommune), orte = ListLoadState(isLoading = true)) }
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
        _state.update {
            it.copy(step = Step.StrasseAuswahl(k, ort), strassen = ListLoadState(isLoading = true))
        }
        viewModelScope.launch {
            runCatching { repository.loadStrassen(k.regionCode, ort.id) }
                .onSuccess { items -> _state.update { it.copy(strassen = ListLoadState(items = items)) } }
                .onFailure { e -> _state.update { it.copy(strassen = ListLoadState(error = e.message ?: "Fehler")) } }
        }
    }

    fun selectStrasse(strasse: Strasse) {
        val current = _state.value.step
        if (current !is Step.StrasseAuswahl) return
        _state.update {
            it.copy(
                step = Step.HausnummerAuswahl(current.kommune, current.ort, strasse),
                hausnummern = ListLoadState(isLoading = true),
            )
        }
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
        _state.update {
            it.copy(
                step = Step.TermineAnzeige(kommune, ort, strasse, pseudo),
                termine = ListLoadState(isLoading = true),
            )
        }
        viewModelScope.launch {
            runCatching { repository.loadTermineForStrasse(kommune.regionCode, strasse.id) }
                .onSuccess { items -> _state.update { it.copy(termine = ListLoadState(items = items)) } }
                .onFailure { e -> _state.update { it.copy(termine = ListLoadState(error = e.message ?: "Fehler")) } }
        }
    }

    fun selectHausnummer(hausnummer: Hausnummer) {
        val current = _state.value.step
        if (current !is Step.HausnummerAuswahl) return
        _state.update {
            it.copy(
                step = Step.TermineAnzeige(current.kommune, current.ort, current.strasse, hausnummer),
                termine = ListLoadState(isLoading = true),
            )
        }
        viewModelScope.launch {
            runCatching { repository.loadTermine(current.kommune.regionCode, hausnummer.id) }
                .onSuccess { items -> _state.update { it.copy(termine = ListLoadState(items = items)) } }
                .onFailure { e -> _state.update { it.copy(termine = ListLoadState(error = e.message ?: "Fehler")) } }
        }
    }

    fun back() {
        _state.update { current ->
            val newStep = when (val s = current.step) {
                Step.KommuneAuswahl -> Step.KommuneAuswahl
                is Step.OrtAuswahl -> Step.KommuneAuswahl
                is Step.StrasseAuswahl -> Step.OrtAuswahl(s.kommune)
                is Step.HausnummerAuswahl -> Step.StrasseAuswahl(s.kommune, s.ort)
                is Step.TermineAnzeige -> Step.HausnummerAuswahl(s.kommune, s.ort, s.strasse)
            }
            current.copy(step = newStep)
        }
    }
}
