package com.carlitoswy.flashmeet.presentation.event

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class EditEventUiState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val firebaseStorage: FirebaseStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditEventUiState())
    val uiState: StateFlow<EditEventUiState> = _uiState.asStateFlow()

    val isLoading: StateFlow<Boolean> = _uiState.map { it.isLoading }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val errorMessage: StateFlow<String?> = _uiState.map { it.errorMessage }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    fun loadEventById(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val event = eventRepository.getEventById(eventId)
                _uiState.update { it.copy(event = event, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.localizedMessage ?: "Error al cargar el evento.",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        adOption: String,
        highlightedText: String,
        imageUri: Uri?,
        locationName: String,
        onSuccess: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentEvent = _uiState.value.event
                var imageUrl = currentEvent?.imageUrl

                // 🖼️ Subir nueva imagen si es distinta
                if (imageUri != null && imageUri.toString() != imageUrl) {
                    val fileName = "event_images/${UUID.randomUUID()}.jpg"
                    val ref = firebaseStorage.reference.child(fileName)
                    ref.putFile(imageUri).await()
                    imageUrl = ref.downloadUrl.await().toString()
                }

                val updatedEvent = currentEvent?.copy(
                    title = title,
                    description = description,
                    adOption = adOption,
                    highlightedText = highlightedText,
                    locationName = locationName,
                    imageUrl = imageUrl
                )

                if (updatedEvent != null) {
                    eventRepository.updateEvent(updatedEvent)
                    onSuccess()
                } else {
                    _uiState.update { it.copy(errorMessage = "Error: El evento es nulo.") }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Error al actualizar evento.")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
