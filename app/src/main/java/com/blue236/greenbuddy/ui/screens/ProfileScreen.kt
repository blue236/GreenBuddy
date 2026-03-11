package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
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
) {
    val allLessonsComplete = progress.isComplete(lessons)
    val nextLesson = progress.currentLessonOrNull(lessons)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StatCard("Progress") {
            Text("Level 1")
            Text("Starter journey progress ${progress.completedCount}/${lessons.size}")
            Text("XP ${progress.totalXp}")
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
        dailyMissionSet?.let { missions ->
            StatCard("Daily streak") {
                Text("Today: ${missions.completedCount}/${missions.totalCount} missions")
                Text("Current streak: ${missions.currentStreak} day(s)")
                Text("Longest streak: ${missions.longestStreak} day(s)")
                Text("Leaf tokens: ${missions.leafTokens}")
                Text(
                    if (missions.pendingStreakReward) {
                        "Streak reward ready: +${missions.streakRewardTokens} leaf tokens"
                    } else {
                        "Daily reward: +${missions.dailyRewardTokens} leaf tokens when all missions are done"
                    },
                )
            }
        }
        StatCard("Roadmap focus") {
            Text("MVP: onboarding, daily lesson, care loop, growth state, PlantDex")
        }
    }
}
