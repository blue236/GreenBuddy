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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.GrowthStageState
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.ui.components.StatCard
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, starter: StarterPlantOption, lessons: List<Lesson>, progress: LessonProgress, careState: PlantCareState, dailyMissionSet: DailyMissionSet? = null, growthStageState: GrowthStageState, greenhouseCount: Int, rewardState: RewardState, rewardFeedback: String?, realPlantModeState: RealPlantModeState, onPerformCareAction: (CareAction) -> Unit, onAcknowledgeGrowthStage: () -> Unit, onSetRealPlantModeEnabled: (Boolean) -> Unit, onLogRealPlantCare: (RealPlantCareAction) -> Unit) {
    val dialogue = CompanionPersonalitySystem.dialogueFor(starter, careState, progress, lessons)
    val currentLesson = progress.currentLessonOrNull(lessons)
    val zoneId = ZoneId.systemDefault()
    val completedToday = realPlantModeState.completedActionsOn(LocalDate.now(zoneId), zoneId)
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("GreenBuddy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("${starter.companion.name} • greenhouse size $greenhouseCount")
        if (growthStageState.newlyUnlocked) Card { Column(Modifier.padding(16.dp)) { Text("New evolution unlocked: ${growthStageState.currentStage.title}"); Button(onClick = onAcknowledgeGrowthStage) { Text("Nice") } } }
        StatCard("Companion") { Text(dialogue.headline); Text(dialogue.line); Text("Wallet: ${rewardState.leafTokens} leaf tokens") }
        rewardFeedback?.let { StatCard("Reward pulse") { Text(it) } }
        dailyMissionSet?.let { StatCard("Daily missions") { Text("Completed ${it.completedCount}/${it.totalCount}"); Text("Streak ${it.currentStreak}") } }
        StatCard("Today’s lesson") { Text(if (progress.isComplete(lessons)) "Track complete" else currentLesson?.title.orEmpty()); Text(dialogue.lessonNudge) }
        StatCard("Care actions") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { CareAction.entries.forEach { AssistChip(onClick = { onPerformCareAction(it) }, label = { Text(it.label) }) } }
            Text(dialogue.careGuidance, color = Color(0xFF1F5A36))
        }
        StatCard("Real plant mode") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mirror real-world care into your buddy")
                Switch(checked = realPlantModeState.enabled, onCheckedChange = onSetRealPlantModeEnabled)
            }
            if (realPlantModeState.enabled) {
                Text("Today ${completedToday.size}/${RealPlantCareAction.entries.size}")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { RealPlantCareAction.entries.forEach { AssistChip(onClick = { onLogRealPlantCare(it) }, label = { Text(it.label) }) } }
            }
        }
    }
}
