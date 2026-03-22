package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.PlantInventoryEntry
import com.blue236.greenbuddy.model.localizedGrowthTitle
import com.blue236.greenbuddy.model.localizedHealth
import com.blue236.greenbuddy.model.localizedMood
import com.blue236.greenbuddy.model.localizedRequirementSummary
import com.blue236.greenbuddy.model.localizedSubtitle
import com.blue236.greenbuddy.model.localizedTitle
import com.blue236.greenbuddy.model.resolveGrowthStageState
import com.blue236.greenbuddy.model.unlockRequirementFor

@Composable
fun DexScreen(modifier: Modifier = Modifier, entries: List<PlantInventoryEntry>, ownedStarterIds: Set<String>, onSelectStarter: (String) -> Unit) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val ownedCount = entries.count { it.isOwned }
    val totalCount = entries.size
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.greenhouse_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.greenhouse_subtitle, ownedCount, totalCount), color = MaterialTheme.colorScheme.onSurfaceVariant)
        entries.forEach { entry ->
            val option = entry.option
            val growthStageState = resolveGrowthStageState(option.id, entry.progress, entry.careState)
            val cardColor = when {
                entry.isActive -> Color(0xFFE8F5E9)
                entry.isOwned -> MaterialTheme.colorScheme.surfaceVariant
                else -> Color(0xFFF5F5F5)
            }
            Card(modifier = Modifier.fillMaxWidth().clickable(enabled = entry.isOwned) { onSelectStarter(option.id) }, colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(64.dp).background(Color(0xFFC8E6C9), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text(option.previewEmoji, style = MaterialTheme.typography.headlineMedium) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(option.localizedTitle(localeTag), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(option.localizedSubtitle(localeTag), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (entry.isOwned) {
                            Text(
                                stringResource(
                                    R.string.greenhouse_growth_summary,
                                    growthStageState.currentStage.emoji,
                                    growthStageState.currentStage.localizedGrowthTitle(localeTag),
                                    growthStageState.readinessPercent,
                                ),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            LinearProgressIndicator(
                                progress = { growthStageState.progressToNextStage },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                growthStageState.localizedRequirementSummary(localeTag),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(stringResource(R.string.inventory_stats, entry.progress.totalXp, entry.careState.localizedHealth(localeTag), entry.careState.localizedMood(localeTag)))
                            Text(
                                when {
                                    entry.isActive -> stringResource(R.string.greenhouse_active_hint)
                                    else -> stringResource(R.string.greenhouse_owned_hint)
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(unlockRequirementFor(option, ownedStarterIds, localeTag), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        when {
                            entry.isActive -> stringResource(R.string.active)
                            entry.isOwned -> stringResource(R.string.switch_action)
                            else -> stringResource(R.string.locked)
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
