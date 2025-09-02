package com.carlitoswy.flashmeet.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlitoswy.flashmeet.domain.model.Event
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places

@Composable
fun MapSearchBarCompact(
    hint: String = "Buscar...",
    onPlaceSelected: (LatLng?, Event?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<SearchSuggestion>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                if (it.length > 2) {
                    fetchPredictions(placesClient, it) { places ->
                        fetchEventSuggestions(it) { events ->
                            suggestions = places + events
                        }
                    }
                } else {
                    suggestions = emptyList()
                }
            },
            placeholder = { Text(hint) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        suggestions = emptyList()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .background(Color.White, RoundedCornerShape(20.dp)),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        // 📌 Sugerencias visibles si hay texto
        AnimatedVisibility(visible = suggestions.isNotEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp)
            ) {
                suggestions.forEach { suggestion ->
                    when (suggestion) {
                        is SearchSuggestion.GooglePlace -> {
                            Text(
                                text = "📍 ${suggestion.prediction.getFullText(null)}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPlaceSelected(suggestion.latLng, null)
                                        suggestions = emptyList()
                                        query = suggestion.prediction.getFullText(null).toString()
                                    }
                                    .padding(8.dp)
                            )
                        }

                        is SearchSuggestion.EventSuggestion -> {
                            Text(
                                text = "🎫 ${suggestion.event.title} - ${suggestion.event.city}",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPlaceSelected(
                                            LatLng(suggestion.event.latitude, suggestion.event.longitude),
                                            suggestion.event
                                        )
                                        suggestions = emptyList()
                                        query = suggestion.event.title
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                    Divider()
                }
            }
        }
    }
}
