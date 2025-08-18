package com.carlitoswy.flashmeet.presentation.flyer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun FlyerListScreen(navController: NavHostController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Aquí se mostrarán los flyers cercanos")
        // Aquí irán los flyers con lógica futura
    }
}
