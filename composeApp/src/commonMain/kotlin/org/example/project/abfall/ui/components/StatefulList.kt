package org.example.project.abfall.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.example.project.abfall.ui.ListLoadState

@Composable
fun <T> StatefulList(
    state: ListLoadState<T>,
    label: (T) -> String,
    onClick: (T) -> Unit,
    emptyMessage: String = "Keine Einträge",
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "Suchen…",
    favoritesEnabled: Boolean = false,
    keyOf: (T) -> String = { "" },
    favorites: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable(state.items.size) { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        if (searchEnabled && state.items.isNotEmpty()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(searchPlaceholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> Text(
                    "Fehler: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
                state.items.isEmpty() -> Text(emptyMessage)
                else -> {
                    val q = query.trim()
                    val matched = if (q.isEmpty()) state.items
                    else state.items.filter { label(it).contains(q, ignoreCase = true) }
                    val sorted = if (favoritesEnabled && favorites.isNotEmpty()) {
                        matched.sortedByDescending { favorites.contains(keyOf(it)) }
                    } else matched

                    if (sorted.isEmpty()) {
                        Text("Keine Treffer")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                        ) {
                            items(sorted) { item ->
                                ListRow(
                                    label = label(item),
                                    isFavorite = favoritesEnabled && favorites.contains(keyOf(item)),
                                    showFavorite = favoritesEnabled,
                                    onClick = { onClick(item) },
                                    onToggleFavorite = { onToggleFavorite(keyOf(item)) },
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListRow(
    label: String,
    isFavorite: Boolean,
    showFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
        )
        if (showFavorite) {
            IconButton(onClick = onToggleFavorite) {
                Text(
                    text = if (isFavorite) "★" else "☆",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
