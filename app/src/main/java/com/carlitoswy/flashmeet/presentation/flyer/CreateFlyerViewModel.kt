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
import com.google.firebase.firestore.SetOptions
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
    private val sharedFlyerPaymentRepository: SharedFlyerPaymentRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation

    // 🔹 ID del borrador que se crea al entrar a la pantalla y se va mergeando
    private val _draftId = MutableStateFlow<String?>(null)
    val draftId: StateFlow<String?> = _draftId

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

    // Helper to compute price in cents
    private fun getPriceInCents(adOption: AdOption): Long = when (adOption) {
        AdOption.NONE -> 0L
        AdOption.HIGHLIGHTED -> 500L
        AdOption.PROMOTED -> 1000L
        AdOption.BANNER -> 700L
    }

    /**
     * Crea (si no existe) un documento borrador en /flyers/{draftId} para mostrar en tiempo real.
     * Llamar al entrar a la pantalla de creación.
     */
    fun beginDraft(createdBy: String, locationLabel: String) {
        if (_draftId.value != null) return
        val id = UUID.randomUUID().toString()
        _draftId.value = id

        viewModelScope.launch {
            try {
                val location = currentLocation.value ?: GeoPoint(0.0, 0.0)
                val draft = Flyer(
                    id = id,
                    title = "",
                    description = "",
                    imageUrl = "",
                    createdBy = createdBy,
                    timestamp = System.currentTimeMillis(),
                    location = location,
                    bgColor = 0,
                    fontName = "Sans",
                    adOption = AdOption.NONE.name,
                    highlightedText = "",
                    eventId = id,
                    priceCents = getPriceInCents(AdOption.NONE),
                    ownerId = createdBy,
                    dateMillis = System.currentTimeMillis(),
                    city = locationLabel
                )
                firestore.collection("flyers").document(id)
                    .set(draft, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w("CreateFlyerVM", "No se pudo crear draft: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Actualiza parcialmente el borrador (merge).
     */
    fun updateDraft(fields: Map<String, Any?>) {
        val id = _draftId.value ?: return
        viewModelScope.launch {
            try {
                val patch = fields.filterValues { it != null }
                if (patch.isNotEmpty()) {
                    firestore.collection("flyers").document(id)
                        .set(patch, SetOptions.merge())
                        .await()
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * Crea y guarda el flyer inmediatamente (flujo SIN pago).
     * Reutiliza el draftId si existe para que el documento no cambie.
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
                val id = _draftId.value ?: UUID.randomUUID().toString()
                val location = currentLocation.value ?: GeoPoint(0.0, 0.0)
                val priceInCents = getPriceInCents(adOption)

                var imageUrl = ""
                imageUri?.let {
                    val fileRef = storage.child("flyers/$id.jpg")
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
                    highlightedText = highlightedText,
                    eventId = id,
                    priceCents = priceInCents,
                    ownerId = createdBy,
                    dateMillis = System.currentTimeMillis(),
                    city = locationLabel
                )

                firestore.collection("flyers").document(id).set(flyer).await()
                _draftId.value = null
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda los datos de un flyer de forma temporal para flujo CON pago.
     * Pre-genera un eventId estable y precalcula el priceCents.
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
        val preEventId = _draftId.value ?: UUID.randomUUID().toString()
        _draftId.value = preEventId
        val priceInCents = getPriceInCents(adOption)

        val pendingData = PendingFlyerData(
            title = title,
            description = description,
            imageUri = imageUri,
            bgColor = bgColor,
            fontName = fontName,
            locationLabel = locationLabel,
            createdBy = createdBy,
            adOption = adOption,
            highlightedText = highlightedText,
            eventId = preEventId,
            priceCents = priceInCents
        )
        sharedFlyerPaymentRepository.setPendingFlyer(pendingData)

        // Asegura que el draft tenga los últimos valores visibles en tiempo real
        updateDraft(
            mapOf(
                "title" to title,
                "description" to description,
                "bgColor" to bgColor,
                "fontName" to fontName,
                "city" to locationLabel,
                "adOption" to adOption.name,
                "highlightedText" to highlightedText,
                "eventId" to preEventId,
                "priceCents" to priceInCents,
                "ownerId" to createdBy
            )
        )
        Log.d("CreateFlyerViewModel", "Flyer pendiente guardado. eventId=$preEventId, priceCents=$priceInCents")
    }

    /**
     * Finaliza el proceso de creación del flyer después de un pago exitoso.
     * Reutiliza el mismo eventId/draftId para coherencia con Stripe.
     */
    fun finalizeFlyerAfterPayment(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val pendingData = sharedFlyerPaymentRepository.pendingFlyerData.value
                if (pendingData != null) {
                    val id = _draftId.value ?: pendingData.eventId ?: UUID.randomUUID().toString()
                    val location = currentLocation.value ?: GeoPoint(0.0, 0.0)
                    val priceInCents = pendingData.priceCents ?: getPriceInCents(pendingData.adOption)

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
                        highlightedText = pendingData.highlightedText,
                        eventId = id,
                        priceCents = priceInCents,
                        ownerId = pendingData.createdBy,
                        dateMillis = System.currentTimeMillis(),
                        city = pendingData.locationLabel
                    )

                    firestore.collection("flyers").document(id).set(flyer).await()
                    sharedFlyerPaymentRepository.clearPendingFlyer()
                    _draftId.value = null
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

    /** Útil para Stripe: devuelve el eventId actual (preferimos draftId si existe). */
    fun currentEventId(): String? =
        _draftId.value ?: sharedFlyerPaymentRepository.pendingFlyerData.value?.eventId
}
