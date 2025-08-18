package com.carlitoswy.flashmeet.presentation.event

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.AdOption
import com.carlitoswy.flashmeet.domain.model.Event
import com.google.firebase.auth.FirebaseAuth // ¡Importamos FirebaseAuth!
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // ¡Importamos el await de Kotlin Coroutines para Tasks!
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth // ¡Inyectamos FirebaseAuth aquí!
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Estimates the cost of an ad option.
     * @param adOption The selected advertisement option.
     * @return A string representing the estimated cost.
     */
    fun estimateAdCost(adOption: AdOption): String = when (adOption) {
        AdOption.NONE -> "Gratis"
        AdOption.HIGHLIGHTED -> "$5"
        AdOption.PROMOTED -> "$10"
        AdOption.BANNER -> "$7"
    }

    /**
     * Creates a new event, uploads its image (if any), and saves it to Firestore.
     * @param title The title of the event.
     * @param description The description of the event.
     * @param adOption The advertisement option chosen for the event.
     * @param highlightedText Any special text to highlight the event.
     * @param imageUri The URI of the image to be uploaded, nullable.
     * @param locationName The name of the event's location.
     * @param onSuccess Callback to be invoked upon successful event creation.
     */
    fun createEvent(
        title: String,
        description: String,
        adOption: AdOption,
        highlightedText: String,
        imageUri: Uri?,
        locationName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Obtener el ID del usuario actual de Firebase Authentication
                val currentUserId = auth.currentUser?.uid ?: run {
                    _errorMessage.value = "Debes iniciar sesión para crear un evento."
                    _isLoading.value = false // Detener el estado de carga
                    return@launch // Salir de la coroutine ya que no hay usuario
                }

                val id = UUID.randomUUID().toString()
                var imageUrl: String? = null

                // Si hay una imagen, subirla a Firebase Storage
                if (imageUri != null) {
                    val ref = storage.reference.child("event_images/$id.jpg")
                    ref.putFile(imageUri).await() // Sube la imagen y espera a que termine
                    imageUrl = ref.downloadUrl.await().toString() // Obtiene la URL de descarga y espera
                }

                // Crear el objeto Event con todos los datos
                val event = Event(
                    id = id,
                    title = title,
                    description = description,
                    adOption = adOption.name,
                    highlightedText = highlightedText,
                    createdBy = currentUserId, // ¡Usamos el ID del usuario actual aquí!
                    timestamp = System.currentTimeMillis(),
                    locationName = locationName,
                    imageUrl = imageUrl
                )

                // Guardar el evento en Firestore
                firestore.collection("events").document(id).set(event).await() // Guarda el documento y espera

                onSuccess() // Llamar al callback de éxito
            } catch (e: Exception) {
                // Capturar y mostrar cualquier error que ocurra
                _errorMessage.value = e.localizedMessage ?: "Error desconocido al crear el evento."
            } finally {
                // Asegurarse de que el estado de carga se desactive siempre
                _isLoading.value = false
            }
        }
    }
}
