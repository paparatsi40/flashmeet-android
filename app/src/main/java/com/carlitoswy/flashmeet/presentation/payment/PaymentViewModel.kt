package com.carlitoswy.flashmeet.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.data.remote.payments.PaymentApi
import com.carlitoswy.flashmeet.data.remote.payments.StripePaymentRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentApi: PaymentApi
) : ViewModel() {

    private val _clientSecret = MutableStateFlow<String?>(null)
    val clientSecret: StateFlow<String?> = _clientSecret.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun requestPaymentIntent(
        amountCents: Long,
        currency: String = "usd",
        purpose: String? = null,
        metadata: Map<String, String>? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = paymentApi.createPaymentIntent(
                    StripePaymentRequest(
                        amount = amountCents,
                        currency = currency,
                        purpose = purpose,
                        metadata = metadata
                    )
                )
                _clientSecret.value = resp.clientSecret
                _error.value = null
            } catch (e: IOException) {
                _error.value = "Error de red: ${e.message}"
            } catch (e: HttpException) {
                _error.value = "Error del servidor: ${e.message}"
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clear() {
        _clientSecret.value = null
        _error.value = null
        _isLoading.value = false
    }
}
