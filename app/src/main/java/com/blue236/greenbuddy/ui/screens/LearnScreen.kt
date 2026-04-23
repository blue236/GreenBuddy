package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.blue236.greenbuddy.ui.components.GreenBuddyHeroCard
import com.blue236.greenbuddy.ui.components.LessonPathNode
import com.blue236.greenbuddy.ui.components.QuizOptionState
import com.blue236.greenbuddy.ui.components.QuizOptionTile

private enum class LearnUiState {
    IDLE,
    EVALUATED_CORRECT,
    EVALUATED_INCORRECT,
    COMPLETED,
}

@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    starter: StarterPlantOption,
    lessons: List<Lesson>,
    progress: LessonProgress,
    careState: PlantCareState,
    onSubmitAnswer: (Int) -> Boolean,
) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val lesson = progress.currentLessonOrNull(lessons)
    val allLessonsComplete = progress.isComplete(lessons)
    val alreadyCompleted = lesson?.id in progress.completedLessonIds
    val lessonKey = lesson?.id ?: "track_complete"
    val quiz = lesson?.quiz
    val dialogue = CompanionPersonalitySystem.dialogueFor(starter, careState, progress, lessons, localeTag)
    val lessonIndex = lesson?.let { current -> lessons.indexOfFirst { it.id == current.id }.takeIf { it >= 0 }?.plus(1) } ?: lessons.size
    val progressValue = if (lessons.isEmpty()) 1f else (progress.completedCount.coerceAtLeast(0).toFloat() / lessons.size.toFloat()).coerceIn(0f, 1f)

    var selectedAnswerIndex by rememberSaveable(lessonKey) { mutableIntStateOf(-1) }
    var feedbackMessage by rememberSaveable(lessonKey) { mutableStateOf<String?>(null) }
    var learnUiState by rememberSaveable(lessonKey) {
        mutableStateOf(
            when {
                allLessonsComplete || alreadyCompleted -> LearnUiState.COMPLETED
                else -> LearnUiState.IDLE
            },
        )
    }

    val bottomButtonLabel = when (learnUiState) {
        LearnUiState.IDLE -> stringResource(R.string.check_answer)
        LearnUiState.EVALUATED_CORRECT -> stringResource(R.string.learn_continue)
        LearnUiState.COMPLETED -> stringResource(R.string.lesson_completed)
        LearnUiState.EVALUATED_INCORRECT -> stringResource(R.string.check_answer)
    }
    val pickAnswerFirstText = stringResource(R.string.pick_answer_first)
    val tryAgainText = stringResource(R.string.learn_try_again)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            LearnBottomBar(
                uiState = learnUiState,
                feedbackMessage = feedbackMessage,
                buttonLabel = bottomButtonLabel,
                buttonEnabled = !allLessonsComplete && !alreadyCompleted,
                onPrimaryAction = {
                    if (selectedAnswerIndex < 0) {
                        feedbackMessage = pickAnswerFirstText
                        return@LearnBottomBar
                    }
                    val isCorrect = onSubmitAnswer(selectedAnswerIndex)
                    if (isCorrect) {
                        learnUiState = LearnUiState.EVALUATED_CORRECT
                        feedbackMessage = dialogue.line
                    } else {
                        learnUiState = LearnUiState.EVALUATED_INCORRECT
                        feedbackMessage = tryAgainText
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LearnProgressStrip(
                progressValue = progressValue,
                completedCount = progress.completedCount,
                totalCount = lessons.size,
                totalXp = progress.totalXp,
            )
            LearnHeroCard(
                starter = starter,
                localeTag = localeTag,
                lessonTitle = if (allLessonsComplete) stringResource(R.string.all_lessons_complete) else lesson?.title.orEmpty(),
                lessonIndex = lessonIndex,
                lessonCount = lessons.size,
                supportLine = dialogue.lessonNudge,
            )

            if (allLessonsComplete) {
                TrackCompleteCard(
                    starter = starter,
                    localeTag = localeTag,
                    dialogueLine = dialogue.line,
                )
            } else {
                LearnPathCard(
                    lessons = lessons,
                    currentLessonId = lesson?.id,
                    completedLessonIds = progress.completedLessonIds,
                )
                LessonPreviewCard(
                    lesson = lesson,
                )
                QuizChallengeCard(
                    quizType = quiz?.type,
                    prompt = quiz?.prompt.orEmpty(),
                    options = quiz?.options.orEmpty(),
                    selectedAnswerIndex = selectedAnswerIndex,
                    evaluatedState = learnUiState,
                    onSelectAnswer = { index ->
                        if (alreadyCompleted) return@QuizChallengeCard
                        selectedAnswerIndex = index
                        feedbackMessage = null
                        if (learnUiState != LearnUiState.IDLE) learnUiState = LearnUiState.IDLE
                    },
                )
                RewardPreviewCard(
                    rewardXp = lesson?.rewardXp ?: 0,
                    rewardLabel = lesson?.rewardLabel.orEmpty(),
                    alreadyCompleted = alreadyCompleted,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LearnProgressStrip(
    progressValue: Float,
    completedCount: Int,
    totalCount: Int,
    totalXp: Int,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.learn_title), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.xp_value, totalXp), color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            LinearProgressIndicator(progress = { progressValue }, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.completed_count, completedCount, totalCount), fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.today_progress, completedCount, totalCount), color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

@Composable
private fun LearnHeroCard(
    starter: StarterPlantOption,
    localeTag: String,
    lessonTitle: String,
    lessonIndex: Int,
    lessonCount: Int,
    supportLine: String,
) {
    GreenBuddyHeroCard {
        Text(
            stringResource(R.string.current_companion, "${starter.companion.name} · ${starter.localizedTitle(localeTag)}"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            lessonTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.completed_count, lessonIndex.coerceAtLeast(1), lessonCount),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(supportLine, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LearnPathCard(
    lessons: List<Lesson>,
    currentLessonId: String?,
    completedLessonIds: Set<String>,
) {
    Card(shape = MaterialTheme.shapes.medium) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.learn_path_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            lessons.take(5).forEachIndexed { index, lesson ->
                val isCompleted = lesson.id in completedLessonIds
                val isCurrent = lesson.id == currentLessonId
                LessonPathNode(
                    index = index + 1,
                    title = lesson.title,
                    isCurrent = isCurrent,
                    isCompleted = isCompleted,
                )
            }
        }
    }
}

@Composable
private fun LessonPreviewCard(
    lesson: Lesson?,
) {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.learn_preview_title), fontWeight = FontWeight.SemiBold)
            Text(lesson?.summary.orEmpty())
            Text(lesson?.concept.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(lesson?.keyTakeaway.orEmpty(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(
                lesson?.rewardLabel.orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun QuizChallengeCard(
    quizType: QuizType?,
    prompt: String,
    options: List<String>,
    selectedAnswerIndex: Int,
    evaluatedState: LearnUiState,
    onSelectAnswer: (Int) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = when (quizType) {
                    QuizType.TRUE_FALSE -> stringResource(R.string.quiz_type_true_false)
                    QuizType.SCENARIO_CHOICE -> stringResource(R.string.quiz_type_scenario_choice)
                    else -> stringResource(R.string.quiz_type_multiple_choice)
                },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            options.forEachIndexed { index, option ->
                val isSelected = selectedAnswerIndex == index
                val quizState = when {
                    evaluatedState == LearnUiState.EVALUATED_CORRECT && isSelected -> QuizOptionState.Correct
                    evaluatedState == LearnUiState.EVALUATED_INCORRECT && isSelected -> QuizOptionState.Incorrect
                    isSelected -> QuizOptionState.Selected
                    else -> QuizOptionState.Idle
                }
                QuizOptionTile(
                    letterLabel = ('A' + index).toString(),
                    text = option,
                    state = quizState,
                    onClick = { onSelectAnswer(index) },
                )
            }
        }
    }
}

@Composable
private fun RewardPreviewCard(
    rewardXp: Int,
    rewardLabel: String,
    alreadyCompleted: Boolean,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.learn_reward_ready), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.correct_answer_reward, rewardXp), fontWeight = FontWeight.Bold)
            Text(rewardLabel)
            Text(
                if (alreadyCompleted) stringResource(R.string.reward_already_claimed) else stringResource(R.string.answer_to_unlock),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun TrackCompleteCard(
    starter: StarterPlantOption,
    localeTag: String,
    dialogueLine: String,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.you_did_it), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.track_complete_message, starter.localizedTitle(localeTag)))
            Text(stringResource(R.string.next_greenhouse_unlock), fontWeight = FontWeight.SemiBold)
            Text(dialogueLine, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LearnBottomBar(
    uiState: LearnUiState,
    feedbackMessage: String?,
    buttonLabel: String,
    buttonEnabled: Boolean,
    onPrimaryAction: () -> Unit,
) {
    val statusLabel = when (uiState) {
        LearnUiState.EVALUATED_CORRECT -> stringResource(R.string.learn_status_complete)
        LearnUiState.EVALUATED_INCORRECT -> stringResource(R.string.learn_status_retry)
        LearnUiState.COMPLETED -> stringResource(R.string.you_did_it)
        LearnUiState.IDLE -> stringResource(R.string.learn_status_ready)
    }
    Card(
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            feedbackMessage?.let {
                Text(
                    it,
                    color = when (uiState) {
                        LearnUiState.EVALUATED_CORRECT,
                        LearnUiState.COMPLETED,
                            -> MaterialTheme.colorScheme.primary
                        LearnUiState.EVALUATED_INCORRECT -> MaterialTheme.colorScheme.error
                        LearnUiState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (uiState == LearnUiState.EVALUATED_CORRECT) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                    Text(statusLabel)
                }
                Button(
                    onClick = onPrimaryAction,
                    enabled = buttonEnabled,
                    modifier = Modifier.weight(2f),
                ) {
                    Text(buttonLabel)
                }
            }
        }
    }
}
