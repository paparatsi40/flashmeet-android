package com.carlitoswy.flashmeet.domain.usecase

import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.carlitoswy.flashmeet.model.FlashEvent
import javax.inject.Inject

class GetNearbyEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): List<FlashEvent> {
        return repository.getNearbyEvents()
    }
}
