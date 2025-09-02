@file:OptIn(
    ExperimentalMaterial3Api::class,
    com.google.accompanist.permissions.ExperimentalPermissionsApi::class
)

package com.carlitoswy.flashmeet.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.data.preferences.UserPreferences
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.presentation.components.EventCategoryFilterChips
import com.carlitoswy.flashmeet.presentation.components.EventDetailBottomSheet
import com.carlitoswy.flashmeet.presentation.components.MapSearchBar
import com.carlitoswy.flashmeet.presentation.components.RadarFab
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@OptIn(MapsComposeExperimentalApi::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    navController: NavController,
    initialFocusId: String? = null,
    initialFocusLat: Double? = null,
    initialFocusLon: Double? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val events by viewModel.filteredEvents.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isAlertActive by viewModel.eventAlert.collectAsState()
    var showListView by remember { mutableStateOf(false) }
    val userPrefs = remember { UserPreferences(context) }
    val showListPref by userPrefs.showListFlow.collectAsState(initial = false)

    val cameraPositionState = rememberCameraPositionState()
    val scope = rememberCoroutineScope()

    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }

    // filtros/orden
    var showOnlyHighlightedEventsFilter by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(SortBy.DISTANCE) }

    // Sheets
    var showListSheet by remember { mutableStateOf(showListPref) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Intereses
    val interestsVM: EventInterestsViewModel = hiltViewModel()
    val interestedIds by interestsVM.interestedEventIds.collectAsState()
    LaunchedEffect(Unit) { interestsVM.refreshIfNeeded() }

    // Cluster preview / lista filtrada por cluster
    var clusterPreview by remember { mutableStateOf<ClusterPreviewState?>(null) }
    var listFromCluster by remember { mutableStateOf<List<Event>?>(null) }

    // Mapa (tema)
    val mapSettingsVM: MapSettingsViewModel = hiltViewModel()
    val darkMap by mapSettingsVM.darkMap.collectAsState()

    // ---- Permisos de ubicación (Accompanist) ----
    val locationPerms = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(Unit) {
        if (!locationPerms.allPermissionsGranted) {
            locationPerms.launchMultiplePermissionRequest()
        }
    }
    val hasLocationPermission = locationPerms.permissions.any { it.status.isGranted }

    // Inicia ubicación solo si hay permiso
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) viewModel.initLocationClient(context)
    }

    // UX: vibración al activar alerta
    LaunchedEffect(isAlertActive) { if (isAlertActive) vibrateDevice(context) }

    // Centrar cámara cuando llega ubicación
    LaunchedEffect(userLocation) {
        userLocation?.let { (lat, lon) ->
            scope.launch { cameraPositionState.centerOnLocation(lat, lon) }
        }
    }

    // ---------- Fallback deeplink: inyectar evento externo si no está en la lista ----------
    val firestore = remember { FirebaseFirestore.getInstance() }
    var externalFocusEvent by remember { mutableStateOf<Event?>(null) }

    // Lista final para el mapa: events + (externo si no está ya)
    val mapEvents = remember(events, externalFocusEvent) {
        externalFocusEvent?.let { ext ->
            if (events.none { it.id == ext.id }) events + ext else events
        } ?: events
    }

    // Centrar por coordenadas si llegan en deeplink
    LaunchedEffect(initialFocusLat, initialFocusLon) {
        if (initialFocusLat != null && initialFocusLon != null) {
            scope.launch {
                cameraPositionState.centerOnLocation(initialFocusLat, initialFocusLon, zoom = 16f)
            }
        }
    }

    // Abrir detalle por ID; si no existe en lista → fetch “en caliente”
    LaunchedEffect(initialFocusId, events) {
        val targetId = initialFocusId ?: return@LaunchedEffect
        val existing = events.firstOrNull { it.id == targetId } ?: externalFocusEvent?.takeIf { it.id == targetId }
        if (existing != null) {
            selectedEvent = existing
            scope.launch { cameraPositionState.centerOnLocation(existing.latitude, existing.longitude, zoom = 16f) }
        } else {
            try {
                val snap = firestore.collection("events").document(targetId).get().await()
                val fetched = snap.toObject(Event::class.java)?.copy(id = snap.id)
                if (fetched != null) {
                    externalFocusEvent = fetched
                    selectedEvent = fetched
                    scope.launch { cameraPositionState.centerOnLocation(fetched.latitude, fetched.longitude, zoom = 16f) }
                }
            } catch (_: Exception) {
                // silenciar
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Radar de Eventos") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                    TextButton(onClick = { mapSettingsVM.toggleDarkMap() }) {
                        Text(if (darkMap) "Mapa claro" else "Mapa oscuro")
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Routes.AUTH) { popUpTo(0) { inclusive = true } }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = {
                        val (lat, lon) = userLocation ?: return@SmallFloatingActionButton
                        scope.launch { cameraPositionState.centerOnLocation(lat, lon) }
                    }
                ) {
                    Icon(Icons.Outlined.CenterFocusStrong, contentDescription = "Centrar mapa")
                }

                Spacer(Modifier.height(12.dp))

                // ✅ NUEVO: Botón para alternar entre Mapa y Lista
                SmallFloatingActionButton(
                    onClick = { showListView = !showListView }
                ) {
                    Icon(
                        imageVector = if (showListView) Icons.Filled.Close else Icons.AutoMirrored.Filled.List,
                        contentDescription = if (showListView) "Cerrar lista" else "Ver lista"
                    )
                }

                Spacer(Modifier.height(12.dp))



                RadarFab(
                    navController = navController,
                    expanded = fabExpanded,
                    onExpandChanged = { fabExpanded = it },
                    userLocation = userLocation,
                    cameraPositionState = cameraPositionState,
                    scope = scope
                )


                Spacer(Modifier.height(12.dp))


                FloatingActionButton(
                    onClick = { navController.navigate(Routes.MY_EVENTS) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Mis eventos")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets.systemBars
    ) { padding ->

        val mapTopPadding = 120.dp   // buscador + chips
        val mapBottomPadding = 160.dp // FABs + peek

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ------------- MAPA -------------
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = remember(hasLocationPermission, darkMap) {
                    MapProperties(
                        isMyLocationEnabled = hasLocationPermission,
                        mapStyleOptions = if (darkMap)
                            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_dark)
                        else null
                    )
                },
                uiSettings = remember {
                    MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false,
                        compassEnabled = false
                    )
                },
                contentPadding = PaddingValues(
                    top = mapTopPadding,
                    bottom = mapBottomPadding
                )
            ) {
                // Clustering + listeners (DENTRO del bloque GoogleMap)
                MapEffect(mapEvents, showOnlyHighlightedEventsFilter, sortBy, interestedIds) { googleMap ->
                    val clusterManager = ClusterManager<EventClusterItem>(context, googleMap).apply {
                        renderer = EventRenderer(
                            context = context,
                            map = googleMap,
                            clusterManager = this,
                            getInterestedIds = { interestedIds }
                        )
                        setOnClusterItemClickListener { item ->
                            selectedEvent = item.event
                            true
                        }
                        setOnClusterClickListener { cluster: Cluster<EventClusterItem> ->
                            val items = cluster.items.map { it.event }
                            val interestedCount = items.count { it.id.isNotBlank() && interestedIds.contains(it.id) }
                            clusterPreview = ClusterPreviewState(
                                position = cluster.position,
                                total = items.size,
                                interested = interestedCount,
                                items = items
                            )
                            true
                        }
                    }

                    googleMap.setOnCameraIdleListener(clusterManager)
                    googleMap.setOnMarkerClickListener(clusterManager)

                    clusterManager.clearItems()
                    val filtered = mapEvents.filter {
                        if (showOnlyHighlightedEventsFilter) it.adOption == "HIGHLIGHTED" else true
                    }
                    val items = filtered.map { e ->
                        EventClusterItem(
                            position = LatLng(e.latitude, e.longitude),
                            title = e.title,
                            snippet = e.description,
                            isHighlighted = e.adOption == "HIGHLIGHTED",
                            event = e
                        )
                    }
                    clusterManager.addItems(items)
                    clusterManager.cluster()
                }
            }

            AnimatedVisibility(
                visible = showListView,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .zIndex(6f)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                        .navigationBarsPadding()
                ) {
                    // 🔺 Header con botón cerrar
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Eventos cerca",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showListView = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                        }
                    }

                    // 🔽 Filtros y orden
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = showOnlyHighlightedEventsFilter,
                            onClick = { showOnlyHighlightedEventsFilter = !showOnlyHighlightedEventsFilter },
                            label = { Text("Solo destacados ⭐") }
                        )
                        Spacer(Modifier.width(12.dp))
                        SegmentedButtons(sortBy = sortBy, onChangeSort = { sortBy = it })
                    }

                    Spacer(Modifier.height(8.dp))

                    // 📜 Lista de eventos
                    val sortedEvents = remember(events, sortBy, cameraPositionState.position.target) {
                        val base = events.filter {
                            if (showOnlyHighlightedEventsFilter) it.adOption == "HIGHLIGHTED" else true
                        }
                        sortEvents(base, cameraPositionState.position.target, sortBy)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(sortedEvents, key = { it.id.ifBlank { it.hashCode().toString() } }) { event ->

                            val dist = distanceKm(
                                cameraPositionState.position.target.latitude,
                                cameraPositionState.position.target.longitude,
                                event.latitude, event.longitude
                            )

                            val interested = event.id.isNotBlank() && interestedIds.contains(event.id)
                            EventRowCard(
                                event = event,
                                distKm = dist,
                                interested = interested,
                                onToggleInterest = { scope.launch { interestsVM.toggleInterest(event.id) } },
                                onClick = {
                                    scope.launch {
                                        cameraPositionState.centerOnLocation(event.latitude, event.longitude, zoom = 15f)
                                        showListView = false
                                        selectedEvent = event
                                    }
                                }
                            )
                        }
                    }
                }
            }


            // scrim superior para legibilidad
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(mapTopPadding)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            1f to Color.Transparent
                        )
                    )
            )

            // buscador + chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .zIndex(2f)
            ) {
                MapSearchBar(hint = "Buscar lugares o eventos...") { selectedLatLng, _ ->
                    selectedLatLng?.let {
                        scope.launch { cameraPositionState.centerOnLocation(it.latitude, it.longitude) }
                        viewModel.loadEventsNear(it.latitude, it.longitude, context)
                    }
                }

                EventCategoryFilterChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) },
                    showOnlyHighlighted = showOnlyHighlightedEventsFilter,
                    onHighlightedToggle = { showOnlyHighlightedEventsFilter = it }
                )

                // 👇 Carrusel de destacados
                FeaturedStoriesRow(
                    events = events,
                    onSelect = { e ->
                        scope.launch { cameraPositionState.centerOnLocation(e.latitude, e.longitude, zoom = 15f) }
                        selectedEvent = e
                    }
                )
            }

            // Aviso si no hay permisos
            val showPermCard = !hasLocationPermission
            if (showPermCard) {
                ElevatedCard(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 90.dp, start = 16.dp, end = 16.dp)
                        .zIndex(3f)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Activa tu ubicación para centrar el mapa y ver eventos cercanos.")
                        Spacer(Modifier.width(12.dp))
                        TextButton(onClick = { locationPerms.launchMultiplePermissionRequest() }) {
                            Text("Permitir")
                        }
                    }
                }
            }

            // Sonar localizado
            selectedEvent?.let {
                Box(Modifier.align(Alignment.Center).zIndex(3f)) {
                    SonarMultiWaves(color = Color.Cyan)
                }
            }

            // Radar global
            if (isAlertActive) RadarUltraMulti()

            // ---------- Popover del clúster ----------
            clusterPreview?.let { preview ->
                ClusterPreviewBar(
                    preview = preview,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 160.dp)
                        .zIndex(4f),
                    onClose = { clusterPreview = null },
                    onZoom = {
                        clusterPreview = null
                        scope.launch {
                            val current = cameraPositionState.position.zoom
                            cameraPositionState.centerOnLocation(
                                preview.position.latitude,
                                preview.position.longitude,
                                zoom = current + 1.5f
                            )
                        }
                    },
                    onShowList = {
                        listFromCluster = preview.items
                        clusterPreview = null
                        showListSheet = true
                    }
                )
            }

            // ---------- Peek “Eventos cerca” ----------
            if (events.isNotEmpty()) {
                EventPeekHandle(
                    count = events.size,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp)
                        .zIndex(3f),
                    onClick = {
                        showListSheet = true
                        scope.launch { userPrefs.setShowListScreen(true) }
                    }

                )
            }

            // Detalle de evento
            selectedEvent?.let { event ->
                // Estado de interés del usuario para este evento
                val interested = event.id.isNotBlank() && interestedIds.contains(event.id)

                EventDetailBottomSheet(
                    event = event,
                    bottomSheetState = detailSheetState,
                    onDismiss = {
                        selectedEvent = null
                        // Si venía de externo, puedes limpiar si quieres:
                        if (externalFocusEvent?.id == event.id) externalFocusEvent = null
                    },
                    interested = interested,
                    onToggleInterest = {
                        scope.launch { interestsVM.toggleInterest(event.id) }
                    }
                )
            }
        }
    }
}

