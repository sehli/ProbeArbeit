package org.example.project.abfall.ui

import org.example.project.abfall.model.Hausnummer
import org.example.project.abfall.model.Kommune
import org.example.project.abfall.model.Ort
import org.example.project.abfall.model.Strasse
import org.example.project.abfall.model.TerminAnzeige

sealed interface Step {
    data object KommuneAuswahl : Step
    data class OrtAuswahl(val kommune: Kommune) : Step
    data class StrasseAuswahl(val kommune: Kommune, val ort: Ort) : Step
    data class HausnummerAuswahl(val kommune: Kommune, val ort: Ort, val strasse: Strasse) : Step
    data class TermineAnzeige(
        val kommune: Kommune,
        val ort: Ort,
        val strasse: Strasse,
        val hausnummer: Hausnummer,
    ) : Step
}

data class ListLoadState<T>(
    val isLoading: Boolean = false,
    val items: List<T> = emptyList(),
    val error: String? = null,
)

data class AbfallUiState(
    val step: Step = Step.KommuneAuswahl,
    val orte: ListLoadState<Ort> = ListLoadState(),
    val strassen: ListLoadState<Strasse> = ListLoadState(),
    val hausnummern: ListLoadState<Hausnummer> = ListLoadState(),
    val termine: ListLoadState<TerminAnzeige> = ListLoadState(),
    val favorites: Set<String> = emptySet(),
)
