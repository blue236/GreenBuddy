package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.AppLanguage
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.GrowthStageState
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardCatalog
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.WeatherCatalog
import com.blue236.greenbuddy.model.localizedDescription
import com.blue236.greenbuddy.model.localizedGrowthAccentLabel
import com.blue236.greenbuddy.model.localizedGrowthTitle
import com.blue236.greenbuddy.model.localizedName
import com.blue236.greenbuddy.model.localizedRequirementSummary
import com.blue236.greenbuddy.model.localizedUnlockHint
import com.blue236.greenbuddy.model.systemLanguageLabel
import com.blue236.greenbuddy.ui.components.StatCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier, starter: StarterPlantOption, progress: LessonProgress, dailyMissionSet: DailyMissionSet? = null, growthStageState: GrowthStageState, ownedPlantCount: Int, rewardState: RewardState, realPlantModeState: RealPlantModeState, selectedWeatherCityId: String, appLanguage: AppLanguage, onAcknowledgeGrowthStage: () -> Unit, onPurchaseCosmetic: (CosmeticItem) -> Unit, onEquipCosmetic: (String) -> Unit, onSetSelectedWeatherCity: (String) -> Unit, onSetAppLanguage: (AppLanguage) -> Unit) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val personality = CompanionPersonalitySystem.personalityFor(starter.companion.species, localeTag)
    val systemLocaleTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StatCard(stringResource(R.string.progress)) { Text(stringResource(R.string.xp_value, progress.totalXp)); Text(stringResource(R.string.leaf_tokens_value, rewardState.leafTokens)); Text(stringResource(R.string.plants_owned_value, ownedPlantCount)) }
        StatCard(stringResource(R.string.growth_status)) {
            Text(growthStageState.currentStage.localizedGrowthTitle(localeTag), fontWeight = FontWeight.SemiBold)
            Text(growthStageState.currentStage.localizedGrowthAccentLabel(localeTag), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (growthStageState.nextStage != null) Text(growthStageState.localizedRequirementSummary(localeTag))
            Text(growthStageState.localizedUnlockHint(localeTag), color = MaterialTheme.colorScheme.primary)
            if (growthStageState.newlyUnlocked) Button(onClick = onAcknowledgeGrowthStage) { Text(stringResource(R.string.celebrate_growth)) }
        }
        StatCard(stringResource(R.string.companion_personality)) { Text(personality.archetype); Text(personality.tone) }
        StatCard(stringResource(R.string.location_settings_title)) {
            Text(stringResource(R.string.location_settings_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WeatherCatalog.cityOptions.forEach { option ->
                    AssistChip(
                        onClick = { onSetSelectedWeatherCity(option.id) },
                        label = { Text((if (option.id == selectedWeatherCityId) "✓ " else "") + option.defaultName) },
                    )
                }
            }
        }
        StatCard(stringResource(R.string.language_settings)) {
            Text(stringResource(R.string.app_language), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.app_language_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(AppLanguage.SYSTEM, AppLanguage.ENGLISH, AppLanguage.GERMAN, AppLanguage.KOREAN).forEach { option ->
                    AssistChip(
                        onClick = { onSetAppLanguage(option) },
                        label = { Text((if (option == appLanguage) "✓ " else "") + systemLanguageLabel(option, systemLocaleTag, localeTag)) },
                    )
                }
            }
        }
        dailyMissionSet?.let { StatCard(stringResource(R.string.daily_streak)) { Text(stringResource(R.string.today_progress, it.completedCount, it.totalCount)); Text(stringResource(R.string.streak_value, it.currentStreak)) } }
        StatCard(stringResource(R.string.real_plant_habits)) { Text(if (realPlantModeState.enabled) stringResource(R.string.mode_enabled) else stringResource(R.string.mode_off)); Text(stringResource(R.string.logged_entries, realPlantModeState.entries.size)) }
        StatCard(stringResource(R.string.reward_shop)) { RewardCatalog.cosmetics.forEach { CosmeticShopRow(it, rewardState, localeTag, { onPurchaseCosmetic(it) }, { onEquipCosmetic(it.id) }) } }
    }
}

@Composable
private fun CosmeticShopRow(item: CosmeticItem, rewardState: RewardState, localeTag: String, onPurchase: () -> Unit, onEquip: () -> Unit) {
    val isUnlocked = item.id in rewardState.unlockedCosmeticIds
    val isEquipped = rewardState.equippedCosmeticId == item.id
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) { Text("${item.emoji} ${item.localizedName(localeTag)}", fontWeight = FontWeight.SemiBold); Text(item.localizedDescription(localeTag)) }
        when { isEquipped -> OutlinedButton(onClick = {}, enabled = false) { Text(stringResource(R.string.equipped)) }; isUnlocked -> Button(onClick = onEquip) { Text(stringResource(R.string.equip)) }; else -> Button(onClick = onPurchase, enabled = rewardState.leafTokens >= item.cost) { Text(stringResource(R.string.buy)) } }
    }
}
