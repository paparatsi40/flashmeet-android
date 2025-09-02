package com.carlitoswy.flashmeet.presentation.event

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.FlashEvent
import com.carlitoswy.flashmeet.domain.model.AdOption
import com.carlitoswy.flashmeet.domain.usecase.CreateEventUseCase
import com.carlitoswy.flashmeet.domain.repository.FlashEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val flashEventRepository: FlashEventRepository,
    private val createEventUseCase: CreateEventUseCase
) : ViewModel() {

    private val _eventCreated = MutableStateFlow(false)
    val eventCreated: StateFlow<Boolean> = _eventCreated

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    var title: String = ""
    var description: String = ""
    var imageUri: Uri? = null

    fun isFormValid(): Boolean {
        return title.isNotBlank() && description.isNotBlank() && imageUri != null
    }

    fun estimateAdCost(option: AdOption): String {
        return when (option) {
            AdOption.NONE -> "$0"
            AdOption.BASIC -> "$5"
            AdOption.PREMIUM -> "$15"
            AdOption.VIP -> "$50"
        }
    }

    fun createEvent(
        title: String,
        description: String,
        adOption: AdOption,
        highlightedText: String,
        imageUri: Uri?,
        locationName: String,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val newEvent = FlashEvent(
                    id = "",
                    title = title,
                    description = description,
                    highlightedText = highlightedText,
                    adOption = adOption,
                    imageUrl = imageUri?.toString(),
                    locationName = locationName,
                    userId = "USUARIO_ACTUAL_ID",
                    timestamp = System.currentTimeMillis()
                )

                createEventUseCase(newEvent)
                _eventCreated.value = true
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido al crear evento"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetEventCreatedFlag() {
        _eventCreated.value = false
    }
}
