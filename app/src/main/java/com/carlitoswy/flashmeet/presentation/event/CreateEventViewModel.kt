package com.carlitoswy.flashmeet.presentation.event

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.AdOption
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.model.toFirestoreString
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState

    // ✅ Inputs
    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onDescriptionChanged(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun onCityChanged(city: String) {
        _uiState.value = _uiState.value.copy(city = city)
    }

    fun onDateChanged(date: String) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onAdOptionSelected(option: AdOption) {
        _uiState.value = _uiState.value.copy(adOption = option)
    }

    fun onFontFamilyChanged(font: String) {
        _uiState.value = _uiState.value.copy(flyerFontFamily = font)
    }

    fun onFlyerBackgroundColorChange(colorHex: String) {
        _uiState.value = _uiState.value.copy(flyerBackgroundColor = colorHex)
    }

    fun onCreateEvent(context: Context) {
        createEvent()
    }

    // ✅ Imagen
    fun onImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    // ✅ Diálogos
    fun showTextColorPicker() {
        _uiState.value = _uiState.value.copy(showFlyerTextColorPicker = true)
    }

    fun hideTextColorPicker() {
        _uiState.value = _uiState.value.copy(showFlyerTextColorPicker = false)
    }

    fun showBackgroundColorPicker() {
        _uiState.value = _uiState.value.copy(showFlyerBackgroundColorPicker = true)
    }

    fun hideBackgroundColorPicker() {
        _uiState.value = _uiState.value.copy(showFlyerBackgroundColorPicker = false)
    }

    fun showFontPicker() {
        _uiState.value = _uiState.value.copy(showFlyerFontPicker = true)
    }

    fun hideFontPicker() {
        _uiState.value = _uiState.value.copy(showFlyerFontPicker = false)
    }

    // ✅ Crear evento
    fun createEvent() {
        val state = _uiState.value
        if (state.title.isBlank() || state.description.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Completa todos los campos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            try {
                val eventId = java.util.UUID.randomUUID().toString()
                var finalImageUrl: String? = null

                state.imageUri?.let { uri ->
                    finalImageUrl = repository.uploadEventImage(uri, eventId)
                    if (finalImageUrl == null) {
                        _uiState.value = state.copy(isLoading = false, errorMessage = "Error al subir la imagen.")
                        return@launch
                    }
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                    _uiState.value = state.copy(isLoading = false, errorMessage = "Usuario no autenticado.")
                    return@launch
                }

                val newEvent = Event(
                    id = eventId,
                    title = state.title,
                    description = state.description,
                    createdBy = userId,
                    imageUrl = finalImageUrl,
                    flyerTextColor = state.flyerTextColor,
                    flyerBackgroundColor = state.flyerBackgroundColor,
                    flyerFontFamily = state.flyerFontFamily,
                    adOption = state.adOption.toFirestoreString(),
                    latitude = state.latitude ?: 0.0,
                    longitude = state.longitude ?: 0.0,
                    city = state.city,
                    timestamp = System.currentTimeMillis()
                )

                repository.createEvent(newEvent)
                _uiState.value = state.copy(
                    success = true,
                    isLoading = false,
                    newEventId = eventId // ✅ Nuevo campo para redirección
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun reset() {
        _uiState.value = CreateEventUiState()
    }

    fun updateLocation(context: Context) {
        viewModelScope.launch {
            val location = getLastKnownLocation(context)
            _uiState.value = _uiState.value.copy(
                latitude = location?.latitude,
                longitude = location?.longitude
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastKnownLocation(context: Context): Location? {
        return withContext(Dispatchers.IO) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)

            for (provider in providers.reversed()) {
                try {
                    val location = locationManager.getLastKnownLocation(provider)
                    if (location != null) return@withContext location
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
            return@withContext null
        }
    }
}
