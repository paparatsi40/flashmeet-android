package com.carlitoswy.flashmeet.domain.repository

import com.carlitoswy.flashmeet.domain.model.FlashEvent
import kotlinx.coroutines.flow.Flow

interface FlashEventRepository {
    suspend fun createEvent(event: FlashEvent): Result<Unit>
    suspend fun getNearbyEvents(latitude: Double, longitude: Double, radiusInKm: Double): Flow<List<FlashEvent>>
}
