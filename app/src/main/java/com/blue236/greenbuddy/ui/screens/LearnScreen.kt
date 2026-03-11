package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.QuizType
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
    careState: PlantCareState,
    onSubmitAnswer: (Int) -> Boolean,
) {
    val lesson = progress.currentLessonOrNull(lessons)
    val allLessonsComplete = progress.isComplete(lessons)
    val alreadyCompleted = lesson?.id in progress.completedLessonIds
    val lessonKey = lesson?.id ?: "track_complete"
    val quiz = lesson?.quiz
    val dialogue = CompanionPersonalitySystem.dialogueFor(starter, careState, progress, lessons)
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
            Text(dialogue.lessonNudge, color = MaterialTheme.colorScheme.primary)
        }
        if (allLessonsComplete) {
            StatCard("You did it") {
                Text("You’ve completed every lesson in the ${starter.title} starter track.")
                Spacer(Modifier.height(8.dp))
                Text("Your next greenhouse companion unlocks automatically when a track is complete.")
                Spacer(Modifier.height(8.dp))
                Text(dialogue.line)
            }
        } else {
            StatCard("Lesson card") {
                Text(lesson?.summary.orEmpty())
                Spacer(Modifier.height(8.dp))
                Text(lesson?.concept.orEmpty())
                Spacer(Modifier.height(8.dp))
                Text(lesson?.keyTakeaway.orEmpty(), color = MaterialTheme.colorScheme.primary)
            }
            StatCard("Quiz") {
                Text(
                    text = when (quiz?.type) {
                        QuizType.TRUE_FALSE -> "True / False"
                        QuizType.SCENARIO_CHOICE -> "Scenario choice"
                        else -> "Multiple choice"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(quiz?.prompt.orEmpty())
                Spacer(Modifier.height(12.dp))
                quiz?.options?.forEachIndexed { index, option ->
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
                        Row(
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
                Text(lesson?.rewardLabel.orEmpty())
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
                        when (starter.companion.species) {
                            "Monstera" -> if (progress.completedCount + 1 >= lessons.size) {
                                "Correct. We’ve completed the whole track in a very calm, leafy fashion."
                            } else {
                                "Correct. Nice and steady — we’re unfolding well."
                            }
                            "Basil" -> if (progress.completedCount + 1 >= lessons.size) {
                                "Correct! Full track cleared — that was quick and sharp."
                            } else {
                                "Correct! Great pace — let’s keep the energy up."
                            }
                            "Tomato" -> if (progress.completedCount + 1 >= lessons.size) {
                                "Correct. Track complete. Strong work from start to finish."
                            } else {
                                "Correct. Good call — that keeps our growth plan moving."
                            }
                            else -> if (progress.completedCount + 1 >= lessons.size) {
                                "Correct! You finished the full starter track."
                            } else {
                                "Correct! You earned XP, claimed ${lesson?.rewardLabel.orEmpty()}, and advanced to the next lesson."
                            }
                        }
                    } else {
                        when (starter.companion.species) {
                            "Monstera" -> "Not quite. Take another calm look and try again."
                            "Basil" -> "Almost! One more quick shot."
                            "Tomato" -> "Not yet. Reset and take the next attempt seriously."
                            else -> "Not quite — try again."
                        }
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
