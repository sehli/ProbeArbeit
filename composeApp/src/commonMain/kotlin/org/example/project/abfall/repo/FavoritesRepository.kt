package org.example.project.abfall.repo

import org.example.project.abfall.data.local.FavoritesLocalDataSource

class FavoritesRepository(
    private val localDataSource: FavoritesLocalDataSource,
) {
    fun favorites(scope: String): Set<String> = localDataSource.get(scope)

    fun toggle(scope: String, id: String): Set<String> = localDataSource.toggle(scope, id)
}
