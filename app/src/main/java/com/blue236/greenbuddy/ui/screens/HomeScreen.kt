package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
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
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.ui.components.CareStatRow
import com.blue236.greenbuddy.ui.components.StatCard
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    starter: StarterPlantOption,
    lessons: List<Lesson>,
    progress: LessonProgress,
    careState: PlantCareState,
    realPlantModeState: RealPlantModeState,
    onPerformCareAction: (CareAction) -> Unit,
    onSetRealPlantModeEnabled: (Boolean) -> Unit,
    onLogRealPlantCare: (RealPlantCareAction) -> Unit,
) {
    val plant = starter.companion
    val currentLesson = progress.currentLessonOrNull(lessons)
    val nextStageXp = lessons.sumOf { it.rewardXp }
    val completionPercent = if (lessons.isEmpty()) 0 else (progress.completedCount * 100) / lessons.size
    val allLessonsComplete = progress.isComplete(lessons)
    val zoneId = ZoneId.systemDefault()
    val today = java.time.LocalDate.now(zoneId)
    val completedRealActionsToday = realPlantModeState.completedActionsOn(today, zoneId)
    val recentFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("GreenBuddy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Your starter companion is now tuned to ${plant.species} care.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${plant.name} · ${plant.species}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Stage: ${plant.stage} · Mood: ${careState.mood} · Health: ${careState.health}")
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFFC8E6C9), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(plant.emoji, style = MaterialTheme.typography.displayMedium)
                }
                Text("\"${plant.greeting}\"")
            }
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (allLessonsComplete) "Track complete" else "Today’s lesson",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (allLessonsComplete) {
                    Text("You’ve finished the ${starter.title} starter track. Nice work.")
                    Text("Switch starters in PlantDex to begin a new path, or check your profile for total XP.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(currentLesson?.title.orEmpty())
                    Text(currentLesson?.summary.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Starter tip: ${plant.careTip}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        StatCard("Care status") {
            CareStatRow("Hydration", careState.hydration)
            CareStatRow("Sunlight", careState.sunlight)
            CareStatRow("Nutrition", careState.nutrition)
            CareStatRow("Overall", careState.averageScore)
            Text("${plant.name} feels ${careState.mood.lowercase()} and is currently ${careState.health.lowercase()}.")
        }

        StatCard("Care actions") {
            Text("Quick actions change your plant’s live care stats and persist per starter.")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CareAction.entries.forEach { action ->
                    AssistChip(
                        onClick = { onPerformCareAction(action) },
                        label = { Text(action.label) },
                    )
                }
            }
            Text(
                when {
                    careState.hydration <= 35 -> "Try watering next — hydration is getting low."
                    careState.sunlight <= 35 -> "A brighter spot would help right now."
                    careState.nutrition <= 35 -> "Nutrients are running thin — fertilizer would help."
                    else -> "Nice balance. Mix actions over time to keep stats healthy."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        StatCard("Real plant mode") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Track real care habits")
                    Text(
                        "Optional MVP mode: log simple real-world plant care and mirror the first completion of each habit into your companion’s stats each day.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = realPlantModeState.enabled,
                    onCheckedChange = onSetRealPlantModeEnabled,
                )
            }

            if (realPlantModeState.enabled) {
                Text("Today’s checklist")
                RealPlantCareAction.entries.forEach { action ->
                    val done = action in completedRealActionsToday
                    Text("${if (done) "✅" else "⬜"} ${action.label}")
                }
                Text(
                    if (completedRealActionsToday.isEmpty()) {
                        "Start with one real habit today — the first log for each checklist item updates your buddy right away."
                    } else {
                        "${completedRealActionsToday.size}/${RealPlantCareAction.entries.size} real care habits logged today. Repeats on the same day don’t add extra checklist credit or companion boosts."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RealPlantCareAction.entries.forEach { action ->
                        AssistChip(
                            onClick = { onLogRealPlantCare(action) },
                            label = { Text(action.label) },
                        )
                    }
                }

                if (realPlantModeState.entries.isEmpty()) {
                    Text("No real-world care logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Recent log")
                    realPlantModeState.entries.take(4).forEach { entry ->
                        val loggedAt = java.time.Instant.ofEpochMilli(entry.loggedAtEpochMillis)
                            .atZone(zoneId)
                            .format(recentFormatter)
                        Text("• ${entry.action.label} · $loggedAt")
                    }
                }
            } else {
                Text(
                    "Leave this off if you only want the virtual companion loop.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        StatCard("Growth progress") {
            Text(
                if (allLessonsComplete) {
                    "${progress.totalXp} / $nextStageXp XP collected — Young Plant unlocked"
                } else {
                    "${progress.totalXp} / $nextStageXp XP toward Young Plant"
                }
            )
            Spacer(Modifier.size(8.dp))
            Text("Lessons completed: ${progress.completedCount}/${lessons.size} ($completionPercent%)")
            Text(if (allLessonsComplete) "This starter’s intro journey is complete." else "Daily loop: Learn → Quiz → Care → Reward")
        }
    }
}
