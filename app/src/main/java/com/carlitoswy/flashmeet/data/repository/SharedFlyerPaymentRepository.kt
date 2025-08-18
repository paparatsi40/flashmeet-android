package com.carlitoswy.flashmeet.data.repository // <-- ¡Asegúrate de que el paquete sea este!

import android.net.Uri
import com.carlitoswy.flashmeet.domain.model.AdOption // Importa tu clase AdOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Puedes definir una clase de datos para agrupar la información del flyer pendiente
data class PendingFlyerData(
    val title: String,
    val description: String,
    val imageUri: Uri?,
    val bgColor: Int,
    val fontName: String,
    val locationLabel: String,
    val createdBy: String,
    val adOption: AdOption, // Usamos la clase AdOption importada
    val highlightedText: String,
    var clientSecret: String? = null // El clientSecret se guardará aquí si es necesario
)

@Singleton
class SharedFlyerPaymentRepository @Inject constructor() {

    // MutableStateFlow para mantener los datos del flyer pendientes de pago
    private val _pendingFlyerData = MutableStateFlow<PendingFlyerData?>(null)
    val pendingFlyerData: StateFlow<PendingFlyerData?> = _pendingFlyerData

    // MutableStateFlow para el clientSecret, si necesitas que se observe individualmente
    // Es posible que ya esté dentro de PendingFlyerData, pero lo dejo como opción
    private val _clientSecret = MutableStateFlow<String?>(null)
    val clientSecret: StateFlow<String?> = _clientSecret

    fun setPendingFlyer(data: PendingFlyerData) {
        _pendingFlyerData.value = data
        // También puedes limpiar el clientSecret anterior si se establece un nuevo flyer
        _clientSecret.value = null
    }

    fun updateClientSecret(secret: String) {
        _clientSecret.value = secret
        // O si clientSecret es parte de PendingFlyerData:
        // _pendingFlyerData.value = _pendingFlyerData.value?.copy(clientSecret = secret)
    }

    fun clearPendingFlyer() {
        _pendingFlyerData.value = null
        _clientSecret.value = null
    }

    // Método para obtener el clientSecret de la data pendiente si se actualiza allí
    fun getPendingFlyerClientSecret(): String? {
        return _pendingFlyerData.value?.clientSecret
    }
}
