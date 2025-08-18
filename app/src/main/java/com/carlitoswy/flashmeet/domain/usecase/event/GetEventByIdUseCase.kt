package com.carlitoswy.flashmeet.domain.usecase.event

import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Event? {
        return repository.getEventById(eventId)
    }
}
