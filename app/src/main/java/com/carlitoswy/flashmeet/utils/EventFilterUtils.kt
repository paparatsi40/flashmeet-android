package com.carlitoswy.flashmeet.utils

import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.model.EventCategory

/**
 * 🔍 Herramientas reutilizables para filtrar eventos de forma modular y testeable.
 */
object EventFilterUtils {

    /**
     * 🔹 Filtra por categoría (si se especifica)
     */
    fun filterByCategory(events: List<Event>, category: EventCategory?): List<Event> {
        return if (category == null) events
        else events.filter { it.category == category }
    }

    /**
     * 🌍 Filtra por ciudad (ignora mayúsculas y tildes)
     */
    fun filterByCity(events: List<Event>, city: String?): List<Event> {
        return if (city.isNullOrBlank()) events
        else events.filter { it.city.equals(city, ignoreCase = true) }
    }

    /**
     * 📝 Filtra por palabra clave en título o descripción
     */
    fun filterByKeyword(events: List<Event>, keyword: String?): List<Event> {
        return if (keyword.isNullOrBlank()) events
        else events.filter {
            it.title.contains(keyword, ignoreCase = true) ||
                    it.description.contains(keyword, ignoreCase = true)
        }
    }

    /**
     * 🧠 Combina múltiples filtros de forma intuitiva
     */
    fun applyAllFilters(
        events: List<Event>,
        category: EventCategory? = null,
        city: String? = null,
        keyword: String? = null
    ): List<Event> {
        return events
            .let { filterByCategory(it, category) }
            .let { filterByCity(it, city) }
            .let { filterByKeyword(it, keyword) }
    }
}
