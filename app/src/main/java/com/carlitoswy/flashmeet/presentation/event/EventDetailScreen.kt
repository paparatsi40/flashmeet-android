package com.carlitoswy.flashmeet.presentation.event

import com.carlitoswy.flashmeet.presentation.event.EventDetailViewModel
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.carlitoswy.flashmeet.core.deeplink.DeeplinkBuilder
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.domain.model.color
import com.carlitoswy.flashmeet.domain.model.icon
import com.carlitoswy.flashmeet.domain.model.toCategoryEnum
import com.carlitoswy.flashmeet.presentation.components.FlyerPreview
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.carlitoswy.flashmeet.utils.dateFormatted
import com.carlitoswy.flashmeet.utils.navigateWithGoogleMaps
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(eventId) { viewModel.loadEventById(eventId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    uiState.event?.let { e ->
                        IconButton(
                            onClick = {
                                DeeplinkBuilder.shareEvent(
                                    context = context,
                                    id = e.id,
                                    host = DeeplinkBuilder.PROD_HOST,
                                    shortPath = true,
                                    lat = e.latitude,
                                    lon = e.longitude
                                )
                            }
                        ) { Icon(Icons.Default.Share, contentDescription = "Compartir") }

                        IconButton(
                            onClick = {
                                val url = DeeplinkBuilder.eventHttps(
                                    id = e.id,
                                    host = DeeplinkBuilder.PROD_HOST,
                                    shortPath = true,
                                    lat = e.latitude,
                                    lon = e.longitude
                                )
                                copyToClipboard(context, "FlashMeet", url)
                                scope.launch { snackbarHost.showSnackbar("Link copiado") }
                            }
                        ) { Icon(Icons.Default.ContentCopy, contentDescription = "Copiar link") }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHost) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.errorMessage}")
                }
                uiState.event != null -> EventDetailContent(
                    event = uiState.event!!,
                    navController = navController,
                    context = context,
                    onCopy = {
                        val url = DeeplinkBuilder.eventHttps(
                            id = uiState.event!!.id,
                            host = DeeplinkBuilder.PROD_HOST,
                            shortPath = true,
                            lat = uiState.event!!.latitude,
                            lon = uiState.event!!.longitude
                        )
                        copyToClipboard(context, "FlashMeet", url)
                        scope.launch { snackbarHost.showSnackbar("Link copiado") }
                    },
                    onShare = {
                        DeeplinkBuilder.shareEvent(
                            context = context,
                            id = uiState.event!!.id,
                            host = DeeplinkBuilder.PROD_HOST,
                            shortPath = true,
                            lat = uiState.event!!.latitude,
                            lon = uiState.event!!.longitude
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    navController: NavController,
    context: Context,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val categoryEnum = event.toCategoryEnum()
    val hasCustomFlyer = event.flyerTextColor != null || event.flyerBackgroundColor != null || event.flyerFontFamily != null

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (hasCustomFlyer) {
            FlyerPreview(
                title = event.title,
                description = event.description,
                imageUri = event.imageUrl?.let { Uri.parse(it) },
                textColor = (event.flyerTextColor ?: "#FFFFFF").toColor(),
                backgroundColor = (event.flyerBackgroundColor ?: "#000000").toColor(),
                fontFamily = event.flyerFontFamily ?: "SansSerif",
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        } else {
            event.imageUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(text = event.title, style = MaterialTheme.typography.headlineSmall)

        Row(verticalAlignment = Alignment.CenterVertically) {
            categoryEnum?.let {
                Icon(
                    imageVector = it.icon(),
                    contentDescription = it.displayName(),
                    tint = it.color(),
                    modifier = Modifier.size(20.dp).padding(end = 4.dp)
                )
            }
            Text(
                text = event.timestamp.dateFormatted(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (event.city.isNotEmpty() || event.country.isNotEmpty()) {
            Text(
                text = "${event.city}, ${event.country}".trim().trim(','),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Compartir")
                Spacer(Modifier.width(6.dp))
                Text("Compartir")
            }
            OutlinedButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar link")
                Spacer(Modifier.width(6.dp))
                Text("Copiar link")
            }
            OutlinedButton(onClick = { navigateWithGoogleMaps(context, event) }) {
                Icon(Icons.Default.MyLocation, contentDescription = "Navegar")
                Spacer(Modifier.width(6.dp))
                Text("Navegar")
            }
            OutlinedButton(onClick = { navController.navigate(Routes.editEventWithId(event.id)) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
                Spacer(Modifier.width(6.dp))
                Text("Editar")
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(ClipboardManager::class.java)
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun String.toColor(): Color = Color(android.graphics.Color.parseColor(this))
