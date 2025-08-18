package com.carlitoswy.flashmeet.domain.usecase.event

import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import javax.inject.Inject

class SearchEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(
        country: String,
        city: String,
        postal: String,
        category: String,
        date: String
    ): List<Event> {
        return repository.searchEvents(country, city, postal, category, date)
    }
}