/* ------------------- CLUSTERING ------------------- */

private data class EventClusterItem(
    private val position: LatLng,
    private val title: String,
    private val snippet: String,
    val isHighlighted: Boolean,
    val event: Event
) : ClusterItem {
    override fun getPosition(): LatLng = position
    override fun getTitle(): String = if (isHighlighted) "⭐ $title" else title
    override fun getSnippet(): String = snippet
    override fun getZIndex(): Float? = if (isHighlighted) 1f else 0f
}

private class EventRenderer(
    private val context: Context,
    map: com.google.android.gms.maps.GoogleMap,
    clusterManager: ClusterManager<EventClusterItem>,
    private val getInterestedIds: () -> Set<String>
) : DefaultClusterRenderer<EventClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: EventClusterItem, markerOptions: MarkerOptions) {
        val interested = item.event.id.isNotBlank() && getInterestedIds().contains(item.event.id)
        val bmp = createMarkerBitmap(
            context = context,
            highlighted = item.isHighlighted,
            interested = interested
        )
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp))
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<EventClusterItem>, markerOptions: MarkerOptions) {
        val total = cluster.size
        val interestedCount = cluster.items.count { it.event.id.isNotBlank() && getInterestedIds().contains(it.event.id) }
        val bmp = createClusterBitmap(context, total, interestedCount)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp))
        super.onBeforeClusterRendered(cluster, markerOptions)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<EventClusterItem>): Boolean {
        return cluster.size >= 3
    }
}

