package com.carlitoswy.flashmeet.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carlitoswy.flashmeet.domain.model.Flyer

@Composable
fun EventSearchScreen(
    vm: EventSearchViewModel = hiltViewModel()
) {
    val loading by vm.loading.collectAsState()
    val items by vm.items.collectAsState()
    val text by vm.text.collectAsState()
    val radius by vm.radiusKm.collectAsState()
    val promoted by vm.promotedOnly.collectAsState()

    LaunchedEffect(Unit) { vm.searchAroundMe() }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🔎 Buscar cerca de mí", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = text,
            onValueChange = { vm.text.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Texto / ciudad") },
            singleLine = true
        )
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Radio: ${radius.toInt()} km")
            Slider(
                value = radius.toFloat(),
                onValueChange = { vm.radiusKm.value = it.toDouble() },
                valueRange = 1f..200f,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = promoted,
                onClick = { vm.promotedOnly.value = !promoted },
                label = { Text("Solo promovidos") }
            )
            Button(onClick = { vm.searchAroundMe() }) { Text("Buscar") }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { f -> FlyerRow(f) }
            }
        }
    }
}

@Composable
private fun FlyerRow(f: Flyer) {
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(f.title, style = MaterialTheme.typography.titleMedium)
            Text(f.city ?: "Sin ciudad")
            Text("Tipo: ${f.adOption}")
        }
    }
}
