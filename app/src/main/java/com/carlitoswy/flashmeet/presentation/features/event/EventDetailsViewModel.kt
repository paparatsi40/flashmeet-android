package com.carlitoswy.flashmeet.presentation.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.model.toEventCategory
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow // Added this import
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Data class para encapsular todo el estado de la UI
data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EventDetailViewModel @Inject constructor( // <-- Renombrado: EventDetailViewModel
    private val db: FirebaseFirestore
) : ViewModel() {

    // Único StateFlow para el estado de la UI
    private val _uiState = MutableStateFlow(EventDetailUiState(isLoading = true)) // Inicia cargando
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow() // Expone el UiState

    fun loadEventById(eventId: String) { // <-- Renombrada la función: loadEventById
        if (eventId.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "ID vacío", isLoading = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) // Actualiza el estado de carga
            try {
                val doc = db.collection("events").document(eventId).get().await()
                if (!doc.exists()) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Evento no encontrado", event = null, isLoading = false)
                } else {
                    val e = Event(
                        id = doc.getString("id") ?: eventId,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        adOption = doc.getString("adOption") ?: "NONE",
                        highlightedText = doc.getString("highlightedText") ?: "",
                        createdBy = doc.getString("ownerId") ?: (doc.getString("createdBy") ?: ""),
                        timestamp = doc.getLong("createdAt")?.toLong()
                            ?: doc.getLong("timestamp")?.toLong()
                            ?: System.currentTimeMillis(),
                        locationName = doc.getString("locationName") ?: "",
                        category = (doc.getString("category") ?: "").toEventCategory(),
                        imageUrl = doc.getString("imageUrl"),
                        country = doc.getString("country") ?: "",
                        city = doc.getString("city") ?: "",
                        postal = doc.getString("postal") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0
                    )
                    _uiState.value = _uiState.value.copy(event = e, isLoading = false, errorMessage = null)
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(errorMessage = t.message ?: "Error desconocido", event = null, isLoading = false)
            }
        }
    }
}
