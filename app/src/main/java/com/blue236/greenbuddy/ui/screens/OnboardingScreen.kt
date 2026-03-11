package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.ui.components.StatCard
import com.blue236.greenbuddy.ui.components.StarterPlantCard

@Composable
fun OnboardingScreen(
    options: List<StarterPlantOption>,
    selectedStarterId: String,
    currentLessonTitle: String,
    onSelectStarter: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val selectedOption = options.first { it.id == selectedStarterId }
    val personality = CompanionPersonalitySystem.personalityFor(selectedOption.companion.species)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("Welcome to GreenBuddy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Build your first plant companion in under a minute. Pick a starter whose voice fits your vibe, then begin the daily care + learning loop.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How it works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("1. Choose a starter plant")
                Text("2. Learn one short care concept each day")
                Text("3. Keep your companion happy and help it grow")
            }
        }

        Text("Choose your starter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                StarterPlantCard(
                    option = option,
                    selected = option.id == selectedStarterId,
                    onClick = { onSelectStarter(option.id) },
                )
            }
        }

        StatCard("Your current pick") {
            Text("${selectedOption.companion.name} · ${selectedOption.title}", fontWeight = FontWeight.SemiBold)
            Text(selectedOption.subtitle)
            Text("Personality: ${personality.archetype}")
            Text("Tone: ${personality.tone}")
            Text("First lesson: $currentLessonTitle")
        }

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Start with ${selectedOption.title}")
        }
    }
}
