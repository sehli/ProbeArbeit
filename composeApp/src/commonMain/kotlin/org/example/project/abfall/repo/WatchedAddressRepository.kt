package org.example.project.abfall.repo

import org.example.project.abfall.data.local.WatchedAddressLocalDataSource
import org.example.project.abfall.model.WatchedAddress

class WatchedAddressRepository(
    private val localDataSource: WatchedAddressLocalDataSource,
) {
    fun all(): List<WatchedAddress> = localDataSource.all()

    fun isWatched(storageKey: String): Boolean =
        localDataSource.all().any { it.storageKey == storageKey }

    fun add(item: WatchedAddress): List<WatchedAddress> = localDataSource.add(item)

    fun remove(storageKey: String): List<WatchedAddress> = localDataSource.remove(storageKey)
}
