// com.carlitoswy.flashmeet.domain.repository.EventRepository.kt
package com.carlitoswy.flashmeet.domain.repository

import com.carlitoswy.flashmeet.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getMyEvents(): Flow<List<Event>>
    fun getNearbyEvents(latitude: Double, longitude: Double): Flow<List<Event>>
    suspend fun createEvent(event: Event)
    // AÑADE ESTA LÍNEA:
    suspend fun searchEvents(country: String, city: String, postal: String, category: String, date: String): List<Event>

    suspend fun getEventById(eventId: String): Event?
    suspend fun updateEvent(event: Event)

}
