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
    rewardState: RewardState,
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
            Text("Plants unlocked 3")
        }
        StatCard("Starter setup") {
            Text("Chosen starter: ${starter.title}")
            Text("Companion: ${starter.companion.name}")
            Text("Current mood: ${careState.mood}")
            Text("Current health: ${careState.health}")
            Text(if (allLessonsComplete) "Next lesson: starter track completed" else "Next lesson: ${nextLesson?.title.orEmpty()}")
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
            Text("MVP: onboarding, daily lesson, care loop, growth state, PlantDex, rewards + cosmetics")
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
