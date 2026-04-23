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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.localizedTitle
import com.blue236.greenbuddy.ui.components.GreenBuddyButton
import com.blue236.greenbuddy.ui.components.GreenBuddyCard
import com.blue236.greenbuddy.ui.components.GreenBuddyHeroCard
import com.blue236.greenbuddy.ui.components.SectionTitle
import com.blue236.greenbuddy.ui.components.StarterPlantCard

@Composable
fun OnboardingScreen(
    options: List<StarterPlantOption>,
    selectedStarterId: String,
    currentLessonTitle: String,
    onSelectStarter: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val selectedOption = options.first { it.id == selectedStarterId }
    val personality = CompanionPersonalitySystem.personalityFor(selectedOption.companion.species, localeTag)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        // Welcome hero card
        GreenBuddyHeroCard(modifier = Modifier.fillMaxWidth()) {
            Text("🌿", style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // How it works card
        GreenBuddyCard(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            Text(
                stringResource(R.string.how_it_works),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(stringResource(R.string.onboarding_step_1), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.onboarding_step_2), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.onboarding_step_3), style = MaterialTheme.typography.bodyMedium)
        }

        // Starter selection
        SectionTitle(stringResource(R.string.choose_your_starter))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                StarterPlantCard(option, option.id == selectedStarterId) { onSelectStarter(option.id) }
            }
        }

        // Current pick preview
        GreenBuddyCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
            Text(
                stringResource(R.string.current_pick),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "${selectedOption.companion.name} · ${selectedOption.localizedTitle(localeTag)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.personality_label, personality.archetype),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.first_lesson_label, currentLessonTitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // CTA button — full width, rounded
        GreenBuddyButton(
            onClick = onContinue,
            text = stringResource(R.string.start_with_starter, selectedOption.localizedTitle(localeTag)),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))
    }
}
