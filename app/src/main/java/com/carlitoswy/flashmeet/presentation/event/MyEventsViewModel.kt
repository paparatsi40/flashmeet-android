package com.carlitoswy.flashmeet.presentation.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyEventsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    val myEvents = eventRepository.getMyEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Simple loading state toggled true while events loading
    val isLoading: StateFlow<Boolean> = myEvents
        .map { false } // puedes enlazar a un loading real si usas un repositorio sofisticado
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        refreshEvents()
    }

    fun refreshEvents() {
        viewModelScope.launch {
            try {
                // Si tu repositorio lanza excepciones, atrápalas aquí
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }
}
