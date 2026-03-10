package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.model.StarterPlantOption

@Composable
fun DexScreen(
    modifier: Modifier = Modifier,
    options: List<StarterPlantOption>,
    selectedStarterId: String,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("PlantDex", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        options.forEach { option ->
            val prefix = if (option.id == selectedStarterId) "★" else "○"
            Card { Text("$prefix ${option.title} · ${option.subtitle}", modifier = Modifier.padding(16.dp)) }
        }
    }
}
