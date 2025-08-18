package com.carlitoswy.flashmeet.domain.model

/**
 * ✅ Convierte el campo category (String) de un Event en un EventCategory?.
 */
fun Event.toCategoryEnum(): EventCategory? {
    return EventCategory.fromString(this.category.toString())
}
