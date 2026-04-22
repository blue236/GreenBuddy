package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.blue236.greenbuddy.model.PlantInventoryEntry
import com.blue236.greenbuddy.model.activeInventoryEntry
import com.blue236.greenbuddy.model.localizedGrowthTitle
import com.blue236.greenbuddy.model.localizedTitle
import com.blue236.greenbuddy.model.nextUnlockableStarterId
import com.blue236.greenbuddy.model.resolveGrowthStageState
import com.blue236.greenbuddy.ui.components.GreenBuddyHeroCard
import com.blue236.greenbuddy.ui.components.PlantInventoryCard

@Composable
fun DexScreen(
    modifier: Modifier = Modifier,
    entries: List<PlantInventoryEntry>,
    ownedStarterIds: Set<String>,
    onSelectStarter: (String) -> Unit,
) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val ownedCount = entries.count { it.isOwned }
    val totalCount = entries.size
    val activeEntry = activeInventoryEntry(entries)
    val nextUnlockOption = nextUnlockableStarterId(ownedStarterIds)?.let { nextId ->
        entries.firstOrNull { it.option.id == nextId }?.option
    }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.greenhouse_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.greenhouse_subtitle, ownedCount, totalCount),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Greenhouse summary hero card
        GreenBuddyHeroCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.greenhouse_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            activeEntry?.let { active ->
                val growthStageState = resolveGrowthStageState(active.option.id, active.progress, active.careState)
                Text(
                    stringResource(
                        R.string.greenhouse_summary_active_value,
                        active.option.previewEmoji,
                        active.option.localizedTitle(localeTag),
                        growthStageState.currentStage.localizedGrowthTitle(localeTag),
                        growthStageState.readinessPercent,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                if (nextUnlockOption != null) {
                    stringResource(
                        R.string.greenhouse_summary_next_unlock_value,
                        nextUnlockOption.previewEmoji,
                        nextUnlockOption.localizedTitle(localeTag),
                    )
                } else {
                    stringResource(R.string.greenhouse_summary_all_unlocked)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Collection dots
            val collectionDots = (1..totalCount).joinToString(" ") { index ->
                if (index <= ownedCount) "●" else "○"
            }
            Text(
                stringResource(R.string.greenhouse_summary_momentum_value, ownedCount, totalCount) + "  $collectionDots",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            stringResource(R.string.greenhouse_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp),
        )

        entries.forEach { entry ->
            PlantInventoryCard(
                entry = entry,
                ownedStarterIds = ownedStarterIds,
                localeTag = localeTag,
                onSelect = onSelectStarter,
            )
        }
    }
}
