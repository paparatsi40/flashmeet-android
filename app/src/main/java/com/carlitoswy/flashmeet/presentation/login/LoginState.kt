package com.carlitoswy.flashmeet.presentation.login

import com.carlitoswy.flashmeet.domain.model.UserProfile

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserProfile) : LoginState()
    data class Error(val message: String) : LoginState()
}
