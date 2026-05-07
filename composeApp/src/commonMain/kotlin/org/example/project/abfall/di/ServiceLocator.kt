package org.example.project.abfall.di

import org.example.project.abfall.api.AbfallApi
import org.example.project.abfall.api.HttpClientFactory
import org.example.project.abfall.data.local.FavoritesLocalDataSource
import org.example.project.abfall.data.local.WatchedAddressLocalDataSource
import org.example.project.abfall.notification.AbfallNotificationScheduler
import org.example.project.abfall.repo.AbfallRepository
import org.example.project.abfall.repo.FavoritesRepository
import org.example.project.abfall.repo.WatchedAddressRepository

object ServiceLocator {
    private val httpClient by lazy { HttpClientFactory.create() }
    private val api by lazy { AbfallApi(httpClient) }
    private val favoritesLocal by lazy { FavoritesLocalDataSource() }
    private val watchedLocal by lazy { WatchedAddressLocalDataSource() }

    val repository: AbfallRepository by lazy { AbfallRepository(api) }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepository(favoritesLocal) }
    val watchedAddressRepository: WatchedAddressRepository by lazy {
        WatchedAddressRepository(watchedLocal)
    }
    val notificationScheduler: AbfallNotificationScheduler by lazy { AbfallNotificationScheduler() }
}
