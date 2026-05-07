package org.example.project.abfall.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import org.example.project.abfall.model.Fraktion
import org.example.project.abfall.model.Hausnummer
import org.example.project.abfall.model.Ort
import org.example.project.abfall.model.Strasse
import org.example.project.abfall.model.Termin

class AbfallApi(private val client: HttpClient) {

    private fun baseUrl(region: String): String =
        "https://$region-abfallapp.regioit.de/abfall-app-$region/rest"

    private suspend inline fun <reified T> getOrEmpty(url: String, default: () -> T): T = try {
        client.get(url).body()
    } catch (e: ClientRequestException) {
        if (e.response.status == HttpStatusCode.NotFound) default() else throw e
    }

    suspend fun getOrte(region: String): List<Ort> =
        client.get("${baseUrl(region)}/orte").body()

    suspend fun getStrassen(region: String, ortId: Long): List<Strasse> =
        client.get("${baseUrl(region)}/orte/$ortId/strassen").body()

    suspend fun getHausnummern(region: String, strasseId: Long): List<Hausnummer> =
        getOrEmpty("${baseUrl(region)}/strassen/$strasseId/hausnummern") { emptyList() }

    suspend fun getFraktionen(region: String, hausnummerId: Long): List<Fraktion> =
        getOrEmpty("${baseUrl(region)}/hausnummern/$hausnummerId/fraktionen") { emptyList() }

    suspend fun getTermine(region: String, hausnummerId: Long): List<Termin> =
        getOrEmpty("${baseUrl(region)}/hausnummern/$hausnummerId/termine") { emptyList() }

    suspend fun getTermineForStrasse(region: String, strasseId: Long): List<Termin> =
        getOrEmpty("${baseUrl(region)}/strassen/$strasseId/termine") { emptyList() }
}
