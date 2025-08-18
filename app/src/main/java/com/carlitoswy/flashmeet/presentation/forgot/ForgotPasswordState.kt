package com.carlitoswy.flashmeet.presentation.forgot

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
    val error: String? = null
)
