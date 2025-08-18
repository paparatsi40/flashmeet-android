package com.carlitoswy.flashmeet.presentation.payment

// ¡IMPORTANTE! Asegúrate de que esta importación sea correcta.
// Esta importación asume que tu archivo PaymentIntentResponse.kt
// se encuentra en la ruta:
// app/src/main/java/com/carlitoswy/flashmeet/data/model/PaymentIntentResponse.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.data.model.PaymentIntentResponse
import com.carlitoswy.flashmeet.data.repository.SharedFlyerPaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StripePaymentViewModel @Inject constructor(
    private val sharedFlyerPaymentRepository: SharedFlyerPaymentRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _statusText = MutableStateFlow("")
    val statusText: StateFlow<String> = _statusText

    val publishableKey: String = "pk_test_51KgF4kIHad7GoCUdlt2ifslj3nKHu4ad79MwhvKpQBVJrG8qRKE7z1fCfcYOdJPLpm8AT3fNelJkwWiL48STKu030006fulz3O"

    val clientSecret: StateFlow<String?> = sharedFlyerPaymentRepository.clientSecret

    init {
        // No necesitas código aquí a menos que quieras cargar el PaymentIntent
        // inmediatamente al crear el ViewModel, lo cual ya haces en fetchPaymentIntent().
    }

    fun fetchPaymentIntent() {
        viewModelScope.launch {
            _isLoading.value = true
            _statusText.value = "Fetching payment intent..."
            try {
                val pendingFlyer = sharedFlyerPaymentRepository.pendingFlyerData.value
                if (pendingFlyer != null) {
                    // Aquí es donde harías la llamada real a tu backend.
                    // Voy a simular la respuesta del backend como si viniera
                    // de un servicio Retrofit o similar.
                    // Las líneas de error 32, 33, 35 probablemente están dentro de este bloque.

                    // SIMULACIÓN DE LLAMADA A BACKEND:
                    // En un proyecto real, tendrías un servicio (ej. Retrofit) inyectado aquí
                    // y llamarías a un método para obtener el clientSecret de tu servidor.
                    // Por ejemplo: val response = myBackendService.createPaymentIntent(pendingFlyer)
                    // Y luego procesarías la respuesta:
                    // val paymentIntentResponse = response.body()

                    // Para que compile, y asumiendo que tu backend te devuelve un PaymentIntentResponse
                    // con un clientSecret:
                    val mockBackendResponse = PaymentIntentResponse(
                        clientSecret = "pi_YOUR_ACTUAL_CLIENT_SECRET_FROM_BACKEND_LIVE_OR_TEST" // Reemplaza con uno real
                    )

                    // Linea 32 (aprox.) - val fetchedResponse = ...
                    // Linea 33 (aprox.) - val paymentIntentResponse = fetchedResponse.body() o directamente mockBackendResponse
                    val fetchedSecret = mockBackendResponse.clientSecret // Linea 35 (aprox.)

                    if (fetchedSecret != null) {
                        sharedFlyerPaymentRepository.updateClientSecret(fetchedSecret)
                        _statusText.value = "Payment intent fetched!"
                    } else {
                        _statusText.value = "Error: Client secret not found in response."
                    }
                } else {
                    _statusText.value = "No pending flyer data found."
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _statusText.value = "Error fetching payment intent: ${e.localizedMessage}"
                _isLoading.value = false
            }
        }
    }

    fun setStatus(message: String) {
        _statusText.value = message
    }

    // Esta función probablemente la uses para limpiar el estado del flyer una vez pagado
    // o para disparar la finalización en el CreateFlyerViewModel.
    fun finalizeFlyerPaymentSuccess(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Aquí puedes añadir cualquier lógica de tu StripePaymentViewModel
            // para después de un pago exitoso (ej. notificar a tu backend).
            // Luego, limpia el estado pendiente del flyer si ya se ha procesado.
            sharedFlyerPaymentRepository.clearPendingFlyer()
            onSuccess() // Este callback lo usará la UI para navegar o mostrar un mensaje
        }
    }
}
