package com.carlitoswy.flashmeet.presentation.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.model.EventCategory
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: EventRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Event>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // 🗓 Filtros seleccionados
    val dateFilters = listOf("Todos", "Hoy", "Esta semana", "Este mes")
    private val _selectedDateFilter = MutableStateFlow("Todos")
    val selectedDateFilter = _selectedDateFilter.asStateFlow()

    val categoryFilters = EventCategory.entries.map { it.displayName() }.toMutableList().apply { add(0, "Todos") }
    private val _selectedCategoryFilter = MutableStateFlow("Todos")
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    // Resultado filtrado
    val filteredResults = combine(
        _searchResults, _selectedDateFilter, _selectedCategoryFilter
    ) { list, date, categoryDisplayName ->
        list.filter { event ->
            // 🔄 Comparamos con el displayName real del EventCategory
            val matchCategory = categoryDisplayName == "Todos" ||
                    EventCategory.fromString(event.category.toString())?.displayName() == categoryDisplayName

            val matchDate = when (date) {
                "Todos" -> true
                "Hoy" -> {
                    val today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
                    val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    event.timestamp in start until end
                }

                "Esta semana" -> {
                    val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
                    val start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                        .plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    event.timestamp in start until end
                }

                "Este mes" -> {
                    val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
                    val start = now.with(TemporalAdjusters.firstDayOfMonth())
                        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val end = now.with(TemporalAdjusters.lastDayOfMonth())
                        .plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    event.timestamp in start until end
                }

                else -> true
            }

            matchCategory && matchDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDateFilter(value: String) {
        _selectedDateFilter.value = value
    }

    fun setCategoryFilter(value: String) {
        _selectedCategoryFilter.value = value
    }

    fun search(
        country: String,
        city: String,
        postal: String,
        category: String,
        date: String
    ) = viewModelScope.launch {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val results = repo.searchEvents(country, city, postal, category, date)
            _searchResults.value = results
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
        } finally {
            _isLoading.value = false
        }
    }
}
