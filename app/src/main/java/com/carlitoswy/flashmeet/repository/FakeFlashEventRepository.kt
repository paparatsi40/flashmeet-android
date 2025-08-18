package com.carlitoswy.flashmeet.repository

import com.carlitoswy.flashmeet.domain.model.FlashEvent
import com.carlitoswy.flashmeet.domain.repository.FlashEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFlashEventRepository : FlashEventRepository {

    override suspend fun createEvent(event: FlashEvent): Result<Unit> {
        // No-op para tests
        return Result.success(Unit) // <-- Added the return statement
    }

    override suspend fun getNearbyEvents(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Flow<List<FlashEvent>> {
        // Devuelve una lista falsa de eventos para pruebas
        return flowOf(
            listOf(
                FlashEvent(
                    id = "1",
                    title = "Evento de prueba",
                    description = "Este es un evento cercano.",
                    latitude = latitude,
                    longitude = longitude,
                    time = System.currentTimeMillis(),
                    creatorId = "user123"
                )
            )
        )
    }
}
