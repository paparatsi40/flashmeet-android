package com.carlitoswy.flashmeet.presentation.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state

    fun sendResetEmail(email: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        emailSent = true
                    )
                }
                .addOnFailureListener {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = it.localizedMessage ?: "Error desconocido"
                    )
                }
        }
    }

    fun clearFlags() {
        _state.value = _state.value.copy(
            emailSent = false,
            error = null
        )
    }
}
