package com.carlitoswy.flashmeet.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.data.location.LocationService
import com.carlitoswy.flashmeet.data.repository.EventSearchRepository
import com.carlitoswy.flashmeet.domain.model.Flyer
import com.carlitoswy.flashmeet.utils.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventSearchViewModel @Inject constructor(
    private val repo: EventSearchRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _items = MutableStateFlow<List<Flyer>>(emptyList())
    val items: StateFlow<List<Flyer>> = _items

    // Filtros controlados por UI
    val text = MutableStateFlow("")
    val radiusKm = MutableStateFlow(25.0)
    val fromDateMillis = MutableStateFlow<Long?>(null)
    val toDateMillis = MutableStateFlow<Long?>(null)
    val promotedOnly = MutableStateFlow(false)

    fun searchAroundMe() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val loc = locationService.getLastKnownLocation()
                val center = if (loc != null) LatLng(loc.latitude, loc.longitude)
                else LatLng(0.0, 0.0)

                val result = repo.searchFlyers(
                    center = center,
                    radiusKm = radiusKm.value,
                    text = text.value.ifBlank { null },
                    fromDateMillis = fromDateMillis.value,
                    toDateMillis = toDateMillis.value,
                    promotedOnly = promotedOnly.value
                )
                _items.value = result
            } finally {
                _loading.value = false
            }
        }
    }
}
