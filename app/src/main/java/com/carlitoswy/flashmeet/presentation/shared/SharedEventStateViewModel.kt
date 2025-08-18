package com.carlitoswy.flashmeet.presentation.shared

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow // Import this
import kotlinx.coroutines.flow.StateFlow       // Import this
import kotlinx.coroutines.flow.asStateFlow     // Import this

@HiltViewModel
class SharedEventStateViewModel @Inject constructor(): ViewModel() {

    // 1. Declare a private MutableStateFlow
    private val _pendingEvent = MutableStateFlow<PendingEventData?>(null)

    // 2. Expose it as an immutable StateFlow for external observation
    val pendingEvent: StateFlow<PendingEventData?> = _pendingEvent.asStateFlow()

    fun savePendingEvent(event: PendingEventData) {
        // 3. Update the value of the MutableStateFlow
        _pendingEvent.value = event
    }

    fun clear() {
        _pendingEvent.value = null
    }
}
