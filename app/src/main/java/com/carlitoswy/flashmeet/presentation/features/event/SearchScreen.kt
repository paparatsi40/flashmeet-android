package com.carlitoswy.flashmeet.presentation.event

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.carlitoswy.flashmeet.domain.model.Event
import com.carlitoswy.flashmeet.ui.navigation.Routes
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val country = remember { mutableStateOf("") }
    val city = remember { mutableStateOf("") }
    val postal = remember { mutableStateOf("") }

    // Nuevos filtros
    val categories = listOf("Todos", "Música", "Deporte", "Arte", "Tecnología")
    var selectedCategory by remember { mutableStateOf("Todos") }
    var expandedCategory by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf("") }

    // DatePickerDialog
    val datePicker = DatePickerDialog(
        context, { _, year, month, day ->
            calendar.set(year, month, day)
            selectedDate = dateFormat.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Buscar eventos") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = country.value,
                onValueChange = { country.value = it },
                label = { Text("País") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = city.value,
                onValueChange = { city.value = it },
                label = { Text("Ciudad") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = postal.value,
                onValueChange = { postal.value = it },
                label = { Text("Código Postal") },
                modifier = Modifier.fillMaxWidth()
            )

            // Dropdown para categoría
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Categoría") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // Botón para seleccionar fecha
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("Fecha (YYYY-MM-DD)") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePicker.show() }
            )

            Button(
                onClick = {
                    viewModel.search(
                        country.value.trim(),
                        city.value.trim(),
                        postal.value.trim(),
                        if (selectedCategory != "Todos") selectedCategory else "",
                        selectedDate
                    )
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Buscar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { event ->
                    SearchResultCard(event) {
                        navController.navigate("${Routes.CREATE_EVENT}/${event.id}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text(event.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Ubicación: ${event.locationName}", style = MaterialTheme.typography.labelSmall)
            Text("Publicidad: ${event.adOption}", style = MaterialTheme.typography.labelSmall)
            if (event.timestamp > 0) {
                Text(
                    "Fecha: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(event.timestamp))}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
