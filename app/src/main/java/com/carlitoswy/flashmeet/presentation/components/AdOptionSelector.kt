package com.carlitoswy.flashmeet.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carlitoswy.flashmeet.domain.model.AdOption

@Composable
fun AdOptionSelector(
    selectedOption: AdOption,
    onOptionSelected: (AdOption) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Nivel de promoción", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        AdOption.entries.forEach { option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onOptionSelected(option) },
                border = if (option == selectedOption)
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else
                    BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(option.displayName, style = MaterialTheme.typography.titleSmall)
                    Text(option.description, style = MaterialTheme.typography.bodySmall)
                    Text(option.displayPrice(), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
        }
    }
}
