package org.example.project.abfall.model

import kotlinx.serialization.Serializable

data class Kommune(
    val regionCode: String,
    val displayName: String,
)

@Serializable
data class Ort(
    val id: Long,
    val name: String,
)

@Serializable
data class Strasse(
    val id: Long,
    val name: String,
)

@Serializable
data class Hausnummer(
    val id: Long,
    val nr: String? = null,
)

@Serializable
data class Fraktion(
    val id: Long,
    val name: String,
)

@Serializable
data class TerminBezirk(
    val id: Long? = null,
    val name: String? = null,
    val fraktionId: Long? = null,
)

@Serializable
data class Termin(
    val datum: String,
    val bezirk: TerminBezirk? = null,
    val fraktionId: Long? = null,
)

data class TerminAnzeige(
    val datum: String,
    val fraktionName: String,
)
