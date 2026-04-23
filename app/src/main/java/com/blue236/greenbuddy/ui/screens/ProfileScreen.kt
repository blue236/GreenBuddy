package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.GrowthStageState
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardCatalog
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.localizedDescription
import com.blue236.greenbuddy.model.localizedGrowthAccentLabel
import com.blue236.greenbuddy.model.localizedGrowthTitle
import com.blue236.greenbuddy.model.localizedName
import com.blue236.greenbuddy.model.localizedRequirementSummary
import com.blue236.greenbuddy.model.localizedUnlockHint
import com.blue236.greenbuddy.model.localizedUnlockedMessage
import com.blue236.greenbuddy.ui.components.CosmeticShopCard
import com.blue236.greenbuddy.ui.components.GreenBuddyHeroCard
import com.blue236.greenbuddy.ui.components.LeafTokenDisplay
import com.blue236.greenbuddy.ui.components.SectionTitle
import com.blue236.greenbuddy.ui.components.StatCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    starter: StarterPlantOption,
    progress: LessonProgress,
    dailyMissionSet: DailyMissionSet? = null,
    growthStageState: GrowthStageState,
    ownedPlantCount: Int,
    rewardState: RewardState,
    realPlantModeState: RealPlantModeState,
    onOpenSettings: () -> Unit,
    onAcknowledgeGrowthStage: () -> Unit,
    onPurchaseCosmetic: (CosmeticItem) -> Unit,
    onEquipCosmetic: (String) -> Unit,
) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val personality = CompanionPersonalitySystem.personalityFor(starter.companion.species, localeTag)

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top bar row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = onOpenSettings) {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = stringResource(R.string.open_settings))
            }
        }

        // Companion identity hero card
        GreenBuddyHeroCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${starter.previewEmoji} ${starter.companion.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    rewardState.equippedCosmetic?.let {
                        Text("${it.emoji} ${it.localizedName(localeTag)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                LeafTokenDisplay(amount = rewardState.leafTokens, large = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column {
                    Text("⚡ ${progress.totalXp} XP", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.plants_owned_value, ownedPlantCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                dailyMissionSet?.let {
                    Column {
                        Text(stringResource(R.string.streak_value, it.currentStreak), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                        Text(stringResource(R.string.today_progress, it.completedCount, it.totalCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        RewardWalletCard(rewardState = rewardState, localeTag = localeTag)
        StatCard(stringResource(R.string.growth_status)) {
            Text(
                text = "${growthStageState.currentStage.emoji} ${growthStageState.currentStage.localizedGrowthTitle(localeTag)}",
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                growthStageState.currentStage.localizedGrowthAccentLabel(localeTag),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(onClick = { }, label = { Text(stringResource(R.string.growth_readiness_chip, growthStageState.readinessPercent)) })
                growthStageState.nextStage?.let { nextStage ->
                    AssistChip(onClick = { }, label = { Text(stringResource(R.string.growth_next_stage_chip, nextStage.localizedGrowthTitle(localeTag))) })
                } ?: AssistChip(onClick = { }, label = { Text(stringResource(R.string.growth_final_stage_chip)) })
            }
            growthStageState.nextStage?.let {
                LinearProgressIndicator(
                    progress = { growthStageState.progressToNextStage },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(growthStageState.localizedRequirementSummary(localeTag), fontWeight = FontWeight.SemiBold)
                Text(growthStageState.localizedUnlockHint(localeTag), color = MaterialTheme.colorScheme.primary)
            } ?: Text(
                stringResource(R.string.growth_final_stage_home, growthStageState.currentStage.localizedGrowthTitle(localeTag)),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(growthStageState.currentStage.localizedUnlockedMessage(localeTag))
            if (growthStageState.newlyUnlocked) Button(onClick = onAcknowledgeGrowthStage) { Text(stringResource(R.string.celebrate_growth)) }
        }
        StatCard(stringResource(R.string.companion_personality)) {
            Text(personality.archetype)
            Text(personality.tone)
        }
        dailyMissionSet?.let {
            StatCard(stringResource(R.string.daily_streak)) {
                Text(stringResource(R.string.today_progress, it.completedCount, it.totalCount))
                Text(stringResource(R.string.streak_value, it.currentStreak))
            }
        }
        StatCard(stringResource(R.string.real_plant_habits)) {
            Text(if (realPlantModeState.enabled) stringResource(R.string.mode_enabled) else stringResource(R.string.mode_off))
            Text(stringResource(R.string.logged_entries, realPlantModeState.entries.size))
        }
        SectionTitle(stringResource(R.string.reward_shop))
        Text(
            stringResource(R.string.reward_shop_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RewardCatalog.cosmetics.forEach { item ->
                CosmeticShopCard(
                    item = item,
                    rewardState = rewardState,
                    localeTag = localeTag,
                    onPurchase = { onPurchaseCosmetic(item) },
                    onEquip = { onEquipCosmetic(item.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RewardWalletCard(rewardState: RewardState, localeTag: String) {
    val nextUnlock = rewardState.nextUnlockableCosmetic
    val affordableCount = RewardCatalog.cosmetics.count { rewardState.canPurchase(it) }

    StatCard(stringResource(R.string.reward_wallet_title)) {
        Text(
            stringResource(R.string.wallet_value, rewardState.leafTokens),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            stringResource(
                R.string.reward_token_purpose,
                RewardState.lessonTokenReward(24),
                RewardState.careTokenReward(),
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        rewardState.equippedCosmetic?.let {
            Text(
                stringResource(R.string.reward_equipped_cosmetic, it.emoji, it.localizedName(localeTag)),
                fontWeight = FontWeight.Medium,
            )
        } ?: Text(
            stringResource(R.string.reward_no_cosmetic_equipped),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.reward_affordable_count, affordableCount),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            nextUnlock?.let {
                AssistChip(
                    onClick = { },
                    label = { Text(stringResource(R.string.reward_next_unlock_chip, it.emoji, it.localizedName(localeTag))) },
                )
                val tokensNeeded = rewardState.tokensNeededFor(it)
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            if (tokensNeeded == 0) stringResource(R.string.reward_ready_to_buy_chip)
                            else stringResource(R.string.reward_tokens_needed_chip, tokensNeeded),
                        )
                    },
                )
            } ?: AssistChip(onClick = { }, label = { Text(stringResource(R.string.reward_all_cosmetics_unlocked_short)) })
        }
        Text(
            text = nextUnlock?.let {
                val tokensNeeded = rewardState.tokensNeededFor(it)
                if (tokensNeeded == 0) {
                    stringResource(R.string.reward_ready_to_buy_summary, it.localizedName(localeTag))
                } else {
                    stringResource(R.string.reward_progress_summary, it.localizedName(localeTag), tokensNeeded)
                }
            } ?: stringResource(R.string.reward_all_cosmetics_unlocked),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CosmeticShopRow(item: CosmeticItem, rewardState: RewardState, localeTag: String, onPurchase: () -> Unit, onEquip: () -> Unit) {
    val isUnlocked = item.id in rewardState.unlockedCosmeticIds
    val isEquipped = rewardState.equippedCosmeticId == item.id
    val tokensNeeded = rewardState.tokensNeededFor(item)
    val statusText = when {
        isEquipped -> stringResource(R.string.reward_item_status_equipped)
        isUnlocked -> stringResource(R.string.reward_item_status_owned)
        tokensNeeded == 0 -> stringResource(R.string.reward_item_status_ready)
        else -> stringResource(R.string.reward_item_status_tokens_to_go, tokensNeeded)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${item.emoji} ${item.localizedName(localeTag)}", fontWeight = FontWeight.SemiBold)
            Text(item.localizedDescription(localeTag))
            Text(
                stringResource(R.string.reward_cost_value, item.cost),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(statusText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when {
            isEquipped -> OutlinedButton(onClick = {}, enabled = false) { Text(stringResource(R.string.equipped)) }
            isUnlocked -> Button(onClick = onEquip) { Text(stringResource(R.string.equip)) }
            else -> Button(onClick = onPurchase, enabled = tokensNeeded == 0) { Text(stringResource(R.string.buy)) }
        }
    }
}
