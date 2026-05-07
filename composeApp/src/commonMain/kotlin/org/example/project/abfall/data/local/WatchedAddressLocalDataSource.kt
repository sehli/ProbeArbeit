package org.example.project.abfall.data.local

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.abfall.model.WatchedAddress

class WatchedAddressLocalDataSource(private val settings: Settings = Settings()) {

    private val json = Json { ignoreUnknownKeys = true }
    private val storageKey = "watched.addresses"

    fun all(): List<WatchedAddress> {
        val raw = settings.getStringOrNull(storageKey) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<WatchedAddress>>(raw)
        }.getOrDefault(emptyList())
    }

    fun add(item: WatchedAddress): List<WatchedAddress> {
        val current = all().toMutableList()
        if (current.none { it.storageKey == item.storageKey }) current.add(item)
        save(current)
        return current
    }

    fun remove(storageKey: String): List<WatchedAddress> {
        val updated = all().filterNot { it.storageKey == storageKey }
        save(updated)
        return updated
    }

    private fun save(items: List<WatchedAddress>) {
        settings.putString(this.storageKey, json.encodeToString(items))
    }
}