/* ------------------- DIBUJO DE ICONOS (BADGE & CLUSTER) ------------------- */

private fun createMarkerBitmap(
    context: Context,
    highlighted: Boolean,
    interested: Boolean
): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (40 * density).toInt().coerceAtLeast(40)
    val badgeSize = (14 * density).toInt()
    val stroke = (2 * density).toFloat()

    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Fondo circular
    paint.style = Paint.Style.FILL
    paint.color = if (highlighted)
        android.graphics.Color.parseColor("#FFD54F")
    else
        android.graphics.Color.parseColor("#EF5350")
    c.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)

    // Borde
    paint.style = Paint.Style.STROKE
    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = stroke
    c.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)

    // Badge ❤️ si interesado
    if (interested) {
        val cx = size - badgeSize / 2f
        val cy = badgeSize / 2f
        // fondo badge
        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.WHITE
        c.drawCircle(cx, cy, badgeSize / 2f, paint)
        // corazón
        paint.color = android.graphics.Color.parseColor("#E91E63")
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = badgeSize * 0.7f
        c.drawText("❤", cx, cy + (badgeSize * 0.28f), paint)
    }
    return bmp
}

private fun createClusterBitmap(
    context: Context,
    count: Int,
    interestedCount: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (48 * density).toInt().coerceAtLeast(48)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Base círculo
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#3949AB") // Indigo
    c.drawCircle(size / 2f, size / 2f, size / 2.0f, paint)

    // Borde
    paint.style = Paint.Style.STROKE
    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = (2 * density)
    c.drawCircle(size / 2f, size / 2f, size / 2.0f, paint)

    // Texto del conteo
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textSize = (16 * density)
    val text = count.toString()
    val bounds = Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    c.drawText(text, size / 2f, size / 2f + bounds.height() / 2f, paint)

    // Badge ❤️ con número de interesados (si > 0)
    if (interestedCount > 0) {
        val badgeW = (22 * density)
        val badgeH = (14 * density)
        val rect = RectF(
            size - badgeW - (4 * density),
            size - badgeH - (4 * density),
            size - (4 * density),
            size - (4 * density)
        )
        // fondo pill
        paint.color = android.graphics.Color.WHITE
        c.drawRoundRect(rect, badgeH / 2, badgeH / 2, paint)
        // corazón + número
        paint.color = android.graphics.Color.parseColor("#E91E63")
        paint.textSize = (11 * density)
        val txt = "❤ $interestedCount"
        val textBounds = Rect()
        paint.getTextBounds(txt, 0, txt.length, textBounds)
        val tx = rect.centerX()
        val ty = rect.centerY() + textBounds.height() / 2f
        c.drawText(txt, tx, ty, paint)
    }

    return bmp
}

