package com.carlitoswy.flashmeet.presentation.search

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlitoswy.flashmeet.presentation.EventItem

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val TAG = "SearchScreen"

    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postal by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🔍 Buscar Eventos", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(8.dp))

        // Campos de búsqueda
        OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("País") })
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Ciudad") })
        OutlinedTextField(value = postal, onValueChange = { postal = it }, label = { Text("Código Postal") })
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") })
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha (yyyy-MM-dd)") })

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            Log.d(TAG, "🚀 Botón buscar pulsado con parámetros: country=$country | city=$city | postal=$postal | category=$category | date=$date")
            viewModel.searchEvents(country, city, postal, category, date)
        }) {
            Text("Buscar")
        }

        Spacer(Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(searchResults) { event ->
                    EventItem(event = event, onClick = {
                        Log.d(TAG, "👉 Evento clicado: ${event.id} - ${event.title}")
                        // Aquí podrías navegar al detalle
                        // navController.navigate("${Routes.EVENT_DETAIL}/${event.id}")
                    })
                }
            }
        }
    }
}
