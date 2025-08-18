package com.carlitoswy.flashmeet.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        _authState.update {
            it.copy(isAuthenticated = auth.currentUser != null)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signInWithGoogle(idToken)

            _authState.update { currentState ->
                if (result.isSuccess) {
                    currentState.copy(isAuthenticated = true, isLoading = false)
                } else {
                    val errorMessage = handleFirebaseAuthError(result.exceptionOrNull())
                    currentState.copy(isAuthenticated = false, isLoading = false, error = errorMessage)
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.update { it.copy(isAuthenticated = true, isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = handleFirebaseAuthError(e)
                _authState.update { it.copy(isAuthenticated = false, isLoading = false, error = errorMessage) }
            }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.update { it.copy(isAuthenticated = true, isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = handleFirebaseAuthError(e)
                _authState.update { it.copy(isAuthenticated = false, isLoading = false, error = errorMessage) }
            }
        }
    }

    fun setError(message: String?) {
        _authState.update { it.copy(error = message) }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            _authState.update { it.copy(isAuthenticated = false, isLoading = false, error = null) }
        }
    }

    private fun handleFirebaseAuthError(e: Throwable?): String {
        return when (e) {
            is FirebaseAuthWeakPasswordException -> context.getString(R.string.auth_error_weak_password)
            is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.auth_error_invalid_credentials)
            is FirebaseAuthInvalidUserException -> context.getString(R.string.auth_error_user_not_found)
            is FirebaseAuthUserCollisionException -> context.getString(R.string.auth_error_email_in_use)
            is com.google.android.gms.common.api.ApiException -> {
                when (e.statusCode) {
                    com.google.android.gms.common.api.CommonStatusCodes.CANCELED ->
                        context.getString(R.string.auth_error_google_canceled)
                    com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR ->
                        context.getString(R.string.auth_error_network)
                    else ->
                        context.getString(R.string.auth_error_google_signin_failed, e.localizedMessage ?: "Code ${e.statusCode}")
                }
            }
            else -> e?.localizedMessage ?: context.getString(R.string.auth_error_unknown)
        }
    }
}