private enum class SortBy {
    DISTANCE,
    NAME,
    DATE_DESC,
    DATE_ASC,
    POPULARITY
}


@Composable
private fun SegmentedButtons(
    sortBy: SortBy,
    onChangeSort: (SortBy) -> Unit
) {
    Row {
        AssistChip(
            onClick = { onChangeSort(SortBy.DISTANCE) },
            label = { Text("Distancia") },
            enabled = sortBy != SortBy.DISTANCE
        )
        Spacer(Modifier.width(8.dp))
        AssistChip(
            onClick = { onChangeSort(SortBy.NAME) },
            label = { Text("Nombre") },
            enabled = sortBy != SortBy.NAME
        )
        Spacer(Modifier.width(8.dp))
        val isDate = sortBy == SortBy.DATE_DESC || sortBy == SortBy.DATE_ASC
        val arrow = when (sortBy) {
            SortBy.DATE_DESC -> "▼"
            SortBy.DATE_ASC -> "▲"
            else -> ""
        }
        AssistChip(
            onClick = {
                onChangeSort(
                    when (sortBy) {
                        SortBy.DATE_DESC -> SortBy.DATE_ASC
                        SortBy.DATE_ASC -> SortBy.DATE_DESC
                        else -> SortBy.DATE_DESC
                    }
                )
            },
            label = { Text("Fecha $arrow") },
            enabled = !isDate
        )
    }
}

