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
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.GrowthStageState
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardCatalog
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.ui.components.StatCard

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, starter: StarterPlantOption, lessons: List<Lesson>, progress: LessonProgress, careState: PlantCareState, dailyMissionSet: DailyMissionSet? = null, growthStageState: GrowthStageState, ownedPlantCount: Int, rewardState: RewardState, realPlantModeState: RealPlantModeState, onAcknowledgeGrowthStage: () -> Unit, onPurchaseCosmetic: (CosmeticItem) -> Unit, onEquipCosmetic: (String) -> Unit) {
    val personality = CompanionPersonalitySystem.personalityFor(starter.companion.species)
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StatCard("Progress") { Text("XP ${progress.totalXp}"); Text("Leaf tokens ${rewardState.leafTokens}"); Text("Plants owned $ownedPlantCount") }
        StatCard("Growth status") { Text(growthStageState.currentStage.title); if (growthStageState.newlyUnlocked) Button(onClick = onAcknowledgeGrowthStage) { Text("Celebrate growth") } }
        StatCard("Companion personality") { Text(personality.archetype); Text(personality.tone) }
        dailyMissionSet?.let { StatCard("Daily streak") { Text("${it.completedCount}/${it.totalCount} today"); Text("Streak ${it.currentStreak}") } }
        StatCard("Real plant habits") { Text(if (realPlantModeState.enabled) "Mode enabled" else "Mode off"); Text("Logged entries ${realPlantModeState.entries.size}") }
        StatCard("Reward shop") { RewardCatalog.cosmetics.forEach { CosmeticShopRow(it, rewardState, { onPurchaseCosmetic(it) }, { onEquipCosmetic(it.id) }) } }
    }
}

@Composable
private fun CosmeticShopRow(item: CosmeticItem, rewardState: RewardState, onPurchase: () -> Unit, onEquip: () -> Unit) {
    val isUnlocked = item.id in rewardState.unlockedCosmeticIds
    val isEquipped = rewardState.equippedCosmeticId == item.id
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) { Text("${item.emoji} ${item.name}", fontWeight = FontWeight.SemiBold); Text(item.description) }
        when { isEquipped -> OutlinedButton(onClick = {}, enabled = false) { Text("Equipped") }; isUnlocked -> Button(onClick = onEquip) { Text("Equip") }; else -> Button(onClick = onPurchase, enabled = rewardState.leafTokens >= item.cost) { Text("Buy") } }
    }
}
