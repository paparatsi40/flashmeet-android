package com.carlitoswy.flashmeet.presentation.createevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.FlashEvent
import com.carlitoswy.flashmeet.domain.repository.FlashEventRepository
import com.carlitoswy.flashmeet.domain.usecase.GetSignedInUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// State class to hold the input field values
data class EventState(
    val title: String = "",
    val description: String = "",
    // You might add state for loading, errors, success for the submission process here too
    // val isLoading: Boolean = false,
    // val submissionError: String? = null,
    // val isSubmitted: Boolean = false
)

// You might also define a separate sealed class for submission state:
// sealed class EventSubmissionState {
//     object Idle : EventSubmissionState()
//     object Loading : EventSubmissionState()
//     object Success : EventSubmissionState()
//     data class Error(val message: String) : EventSubmissionState()
// }


@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventRepository: FlashEventRepository,
    private val getSignedInUserUseCase: GetSignedInUserUseCase // Use case to get the currently signed-in UserProfile
) : ViewModel() {

    // StateFlow for the input fields
    private val _eventState = MutableStateFlow(EventState())
    val eventState = _eventState.asStateFlow()

    // Optional: StateFlow for the event submission process state
    // private val _submissionState = MutableStateFlow<EventSubmissionState>(EventSubmissionState.Idle)
    // val submissionState = _submissionState.asStateFlow()

    fun onTitleChanged(newTitle: String) {
        _eventState.value = _eventState.value.copy(title = newTitle)
    }

    fun onDescriptionChanged(newDescription: String) {
        _eventState.value = _eventState.value.copy(description = newDescription)
    }

    fun submitEvent() {
        viewModelScope.launch {
            // Optional: Update submission state to Loading
            // _submissionState.value = EventSubmissionState.Loading

            val user = getSignedInUserUseCase() // Get the signed-in user profile
            if (user == null) {
                // Handle case where user is not signed in
                // Optional: Update submission state to Error
                // _submissionState.value = EventSubmissionState.Error("User not signed in.")
                return@launch
            }

            val currentEventState = _eventState.value

            // Ensure title and description are not empty (optional, add validation)
            if (currentEventState.title.isBlank() || currentEventState.description.isBlank()) {
                // Optional: Update submission state to Error
                // _submissionState.value = EventSubmissionState.Error("Title and description cannot be empty.")
                return@launch // Or show a message to the user
            }

            // Create the FlashEvent object
            val newEvent = FlashEvent(
                id = UUID.randomUUID().toString(),
                title = currentEventState.title,
                description = currentEventState.description,
                creatorId = user.uid, // Use user.uid for creatorId
                // FIX: Use user.name and provide a default value if user.name is nullable
                // Assuming FlashEvent.createdBy is String (non-nullable)
                createdBy = user.name ?: user.email ?: "Unknown User" // Use name, fallback to email, then "Unknown User" if both are null
            )

            // Save the event via the repository
            try {
                eventRepository.createEvent(newEvent)
                // Optional: Update submission state to Success
                // _submissionState.value = EventSubmissionState.Success

                // Clear input fields after successful submission (optional)
                _eventState.value = EventState()

            } catch (e: Exception) {
                // Handle creation error
                // Optional: Update submission state to Error
                // _submissionState.value = EventSubmissionState.Error(e.localizedMessage ?: "Failed to create event")
            }
        }
    }

    // Optional function to reset submission state
    // fun resetSubmissionState() {
    //     _submissionState.value = EventSubmissionState.Idle
    // }
}
