package org.example.project.abfall.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.abfall.di.ServiceLocator
import org.example.project.abfall.model.Kommunen
import org.example.project.abfall.ui.components.StatefulList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbfallApp() {
    MaterialTheme {
        val viewModel: AbfallViewModel = viewModel {
            AbfallViewModel(ServiceLocator.repository, ServiceLocator.favoritesRepository)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()

        val (title, canGoBack) = when (val step = state.step) {
            Step.KommuneAuswahl -> "Kommune wählen" to false
            is Step.OrtAuswahl -> "${step.kommune.displayName} – Ort" to true
            is Step.StrasseAuswahl -> "${step.ort.name} – Straße" to true
            is Step.HausnummerAuswahl -> "${step.strasse.name} – Hausnummer" to true
            is Step.TermineAnzeige -> "Termine ${step.strasse.name}${step.hausnummer.nr?.let { " $it" } ?: ""}" to true
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, maxLines = 1) },
                    navigationIcon = {
                        if (canGoBack) {
                            IconButton(onClick = { viewModel.back() }) {
                                Text("‹", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (val step = state.step) {
                    Step.KommuneAuswahl -> {
                        val kommunen = remember { Kommunen.all }
                        StatefulList(
                            state = ListLoadState(items = kommunen),
                            label = { it.displayName },
                            keyOf = { it.regionCode },
                            favoritesEnabled = true,
                            favorites = state.favorites,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onClick = { viewModel.selectKommune(it) },
                        )
                    }
                    is Step.OrtAuswahl -> StatefulList(
                        state = state.orte,
                        label = { it.name },
                        keyOf = { it.id.toString() },
                        favoritesEnabled = true,
                        favorites = state.favorites,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onClick = { viewModel.selectOrt(it) },
                    )
                    is Step.StrasseAuswahl -> StatefulList(
                        state = state.strassen,
                        label = { it.name },
                        keyOf = { it.id.toString() },
                        favoritesEnabled = true,
                        favorites = state.favorites,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onClick = { viewModel.selectStrasse(it) },
                    )
                    is Step.HausnummerAuswahl -> StatefulList(
                        state = state.hausnummern,
                        label = { it.nr ?: "—" },
                        keyOf = { it.id.toString() },
                        favoritesEnabled = true,
                        favorites = state.favorites,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onClick = { viewModel.selectHausnummer(it) },
                    )
                    is Step.TermineAnzeige -> StatefulList(
                        state = state.termine,
                        label = { "${it.datum}  ·  ${it.fraktionName}" },
                        onClick = { },
                        emptyMessage = "Keine Termine gefunden",
                    )
                }
            }
        }
    }
}
