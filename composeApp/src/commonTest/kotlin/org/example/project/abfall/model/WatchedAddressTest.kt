package org.example.project.abfall.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WatchedAddressTest {

    private fun hausnummerAddress(
        region: String = "aachen",
        regionDisplay: String = "Aachen",
        ortName: String = "Aachen",
        strasseName: String = "Hauptstraße",
        hausnummerId: Long? = 42L,
        hausnummerNr: String? = "12a",
        strasseId: Long? = 7L,
    ) = WatchedAddress(
        region = region,
        regionDisplay = regionDisplay,
        ortName = ortName,
        strasseName = strasseName,
        hausnummerId = hausnummerId,
        hausnummerNr = hausnummerNr,
        strasseId = strasseId,
    )

    private fun strasseAddress(
        region: String = "aachen",
        regionDisplay: String = "Aachen",
        ortName: String = "Aachen",
        strasseName: String = "Hauptstraße",
        strasseId: Long? = 7L,
    ) = WatchedAddress(
        region = region,
        regionDisplay = regionDisplay,
        ortName = ortName,
        strasseName = strasseName,
        hausnummerId = null,
        hausnummerNr = null,
        strasseId = strasseId,
    )

    @Test
    fun isHausnummer_isTrue_whenHausnummerIdIsSet() {
        assertTrue(hausnummerAddress(hausnummerId = 1L).isHausnummer)
    }

    @Test
    fun isHausnummer_isFalse_whenHausnummerIdIsNull() {
        assertFalse(strasseAddress().isHausnummer)
    }

    @Test
    fun storageKey_usesHausnummerId_whenAvailable() {
        val address = hausnummerAddress(region = "aachen", hausnummerId = 99L, strasseId = 7L)
        assertEquals("aachen|h:99", address.storageKey)
    }

    @Test
    fun storageKey_usesStrasseId_whenHausnummerIsMissing() {
        val address = strasseAddress(region = "solingen", strasseId = 5L)
        assertEquals("solingen|s:5", address.storageKey)
    }

    @Test
    fun storageKey_isUnique_perRegionAndId() {
        val a = hausnummerAddress(region = "aachen", hausnummerId = 1L)
        val b = hausnummerAddress(region = "solingen", hausnummerId = 1L)
        assertNotEquals(a.storageKey, b.storageKey)
    }

    @Test
    fun displayName_includesHausnummer_whenPresent() {
        val address = hausnummerAddress(
            strasseName = "Hauptstraße",
            hausnummerNr = "12a",
            ortName = "Aachen",
        )
        assertEquals("Hauptstraße 12a, Aachen", address.displayName)
    }

    @Test
    fun displayName_omitsHausnummer_whenNull() {
        val address = strasseAddress(strasseName = "Hauptstraße", ortName = "Aachen")
        assertEquals("Hauptstraße, Aachen", address.displayName)
    }

    @Test
    fun displayName_omitsHausnummer_whenBlank() {
        val address = hausnummerAddress(
            strasseName = "Hauptstraße",
            hausnummerNr = "   ",
            ortName = "Aachen",
        )
        assertEquals("Hauptstraße, Aachen", address.displayName)
    }

    @Test
    fun displayName_omitsHausnummer_whenPlaceholderDash() {
        val address = hausnummerAddress(
            strasseName = "Hauptstraße",
            hausnummerNr = "—",
            ortName = "Aachen",
        )
        assertEquals("Hauptstraße, Aachen", address.displayName)
    }
}
