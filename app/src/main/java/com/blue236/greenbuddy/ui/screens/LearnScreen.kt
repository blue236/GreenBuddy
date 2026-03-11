package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.ui.components.StatCard

@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    starter: StarterPlantOption,
    lessons: List<Lesson>,
    progress: LessonProgress,
    onSubmitAnswer: (Int) -> Boolean,
) {
    val lesson = progress.currentLessonOrNull(lessons)
    val allLessonsComplete = progress.isComplete(lessons)
    val alreadyCompleted = lesson?.id in progress.completedLessonIds
    val lessonKey = lesson?.id ?: "track_complete"
    var selectedAnswerIndex by rememberSaveable(lessonKey) { mutableIntStateOf(-1) }
    var feedbackMessage by rememberSaveable(lessonKey) { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Learn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StatCard("Starter focus") {
            Text("Current companion: ${starter.companion.name} the ${starter.companion.species}")
            Text(if (allLessonsComplete) "Current lesson: All starter lessons complete" else "Current lesson: ${lesson?.title.orEmpty()}")
            Text("Completed: ${progress.completedCount}/${lessons.size}")
        }
        if (allLessonsComplete) {
            StatCard("You did it") {
                Text("You’ve completed every lesson in the ${starter.title} starter track.")
                Spacer(Modifier.height(8.dp))
                Text("Your next greenhouse companion unlocks automatically when a track is complete.")
            }
        } else {
            StatCard("Lesson card") {
                Text(lesson?.summary.orEmpty())
                Spacer(Modifier.height(8.dp))
                Text(lesson?.concept.orEmpty())
            }
            StatCard("Quiz") {
                Text(lesson?.quizPrompt.orEmpty())
                Spacer(Modifier.height(12.dp))
                lesson?.quizOptions?.forEachIndexed { index, option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedAnswerIndex == index,
                            enabled = !alreadyCompleted,
                            onClick = {
                                selectedAnswerIndex = index
                                feedbackMessage = null
                            },
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedAnswerIndex == index) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selectedAnswerIndex == index, onClick = null, enabled = !alreadyCompleted)
                        Text(option, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

                if (feedbackMessage != null) {
                    Text(
                        text = feedbackMessage.orEmpty(),
                        color = if (alreadyCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            StatCard("Reward") {
                Text("Correct answer reward: XP +${lesson?.rewardXp ?: 0}")
                Text(
                    if (alreadyCompleted) {
                        "Lesson completed. Reward already claimed."
                    } else {
                        "Answer correctly to unlock the next lesson."
                    },
                )
            }
            Button(
                onClick = {
                    if (selectedAnswerIndex < 0) {
                        feedbackMessage = "Pick an answer first."
                        return@Button
                    }

                    val isCorrect = onSubmitAnswer(selectedAnswerIndex)
                    feedbackMessage = if (isCorrect) {
                        if (progress.completedCount + 1 >= lessons.size) {
                            "Correct! You finished the full starter track."
                        } else {
                            "Correct! You earned XP and advanced to the next lesson."
                        }
                    } else {
                        "Not quite — try again."
                    }
                },
                enabled = !alreadyCompleted,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (alreadyCompleted) "Lesson completed" else "Check answer")
            }
        }
    }
}
