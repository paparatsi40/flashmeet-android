package com.carlitoswy.flashmeet.presentation.flyer

import android.net.Uri

data class CreateFlyerUiState(
    val title: String = "",
    val description: String = "",
    val imageUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
