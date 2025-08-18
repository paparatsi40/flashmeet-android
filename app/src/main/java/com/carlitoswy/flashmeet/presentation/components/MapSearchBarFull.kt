package com.carlitoswy.flashmeet.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore

// ✅ Modelo genérico para sugerencias
sealed class SearchSuggestion {
    data class GooglePlace(val prediction: AutocompletePrediction, val latLng: LatLng?) : SearchSuggestion()
    data class EventSuggestion(val event: Event) : SearchSuggestion()
}

@OptIn(ExperimentalMaterial3Api::class) // Keep this annotation as TextFieldDefaults.colors is experimental
@Composable
fun MapSearchBar(
    hint: String = "Buscar lugares o eventos...",
    onPlaceSelected: (LatLng?, Event?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val placesClient: PlacesClient = remember { Places.createClient(context) }

    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<SearchSuggestion>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 🔍 Input de búsqueda
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
                } else suggestions = emptyList()
            },
            placeholder = { Text(hint) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            // HERE IS THE CORRECTED FIX!
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 📌 Lista de sugerencias combinadas
        suggestions.forEach { suggestion ->
            when (suggestion) {
                is SearchSuggestion.GooglePlace -> {
                    Text(
                        text = "📍 ${suggestion.prediction.getFullText(null)}",
                        style = MaterialTheme.typography.bodyMedium,
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
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF00796B)),
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

// ✅ 🔥 Helper: predicciones de Google Places
fun fetchPredictions(
    placesClient: PlacesClient,
    query: String,
    onResult: (List<SearchSuggestion.GooglePlace>) -> Unit
) {
    val request = FindAutocompletePredictionsRequest.builder().setQuery(query).build()
    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            val list = response.autocompletePredictions.map {
                SearchSuggestion.GooglePlace(it, null) // LatLng se obtiene después si se selecciona
            }
            onResult(list)
        }
        .addOnFailureListener { e ->
            Log.e("MapSearchBar", "❌ Error Places API: ${e.message}")
            onResult(emptyList())
        }
}

// ✅ 🔥 Helper: obtener LatLng de Google Place cuando se selecciona
fun fetchPlaceLatLng(
    placesClient: PlacesClient,
    prediction: AutocompletePrediction,
    callback: (LatLng?) -> Unit
) {
    val request = FetchPlaceRequest.builder(prediction.placeId, listOf(Place.Field.LAT_LNG)).build()
    placesClient.fetchPlace(request)
        .addOnSuccessListener { callback(it.place.latLng) }
        .addOnFailureListener { callback(null) }
}

// ✅ 🔥 Helper: sugerencias desde Firestore (eventos)
fun fetchEventSuggestions(
    query: String,
    onResult: (List<SearchSuggestion.EventSuggestion>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("events")
        .whereGreaterThanOrEqualTo("title", query)
        .whereLessThanOrEqualTo("title", query + "\uf8ff")
        .limit(5)
        .get()
        .addOnSuccessListener { snapshot ->
            val results = snapshot.documents.mapNotNull { it.toObject(com.carlitoswy.flashmeet.domain.model.Event::class.java) }
                .map { SearchSuggestion.EventSuggestion(it) }
            onResult(results)
        }
        .addOnFailureListener { e ->
            Log.e("MapSearchBar", "❌ Error Firestore: ${e.message}")
            onResult(emptyList())
        }
}
