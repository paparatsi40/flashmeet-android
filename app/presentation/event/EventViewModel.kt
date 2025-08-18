package com.carlitoswy.flashmeet.presentation.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.FlashEvent
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    fun createEvent(
        name: String,
        sticker: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val event = FlashEvent(name = name, sticker = sticker)
                repository.createEvent(event)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al crear evento")
            }
        }
    }
}
