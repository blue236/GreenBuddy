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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.localizedTitle
import com.blue236.greenbuddy.ui.components.StatCard
import com.blue236.greenbuddy.ui.components.StarterPlantCard

@Composable
fun OnboardingScreen(options: List<StarterPlantOption>, selectedStarterId: String, currentLessonTitle: String, onSelectStarter: (String) -> Unit, onContinue: () -> Unit) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val selectedOption = options.first { it.id == selectedStarterId }
    val personality = CompanionPersonalitySystem.personalityFor(selectedOption.companion.species, localeTag)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.onboarding_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.how_it_works), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.onboarding_step_1)); Text(stringResource(R.string.onboarding_step_2)); Text(stringResource(R.string.onboarding_step_3))
            }
        }
        Text(stringResource(R.string.choose_your_starter), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { options.forEach { StarterPlantCard(it, it.id == selectedStarterId) { onSelectStarter(it.id) } } }
        StatCard(stringResource(R.string.current_pick)) {
            Text("${selectedOption.companion.name} · ${selectedOption.localizedTitle(localeTag)}", fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.personality_label, personality.archetype))
            Text(stringResource(R.string.tone_label, personality.tone))
            Text(stringResource(R.string.first_lesson_label, currentLessonTitle))
        }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.start_with_starter, selectedOption.localizedTitle(localeTag))) }
    }
}
