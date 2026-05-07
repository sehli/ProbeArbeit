package org.example.project.abfall.di

import org.example.project.abfall.api.AbfallApi
import org.example.project.abfall.api.HttpClientFactory
import org.example.project.abfall.data.local.FavoritesLocalDataSource
import org.example.project.abfall.repo.AbfallRepository
import org.example.project.abfall.repo.FavoritesRepository

object ServiceLocator {
    private val httpClient by lazy { HttpClientFactory.create() }
    private val api by lazy { AbfallApi(httpClient) }
    private val favoritesLocal by lazy { FavoritesLocalDataSource() }

    val repository: AbfallRepository by lazy { AbfallRepository(api) }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepository(favoritesLocal) }
}
