package com.carlitoswy.flashmeet.presentation.flyer

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.data.location.LocationService
import com.carlitoswy.flashmeet.data.repository.PendingFlyerData
import com.carlitoswy.flashmeet.data.repository.SharedFlyerPaymentRepository
import com.carlitoswy.flashmeet.domain.model.AdOption
import com.carlitoswy.flashmeet.domain.model.Flyer
import com.carlitoswy.flashmeet.utils.await
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateFlyerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val locationService: LocationService,
    private val sharedFlyerPaymentRepository: SharedFlyerPaymentRepository // <-- ¡NUEVA INYECCIÓN!
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation

    private val storage = FirebaseStorage.getInstance().reference

    init {
        viewModelScope.launch {
            locationService.getLastKnownLocation()?.let { loc ->
                _currentLocation.value = GeoPoint(loc.latitude, loc.longitude)
            }
        }
    }

    fun estimateAdCost(adOption: AdOption): String = when (adOption) {
        AdOption.NONE -> "Gratis"
        AdOption.HIGHLIGHTED -> "$5"
        AdOption.PROMOTED -> "$10"
        AdOption.BANNER -> "$7"
    }

    /**
     * Guarda el flyer directamente en Firestore y Storage (para flyers sin pago o después de pago).
     */
    fun createFlyer(
        title: String,
        description: String,
        imageUri: Uri?,
        bgColor: Int,
        fontName: String,
        locationLabel: String,
        createdBy: String,
        adOption: AdOption,
        highlightedText: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val id = UUID.randomUUID().toString()
                val location = currentLocation.value ?: GeoPoint(0.0, 0.0)

                var imageUrl = ""
                imageUri?.let {
                    val fileRef = storage.child("flyers/$id.jpg")
                    // .await() es una función de extensión que convierte Tasks a suspend functions
                    fileRef.putFile(it).await()
                    imageUrl = fileRef.downloadUrl.await().toString()
                }

                val flyer = Flyer(
                    id = id,
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    createdBy = createdBy,
                    timestamp = System.currentTimeMillis(),
                    location = location,
                    bgColor = bgColor,
                    fontName = fontName,
                    adOption = adOption.name,
                    highlightedText = highlightedText
                )

                firestore.collection("flyers").document(id).set(flyer).await()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda los datos de un flyer de forma temporal en el SharedFlyerPaymentRepository.
     * Esto se usa cuando el flyer requiere pago y necesita ser finalizado después.
     */
    fun savePendingFlyer(
        title: String,
        description: String,
        imageUri: Uri?,
        bgColor: Int,
        fontName: String,
        locationLabel: String,
        createdBy: String,
        adOption: AdOption,
        highlightedText: String
    ) {
        val pendingData = PendingFlyerData(
            title = title,
            description = description,
            imageUri = imageUri,
            bgColor = bgColor,
            fontName = fontName,
            locationLabel = locationLabel,
            createdBy = createdBy,
            adOption = adOption,
            highlightedText = highlightedText
        )
        // Guarda los datos del flyer en el repositorio compartido
        sharedFlyerPaymentRepository.setPendingFlyer(pendingData)
        Log.d("CreateFlyerViewModel", "Flyer pendiente guardado en el repositorio.")
    }

    /**
     * Función que finaliza el proceso de creación del flyer después de un pago exitoso.
     * Esta función podría ser llamada desde el StripePaymentViewModel o desde la Activity/Screen
     * después de recibir la confirmación de pago.
     *
     * @param onSuccess Callback para cuando el flyer ha sido guardado exitosamente.
     */
    fun finalizeFlyerAfterPayment(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val pendingData = sharedFlyerPaymentRepository.pendingFlyerData.value
                if (pendingData != null) {
                    val id = UUID.randomUUID().toString() // Podrías almacenar el ID también en PendingFlyerData si lo generas antes
                    val location = currentLocation.value ?: GeoPoint(0.0, 0.0)

                    var imageUrl = ""
                    pendingData.imageUri?.let { uri ->
                        val fileRef = storage.child("flyers/$id.jpg")
                        fileRef.putFile(uri).await()
                        imageUrl = fileRef.downloadUrl.await().toString()
                    }

                    val flyer = Flyer(
                        id = id,
                        title = pendingData.title,
                        description = pendingData.description,
                        imageUrl = imageUrl,
                        createdBy = pendingData.createdBy,
                        timestamp = System.currentTimeMillis(),
                        location = location,
                        bgColor = pendingData.bgColor,
                        fontName = pendingData.fontName,
                        adOption = pendingData.adOption.name,
                        highlightedText = pendingData.highlightedText
                    )

                    firestore.collection("flyers").document(id).set(flyer).await()
                    sharedFlyerPaymentRepository.clearPendingFlyer() // Limpia el estado del repositorio
                    onSuccess()
                } else {
                    _errorMessage.value = "No hay datos de flyer pendientes para finalizar."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al finalizar flyer después del pago: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
