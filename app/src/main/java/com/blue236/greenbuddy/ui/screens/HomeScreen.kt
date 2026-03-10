package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.companionFeedback
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.growthStageVisual
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.ui.components.CareStatRow
import com.blue236.greenbuddy.ui.components.StatCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    starter: StarterPlantOption,
    lessons: List<Lesson>,
    progress: LessonProgress,
    careState: PlantCareState,
    onPerformCareAction: (CareAction) -> Unit,
) {
    val plant = starter.companion
    val currentLesson = progress.currentLessonOrNull(lessons)
    val nextStageXp = lessons.sumOf { it.rewardXp }
    val completionPercent = if (lessons.isEmpty()) 0 else (progress.completedCount * 100) / lessons.size
    val allLessonsComplete = progress.isComplete(lessons)
    val growthStage = progress.growthStageVisual(lessons)
    val feedback = companionFeedback(
        plantName = plant.name,
        careState = careState,
        progress = progress,
        lessons = lessons,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("GreenBuddy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "${plant.name} is your ${plant.species.lowercase()} study companion.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F5A36)),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            plant.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            "${growthStage.title} • ${careState.health}",
                            color = Color(0xFFE9F6EC),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            growthStage.accentLabel,
                            color = Color(0xFFB8F2C5),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(Color(0xFFB8E6C1), RoundedCornerShape(28.dp))
                            .border(2.dp, Color(0xFFE8FFF0), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(growthStage.emoji, style = MaterialTheme.typography.displayMedium)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroPill(label = "Mood", value = careState.mood)
                    HeroPill(label = "Lessons", value = "${progress.completedCount}/${lessons.size}")
                    HeroPill(label = "XP", value = progress.totalXp.toString())
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Growth progress", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("$completionPercent%", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { growthStage.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = Color(0xFF9BE7AE),
                        trackColor = Color(0xFF2F7448),
                    )
                    Text(growthStage.milestoneText, color = Color(0xFFD7F3DE))
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6ECD2)),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(feedback.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(feedback.message, color = Color(0xFF3D3421))
                Text(feedback.focusLabel, color = Color(0xFF7A4B00), fontWeight = FontWeight.SemiBold)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F2)),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (allLessonsComplete) "Track complete" else "Up next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (allLessonsComplete) "All lessons done" else "Quick win",
                        color = Color(0xFF1F5A36),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (allLessonsComplete) {
                    Text("You’ve finished the ${starter.title} starter track. Keep care balanced to hold the thriving state.")
                    Text(
                        "Switch starters in PlantDex for a new path, or use care actions below to maintain momentum.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(currentLesson?.title.orEmpty(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(currentLesson?.summary.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Starter tip: ${plant.careTip}", color = Color(0xFF1F5A36), fontWeight = FontWeight.Medium)
                }
            }
        }

        StatCard(
            title = "Care actions",
            containerColor = Color(0xFFE7F4EA),
        ) {
            Text("Tap an action to change live plant stats right away.")
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
                    careState.hydration <= 35 -> "Try watering next — hydration is low."
                    careState.sunlight <= 35 -> "Sunlight is lagging — a brighter spot helps most."
                    careState.nutrition <= 35 -> "Nutrition is the weakest stat — fertilize soon."
                    else -> "Everything looks balanced. Keep rotating actions to stay in the thriving range."
                },
                color = Color(0xFF1F5A36),
                fontWeight = FontWeight.Medium,
            )
        }

        StatCard(
            title = "Care status",
            containerColor = Color(0xFFFFFFFF),
        ) {
            CareStatRow("Hydration", careState.hydration)
            CareStatRow("Sunlight", careState.sunlight)
            CareStatRow("Nutrition", careState.nutrition)
            CareStatRow("Overall", careState.averageScore)
            Text("${plant.name} feels ${careState.mood.lowercase()} and is currently ${careState.health.lowercase()}.")
        }

        StatCard(
            title = "Growth progress",
            containerColor = Color(0xFFF4F1FF),
        ) {
            Text(
                if (allLessonsComplete) {
                    "${progress.totalXp} / $nextStageXp XP collected — ${growthStage.title} unlocked"
                } else {
                    "${progress.totalXp} / $nextStageXp XP toward ${growthStage.title}"
                }
            )
            Spacer(Modifier.height(4.dp))
            Text("Lessons completed: ${progress.completedCount}/${lessons.size} ($completionPercent%)")
            Text(if (allLessonsComplete) "This starter’s intro journey is complete." else "Daily loop: Learn → Quiz → Care → Reward")
        }
    }
}

@Composable
private fun HeroPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFF2F7448), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, color = Color(0xFFD7F3DE), style = MaterialTheme.typography.labelMedium)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
