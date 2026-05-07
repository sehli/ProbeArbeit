package org.example.project.abfall.model

import kotlinx.serialization.Serializable

@Serializable
data class WatchedAddress(
    val region: String,
    val regionDisplay: String,
    val ortName: String,
    val strasseName: String,
    val hausnummerId: Long? = null,
    val hausnummerNr: String? = null,
    val strasseId: Long? = null,
) {
    val isHausnummer: Boolean get() = hausnummerId != null

    val storageKey: String
        get() = if (isHausnummer) "$region|h:$hausnummerId" else "$region|s:$strasseId"

    val displayName: String
        get() = buildString {
            append(strasseName)
            hausnummerNr?.takeIf { it.isNotBlank() && it != "—" }?.let { append(' ').append(it) }
            append(", ").append(ortName)
        }
}
