package com.carlitoswy.flashmeet.presentation.event // <--- Asegúrate que este sea el mismo paquete que el de CreateEventScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.FlashEvent
import com.carlitoswy.flashmeet.domain.model.AdOption // Necesitas importar AdOption
import com.carlitoswy.flashmeet.domain.usecase.CreateEventUseCase // Necesitas importar el UseCase si lo vas a usar
// Asegúrate de tener estas importaciones si FlashEventRepository está en un paquete diferente
// import com.carlitoswy.flashmeet.domain.repository.FlashEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri // Para manejar la Uri de la imagen

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    // Si realmente usas un repositorio directamente en el ViewModel para algo más, déjalo
    private val flashEventRepository: FlashEventRepository, // Renombrado para evitar conflicto con FlashEvent
    // ¡Ahora inyectamos el CreateEventUseCase, que es lo que llama tu función createEvent!
    private val createEventUseCase: CreateEventUseCase
) : ViewModel() { // <--- ¡AQUÍ ESTABA LA LLAVE DE APERTURA QUE FALTABA!

    private val _eventCreated = MutableStateFlow(false)
    val eventCreated: StateFlow<Boolean> = _eventCreated

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null) // Renombrado de _error a _errorMessage
    val errorMessage: StateFlow<String?> = _errorMessage // Expuesto como errorMessage

    // --- ¡NUEVA FUNCIÓN! `estimateAdCost` para que coincida con la llamada en la pantalla ---
    fun estimateAdCost(option: AdOption): String {
        return when (option) {
            AdOption.NONE -> "$0"
            AdOption.BASIC -> "$5"
            AdOption.PREMIUM -> "$15"
            AdOption.VIP -> "$50"
            // Considera si hay otras opciones de AdOption que no se estén manejando aquí
            // Si AdOption es un enum sellado o tiene un 'else' implícito, no es necesario un 'else'
        }
    }

    // --- FUNCIÓN `createEvent` MODIFICADA para que coincida con la llamada de la pantalla ---
    fun createEvent(
        title: String,
        description: String,
        adOption: AdOption,
        highlightedText: String,
        imageUri: Uri?, // La Uri de la imagen
        locationName: String,
        onSuccess: () -> Unit // Callback para cuando el evento se crea con éxito
    ) {
        _isLoading.value = true
        _errorMessage.value = null // Limpiar errores previos

        viewModelScope.launch {
            try {
                // Aquí construimos el objeto FlashEvent a partir de los parámetros
                val newEvent = FlashEvent(
                    id = "", // El ID se generará normalmente en la capa de datos/repositorio
                    title = title,
                    description = description,
                    highlightedText = highlightedText,
                    adOption = adOption,
                    imageUrl = imageUri?.toString(), // Convertir Uri a String para guardar
                    locationName = locationName,
                    // Asegúrate de añadir cualquier otra propiedad que tu FlashEvent necesite
                    // Por ejemplo, userId, timestamp, etc.
                    userId = "USUARIO_ACTUAL_ID", // <--- ¡IMPORTANTE! Reemplazar con el ID de usuario real
                    timestamp = System.currentTimeMillis() // O usa una timestamp del servidor
                )

                // Llamar al UseCase para crear el evento
                createEventUseCase(newEvent)
                _eventCreated.value = true
                onSuccess() // Llamar al callback de éxito después de crear el evento
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido al crear evento"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetEventCreatedFlag() {
        _eventCreated.value = false
    }
}
