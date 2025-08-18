package com.carlitoswy.flashmeet.presentation.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlitoswy.flashmeet.MainActivity
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.model.EventCategory
import com.carlitoswy.flashmeet.domain.repository.EventRepository
import com.carlitoswy.flashmeet.utils.EventFilterUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchCity = MutableStateFlow<String?>(null)
    private val _searchKeyword = MutableStateFlow<String?>(null)

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    private var lastEvents: List<Event> = emptyList()

    private val _eventAlert = MutableStateFlow(false)
    val eventAlert = _eventAlert.asStateFlow()

    private lateinit var locationClient: FusedLocationProviderClient

    fun initLocationClient(context: Context) {
        if (!::locationClient.isInitialized) {
            locationClient = LocationServices.getFusedLocationProviderClient(context)
            fetchUserLocation(context)
        }
    }

    fun onCategorySelected(category: EventCategory?) {
        _selectedCategory.value = category
    }

    fun onCitySearch(city: String?) {
        _searchCity.value = city
    }

    fun onKeywordSearch(keyword: String?) {
        _searchKeyword.value = keyword
    }

    @Suppress("MissingPermission")
    fun fetchUserLocation(context: Context) {
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                _userLocation.value = Pair(location.latitude, location.longitude)
                loadNearbyEvents(context)
            } else Log.w("HomeVM", "⚠️ No se pudo obtener ubicación")
        }.addOnFailureListener { e -> Log.e("HomeVM", "❌ Error ubicación: ${e.message}") }
    }

    private fun loadNearbyEvents(context: Context) {
        val (lat, lon) = _userLocation.value ?: return
        viewModelScope.launch {
            try {
                eventRepository.getNearbyEvents(lat, lon).collect { events ->
                    checkForNewEvents(events, context)
                    _allEvents.value = events
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "❌ Error cargando eventos: ${e.message}")
            }
        }
    }

    fun loadEventsNear(lat: Double, lon: Double, context: Context) {
        viewModelScope.launch {
            try {
                eventRepository.getNearbyEvents(lat, lon).collect { events ->
                    checkForNewEvents(events, context)
                    _allEvents.value = events
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "❌ Error cargando eventos cerca: ${e.message}")
            }
        }
    }

    val filteredEvents: StateFlow<List<Event>> =
        combine(_allEvents, _selectedCategory, _searchCity, _searchKeyword) { events, category, city, keyword ->
            EventFilterUtils.applyAllFilters(
                events = events,
                category = category,
                city = city,
                keyword = keyword
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun checkForNewEvents(newEvents: List<Event>, context: Context) {
        val newOnes = newEvents.filterNot { e -> lastEvents.any { it.id == e.id } }
        if (newOnes.isNotEmpty()) {
            Log.d("HomeVM", "🚀 Nuevos eventos detectados: ${newOnes.size}")
            triggerEventAlert(context, newOnes.first())
        }
        lastEvents = newEvents
    }

    private fun triggerEventAlert(context: Context, event: Event) {
        _eventAlert.value = true
        sendLocalNotification(context, event)
        viewModelScope.launch {
            delay(4000)
            _eventAlert.value = false
        }
    }

    private fun sendLocalNotification(context: Context, event: Event) {
        val channelId = "event_alerts"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, "Eventos Cercanos", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("eventId", event.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_flashmeet_logo)
            .setContentTitle("🎯 Nuevo evento cerca de ti")
            .setContentText(event.title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(event.id.hashCode(), notification)
        }
    }
}
