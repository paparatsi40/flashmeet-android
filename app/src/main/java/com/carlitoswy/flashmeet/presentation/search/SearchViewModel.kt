package com.carlitoswy.flashmeet.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val TAG = "SearchVM"

    private val _searchResults = MutableStateFlow<List<Event>>(emptyList())
    val searchResults: StateFlow<List<Event>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun searchEvents(country: String, city: String, postal: String, category: String, date: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔍 Iniciando búsqueda → country=$country | city=$city | postal=$postal | category=$category | date=$date")
            _isLoading.value = true

            try {
                val results = eventRepository.searchEvents(country, city, postal, category, date)
                Log.d(TAG, "✅ Resultados obtenidos: ${results.size} eventos")
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al buscar eventos: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
