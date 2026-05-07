package org.example.project.abfall.di

import org.example.project.abfall.api.AbfallApi
import org.example.project.abfall.api.HttpClientFactory
import org.example.project.abfall.repo.AbfallRepository

object ServiceLocator {
    private val httpClient by lazy { HttpClientFactory.create() }
    private val api by lazy { AbfallApi(httpClient) }
    val repository: AbfallRepository by lazy { AbfallRepository(api) }
}
