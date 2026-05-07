package org.example.project.abfall.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.example.project.abfall.model.Fraktion
import org.example.project.abfall.model.Hausnummer
import org.example.project.abfall.model.Ort
import org.example.project.abfall.model.Strasse
import org.example.project.abfall.model.Termin

class AbfallApi(private val client: HttpClient) {

    private fun baseUrl(region: String): String =
        "https://$region-abfallapp.regioit.de/abfall-app-$region/rest"

    suspend fun getOrte(region: String): List<Ort> =
        client.get("${baseUrl(region)}/orte").body()

    suspend fun getStrassen(region: String, ortId: Long): List<Strasse> =
        client.get("${baseUrl(region)}/orte/$ortId/strassen").body()

    suspend fun getHausnummern(region: String, strasseId: Long): List<Hausnummer> =
        client.get("${baseUrl(region)}/strassen/$strasseId/hausnummern").body()

    suspend fun getFraktionen(region: String, hausnummerId: Long): List<Fraktion> =
        client.get("${baseUrl(region)}/hausnummern/$hausnummerId/fraktionen").body()

    suspend fun getTermine(region: String, hausnummerId: Long): List<Termin> =
        client.get("${baseUrl(region)}/hausnummern/$hausnummerId/termine").body()

    suspend fun getTermineForStrasse(region: String, strasseId: Long): List<Termin> =
        client.get("${baseUrl(region)}/strassen/$strasseId/termine").body()
}