@Composable
private fun EventRowCard(
    event: Event,
    distKm: Double,
    interested: Boolean,
    onToggleInterest: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val km = (distKm * 10).roundToInt() / 10.0
    val pulse by animateFloatAsState(
        targetValue = if (interested) 1.15f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "heartPulse"
    )

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = onClick, label = { Text("$km km") })

                // ❤️ Contador de interesados (si hay)
                if (event.interestedCount?.takeIf { it > 0 } != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text("❤ ${event.interestedCount}") },
                        enabled = false
                    )
                }

                IconToggleButton(
                    checked = interested,
                    onCheckedChange = { onToggleInterest() }
                ) {
                    if (interested) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Quitar de interesados",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.graphicsLayer(scaleX = pulse, scaleY = pulse)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = "Me interesa",
                            modifier = Modifier.graphicsLayer(scaleX = pulse, scaleY = pulse)
                        )
                    }
                }
            }


            Spacer(Modifier.height(6.dp))
            Text(
                formatEventDate(event.timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if ((event.interestedCount ?: 0) > 0) {
                Text(
                    text = "❤ ${event.interestedCount} interesados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(event.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { openDirections(context, event.latitude, event.longitude, event.title) }
                ) { Text("Cómo llegar") }
            }
        }
    }
}

/* ------------------- UI Peek handle ------------------- */

@Composable
private fun EventPeekHandle(
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shimmer by rememberInfiniteTransition(label = "peek")
        .animateFloat(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
            label = "alpha"
        )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    ) {
        Column(
            Modifier
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .widthIn(min = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Eventos cerca ($count)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = shimmer)
            )
        }
    }
}

/* ------------------- Cluster preview bar ------------------- */

private data class ClusterPreviewState(
    val position: LatLng,
    val total: Int,
    val interested: Int,
    val items: List<Event>
)

