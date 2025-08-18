package com.carlitoswy.flashmeet.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    fun fetchEvents() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("events").get().await()
                val eventList = snapshot.toObjects(Event::class.java)
                _events.value = eventList
            } catch (e: Exception) {
                // Log error si deseas
            }
        }
    }
}
