package com.carlitoswy.flashmeet.domain.usecase

import com.carlitoswy.flashmeet.domain.model.FlashEvent
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import javax.inject.Inject

class CreateEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(event: FlashEvent) {
        repository.createEvent(event)
    }
}
