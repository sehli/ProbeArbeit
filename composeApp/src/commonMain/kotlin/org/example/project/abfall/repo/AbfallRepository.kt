package org.example.project.abfall.repo

import org.example.project.abfall.api.AbfallApi
import org.example.project.abfall.model.Hausnummer
import org.example.project.abfall.model.Ort
import org.example.project.abfall.model.Strasse
import org.example.project.abfall.model.TerminAnzeige

class AbfallRepository(private val api: AbfallApi) {

    suspend fun loadOrte(region: String): List<Ort> =
        api.getOrte(region).sortedBy { it.name.lowercase() }

    suspend fun loadStrassen(region: String, ortId: Long): List<Strasse> =
        api.getStrassen(region, ortId).sortedBy { it.name.lowercase() }

    suspend fun loadHausnummern(region: String, strasseId: Long): List<Hausnummer> =
        api.getHausnummern(region, strasseId).sortedBy { it.nr ?: "" }

    suspend fun loadTermine(region: String, hausnummerId: Long): List<TerminAnzeige> {
        val fraktionen = api.getFraktionen(region, hausnummerId).associateBy { it.id }
        val termine = api.getTermine(region, hausnummerId)
        return termine
            .map { t ->
                val fraktionId = t.fraktionId ?: t.bezirk?.fraktionId
                val name = fraktionen[fraktionId]?.name ?: t.bezirk?.name ?: "Termin"
                TerminAnzeige(datum = t.datum, fraktionName = name)
            }
            .sortedBy { it.datum }
    }

    suspend fun loadTermineForStrasse(region: String, strasseId: Long): List<TerminAnzeige> =
        api.getTermineForStrasse(region, strasseId)
            .map { t ->
                val name = t.bezirk?.name ?: "Termin"
                TerminAnzeige(datum = t.datum, fraktionName = name)
            }
            .sortedBy { it.datum }
}
