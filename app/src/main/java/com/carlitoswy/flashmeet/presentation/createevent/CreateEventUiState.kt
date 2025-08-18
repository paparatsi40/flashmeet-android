// presentation/createevent/CreateEventUiState.kt
package com.carlitoswy.flashmeet.presentation.createevent

sealed class CreateEventUiState {
    object Idle : CreateEventUiState()
    object Loading : CreateEventUiState()
    object Success : CreateEventUiState()
    data class Error(val message: String) : CreateEventUiState()
}
