package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.GrowthStageState
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RewardCatalog
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.ui.components.StatCard

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    starter: StarterPlantOption,
    lessons: List<Lesson>,
    progress: LessonProgress,
    careState: PlantCareState,
    dailyMissionSet: DailyMissionSet? = null,
    growthStageState: GrowthStageState,
    ownedPlantCount: Int,
    rewardState: RewardState,
    onAcknowledgeGrowthStage: () -> Unit,
    onPurchaseCosmetic: (CosmeticItem) -> Unit,
    onEquipCosmetic: (String) -> Unit,
) {
    val allLessonsComplete = progress.isComplete(lessons)
    val nextLesson = progress.currentLessonOrNull(lessons)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StatCard("Progress") {
            Text("Level 1")
            Text("Starter journey progress ${progress.completedCount}/${lessons.size}")
            Text("XP ${progress.totalXp}")
            Text("Leaf tokens ${rewardState.leafTokens}")
            Text(if (allLessonsComplete) "Track status: complete" else "Track status: in progress")
            Text("Plants owned $ownedPlantCount")
        }
        StatCard("Growth status") {
            Text("Growth stage: ${growthStageState.currentStage.title} ${growthStageState.currentStage.emoji}")
            Text("Care score: ${careState.averageScore}")
            Text(growthStageState.requirementSummary)
            Text(if (growthStageState.nextStage == null) "Final stage reached" else "Readiness ${growthStageState.readinessPercent}%")
            if (growthStageState.newlyUnlocked) {
                Text(growthStageState.currentStage.unlockedMessage)
                Button(onClick = onAcknowledgeGrowthStage) {
                    Text("Celebrate growth")
                }
            }
        }
        StatCard("Greenhouse setup") {
            Text("Active plant: ${starter.title}")
            Text("Companion: ${starter.companion.name}")
            Text("Current mood: ${careState.mood}")
            Text("Current health: ${careState.health}")
            Text(if (allLessonsComplete) "Next lesson: starter track completed" else "Next lesson: ${nextLesson?.title.orEmpty()}")
        }
        dailyMissionSet?.let { missions ->
            StatCard("Daily streak") {
                Text("Today: ${missions.completedCount}/${missions.totalCount} missions")
                Text("Current streak: ${missions.currentStreak} day(s)")
                Text("Longest streak: ${missions.longestStreak} day(s)")
                Text("Wallet balance: ${rewardState.leafTokens} leaf tokens")
                Text(
                    if (missions.pendingStreakReward) {
                        "Streak reward ready: +${missions.streakRewardTokens} leaf tokens"
                    } else {
                        "Daily reward: +${missions.dailyRewardTokens} leaf tokens when all missions are done"
                    },
                )
            }
        }
        StatCard("Reward shop") {
            Text("Spend leaf tokens on cosmetic unlocks, then equip your favorite look.")
            RewardCatalog.cosmetics.forEach { item ->
                CosmeticShopRow(
                    item = item,
                    rewardState = rewardState,
                    onPurchase = { onPurchaseCosmetic(item) },
                    onEquip = { onEquipCosmetic(item.id) },
                )
            }
        }
        StatCard("Roadmap focus") {
            Text("MVP: onboarding, growth thresholds, daily loop, greenhouse inventory, rewards + cosmetics")
        }
    }
}

@Composable
private fun CosmeticShopRow(
    item: CosmeticItem,
    rewardState: RewardState,
    onPurchase: () -> Unit,
    onEquip: () -> Unit,
) {
    val isUnlocked = item.id in rewardState.unlockedCosmeticIds
    val isEquipped = rewardState.equippedCosmeticId == item.id
    val canAfford = rewardState.leafTokens >= item.cost

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${item.emoji} ${item.name}", fontWeight = FontWeight.SemiBold)
            Text(item.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                when {
                    isEquipped -> "Equipped"
                    isUnlocked -> "Unlocked"
                    canAfford -> "Cost ${item.cost} leaf tokens"
                    else -> "Need ${item.cost - rewardState.leafTokens} more leaf tokens"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            isEquipped -> OutlinedButton(onClick = {}, enabled = false) { Text("Equipped") }
            isUnlocked -> Button(onClick = onEquip) { Text("Equip") }
            else -> Button(onClick = onPurchase, enabled = canAfford) { Text("Buy") }
        }
    }
}
