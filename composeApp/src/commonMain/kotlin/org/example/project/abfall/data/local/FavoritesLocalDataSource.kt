package org.example.project.abfall.data.local

import com.russhwolf.settings.Settings

private const val SEPARATOR = "|"

class FavoritesLocalDataSource(private val settings: Settings = Settings()) {

    fun get(scope: String): Set<String> =
        settings.getStringOrNull(key(scope))
            ?.split(SEPARATOR)
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()

    fun toggle(scope: String, id: String): Set<String> {
        val current = get(scope).toMutableSet()
        if (!current.add(id)) current.remove(id)
        settings.putString(key(scope), current.joinToString(SEPARATOR))
        return current
    }

    private fun key(scope: String) = "fav.$scope"
}
