package com.carlitoswy.flashmeet.presentation.myevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyEventsScreen(
    vm: MyEventsViewModel = hiltViewModel() // ✅ ViewModel correcto
) {
    val myItems by vm.state.collectAsState()   // ✅ nombre distinto a LazyListScope.items
    val loading by vm.loading.collectAsState()

    Box(Modifier.fillMaxSize()) {
        if (loading) {
            Column(Modifier.align(Alignment.Center)) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Cargando tus eventos…")
            }
        } else {
            if (myItems.isEmpty()) {
                Column(Modifier.align(Alignment.Center)) {
                    Text("Aún no creaste eventos")
                    Spacer(Modifier.height(8.dp))
                    Text("¡Crea uno y promuévelo con FlashMeet! 🚀")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myItems) { e ->
                        EventCard(e)
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(e: MyEventUI) {
    val fmt = remember { SimpleDateFormat("dd MMM, yyyy - HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    e.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (e.promoted) AssistChip(onClick = {}, label = { Text("Promocionado") })
            }
            Text("${e.city} • ${fmt.format(Date(e.dateMillis))}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { /* TODO editar */ }) { Text("Editar") }
                TextButton(onClick = { /* TODO compartir deeplink */ }) { Text("Compartir") }
            }
        }
    }
}