@Composable
private fun ClusterPreviewBar(
    preview: ClusterPreviewState,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onZoom: () -> Unit,
    onShowList: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp)),
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "📍 Zona con ${preview.total} eventos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (preview.interested > 0) {
                    Text(
                        text = "❤ ${preview.interested} interesados aquí",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            TextButton(onClick = onShowList) { Text("Ver (${preview.total})") }
            Spacer(Modifier.width(6.dp))
            FilledTonalButton(onClick = onZoom) { Text("Acercar") }
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar")
            }
        }
    }
}

/* ------------------- UTILS ------------------- */

private fun vibrateDevice(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
}

@Composable
fun RadarUltraMulti() {
    Box(Modifier.fillMaxSize().zIndex(5f), contentAlignment = Alignment.Center) {
        repeat(3) { i -> SingleRadarWave(delay = i * 400, color = Color(0xFF00FFAA)) }
    }
}

@Composable
fun SonarMultiWaves(color: Color) {
    Box(contentAlignment = Alignment.Center) {
        repeat(3) { i -> SingleRadarWave(delay = i * 300, color = color, size = 180.dp) }
    }
}

@Composable
fun SingleRadarWave(delay: Int, color: Color, size: Dp = 250.dp) {
    val transition = rememberInfiniteTransition(label = "wave$delay")
    val scale by transition.animateFloat(
        initialValue = 0.5f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = delay), RepeatMode.Restart),
        label = "scaleAnim$delay"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = delay), RepeatMode.Restart),
        label = "alphaAnim$delay"
    )
    Box(
        Modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun FeaturedStoriesRow(
    events: List<Event>,
    onSelect: (Event) -> Unit
) {
    val featured = remember(events) {
        events.filter {
            it.adOption.equals("HIGHLIGHTED", true) ||
                    it.adOption.equals("PREMIUM", true) ||
                    it.adOption.equals("FEATURED", true)
        }
    }
    if (featured.isEmpty()) return

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(featured, key = { it.id.ifBlank { it.hashCode().toString() } }) { e ->
            FeaturedStoryCard(event = e, onClick = { onSelect(e) })
        }
    }
}

@Composable
private fun FeaturedStoryCard(
    event: Event,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 80.dp)
            .clickable(onClick = onClick)
    ) {
        // marco circular con imagen
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (!event.imageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // fallback
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = event.title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}

suspend fun CameraPositionState.centerOnLocation(lat: Double, lon: Double, zoom: Float = 14f) {
    animate(update = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom), durationMs = 1000)
}

private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val results = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0] / 1000.0
}

private fun formatEventDate(ts: Long): String {
    val now = Instant.now()
    val inst = Instant.ofEpochMilli(ts)
    val days = ChronoUnit.DAYS.between(
        now.truncatedTo(ChronoUnit.DAYS),
        inst.truncatedTo(ChronoUnit.DAYS)
    )
    val dateStr = DateTimeFormatter.ofPattern("EEE d MMM • HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(inst)

    return when {
        days == 0L -> "Hoy • $dateStr"
        days == 1L -> "Mañana • $dateStr"
        days in 2..6 -> "Esta semana • $dateStr"
        days < 0 -> "Pasado • $dateStr"
        else -> dateStr
    }
}

private fun sortEvents(
    events: List<Event>,
    center: LatLng,
    sortBy: SortBy

): List<Event> {
    return when (sortBy) {
        SortBy.DISTANCE -> events.sortedBy {
            distanceKm(center.latitude, center.longitude, it.latitude, it.longitude)
        }
        SortBy.NAME -> events.sortedBy { it.title.lowercase() }
        SortBy.DATE_DESC -> events.sortedByDescending { it.timestamp }
        SortBy.DATE_ASC -> events.sortedBy { it.timestamp }
        SortBy.POPULARITY -> events.sortedByDescending { it.interestedCount ?: 0 }
    }
}

fun openDirections(context: Context, lat: Double, lon: Double, label: String = "Evento") {
    val gmm = Uri.parse("google.navigation:q=$lat,$lon&mode=d")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmm).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(mapIntent)
    } catch (_: ActivityNotFoundException) {
        val uri = Uri.parse("https://maps.google.com/?q=$lat,$lon($label)")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir mapas", Toast.LENGTH_SHORT).show()
        }
    }
}
