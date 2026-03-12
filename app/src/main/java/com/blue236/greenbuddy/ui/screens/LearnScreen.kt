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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.QuizType
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.model.localizedTitle
import com.blue236.greenbuddy.ui.components.StatCard

@Composable
fun LearnScreen(modifier: Modifier = Modifier, starter: StarterPlantOption, lessons: List<Lesson>, progress: LessonProgress, careState: PlantCareState, onSubmitAnswer: (Int) -> Boolean) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val lesson = progress.currentLessonOrNull(lessons)
    val allLessonsComplete = progress.isComplete(lessons)
    val alreadyCompleted = lesson?.id in progress.completedLessonIds
    val lessonKey = lesson?.id ?: "track_complete"
    val quiz = lesson?.quiz
    val dialogue = CompanionPersonalitySystem.dialogueFor(starter, careState, progress, lessons, localeTag)
    var selectedAnswerIndex by rememberSaveable(lessonKey) { mutableIntStateOf(-1) }
    var feedbackMessage by rememberSaveable(lessonKey) { mutableStateOf<String?>(null) }
    val pickAnswerFirst = stringResource(R.string.pick_answer_first)
    val incorrectFeedback = when (localeTag.take(2)) {
        "de" -> "Noch nicht ganz — versuch’s noch einmal."
        "ko" -> "조금 아쉬워요. 한 번 더 해봐요."
        else -> "Not quite — try again."
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.learn_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StatCard(stringResource(R.string.starter_focus)) {
            Text(stringResource(R.string.current_companion, "${starter.companion.name} · ${starter.localizedTitle(localeTag)}"))
            Text(if (allLessonsComplete) stringResource(R.string.all_lessons_complete) else stringResource(R.string.current_lesson, lesson?.title.orEmpty()))
            Text(stringResource(R.string.completed_count, progress.completedCount, lessons.size))
            Text(dialogue.lessonNudge, color = MaterialTheme.colorScheme.primary)
        }
        if (allLessonsComplete) {
            StatCard(stringResource(R.string.you_did_it)) { Text(stringResource(R.string.track_complete_message, starter.localizedTitle(localeTag))); Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.next_greenhouse_unlock)); Spacer(Modifier.height(8.dp)); Text(dialogue.line) }
        } else {
            StatCard(stringResource(R.string.lesson_card)) { Text(lesson?.summary.orEmpty()); Spacer(Modifier.height(8.dp)); Text(lesson?.concept.orEmpty()); Spacer(Modifier.height(8.dp)); Text(lesson?.keyTakeaway.orEmpty(), color = MaterialTheme.colorScheme.primary) }
            StatCard(stringResource(R.string.quiz)) {
                Text(text = when (quiz?.type) { QuizType.TRUE_FALSE -> stringResource(R.string.quiz_type_true_false); QuizType.SCENARIO_CHOICE -> stringResource(R.string.quiz_type_scenario_choice); else -> stringResource(R.string.quiz_type_multiple_choice) }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp)); Text(quiz?.prompt.orEmpty()); Spacer(Modifier.height(12.dp))
                quiz?.options?.forEachIndexed { index, option ->
                    Card(modifier = Modifier.fillMaxWidth().selectable(selected = selectedAnswerIndex == index, enabled = !alreadyCompleted, onClick = { selectedAnswerIndex = index; feedbackMessage = null }), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (selectedAnswerIndex == index) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = selectedAnswerIndex == index, onClick = null, enabled = !alreadyCompleted); Text(option, modifier = Modifier.padding(start = 8.dp)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                feedbackMessage?.let { Text(it, color = if (alreadyCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            StatCard(stringResource(R.string.reward)) { Text(stringResource(R.string.correct_answer_reward, lesson?.rewardXp ?: 0)); Text(lesson?.rewardLabel.orEmpty()); Text(if (alreadyCompleted) stringResource(R.string.reward_already_claimed) else stringResource(R.string.answer_to_unlock)) }
            Button(onClick = {
                if (selectedAnswerIndex < 0) { feedbackMessage = pickAnswerFirst; return@Button }
                val isCorrect = onSubmitAnswer(selectedAnswerIndex)
                feedbackMessage = if (isCorrect) dialogue.line else incorrectFeedback
            }, enabled = !alreadyCompleted, modifier = Modifier.fillMaxWidth()) { Text(if (alreadyCompleted) stringResource(R.string.lesson_completed) else stringResource(R.string.check_answer)) }
        }
    }
}
