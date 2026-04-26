package com.blue236.greenbuddy.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CompanionEmotion
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.QuizType
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.model.localizedTitle
import com.blue236.greenbuddy.ui.components.CompanionAvatarBubble
import com.blue236.greenbuddy.ui.components.GreenBuddyButton
import com.blue236.greenbuddy.ui.components.GreenBuddyButtonVariant
import com.blue236.greenbuddy.ui.components.QuizOptionState
import com.blue236.greenbuddy.ui.components.QuizOptionTile
import com.blue236.greenbuddy.ui.theme.GreenBuddyColors

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
    val lessonIndex = lesson?.let { current ->
        lessons.indexOfFirst { it.id == current.id }.takeIf { it >= 0 }?.plus(1)
    } ?: lessons.size
    val progressValue = if (lessons.isEmpty()) 1f
    else (progress.completedCount.coerceAtLeast(0).toFloat() / lessons.size.toFloat()).coerceIn(0f, 1f)

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

    val pickAnswerFirstText = stringResource(R.string.pick_answer_first)
    val tryAgainText = stringResource(R.string.learn_try_again)
    val correctExclaimText = stringResource(R.string.learn_correct_exclaim)

    // Emotion maps to quiz state — gives the companion life during learning
    val companionEmotion = when (learnUiState) {
        LearnUiState.EVALUATED_CORRECT, LearnUiState.COMPLETED -> CompanionEmotion.PROUD
        LearnUiState.EVALUATED_INCORRECT -> CompanionEmotion.CURIOUS
        LearnUiState.IDLE -> CompanionEmotion.CALM
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            HealingLearnBottomBar(
                uiState = learnUiState,
                feedbackMessage = feedbackMessage,
                buttonEnabled = !allLessonsComplete && !alreadyCompleted,
                onPrimaryAction = {
                    if (selectedAnswerIndex < 0) {
                        feedbackMessage = pickAnswerFirstText
                        return@HealingLearnBottomBar
                    }
                    val isCorrect = onSubmitAnswer(selectedAnswerIndex)
                    if (isCorrect) {
                        learnUiState = LearnUiState.EVALUATED_CORRECT
                        feedbackMessage = correctExclaimText
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
                .padding(innerPadding),
        ) {
            // ── Top progress strip ───────────────────────────────────────
            LearnProgressStrip(
                progressValue = progressValue,
                lessonIndex = lessonIndex,
                lessonCount = lessons.size,
                totalXp = progress.totalXp,
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Companion stage — character + lesson title ───────────
                CompanionLessonStage(
                    starter = starter,
                    localeTag = localeTag,
                    lessonTitle = if (allLessonsComplete) stringResource(R.string.all_lessons_complete)
                    else lesson?.title.orEmpty(),
                    companionEmotion = companionEmotion,
                    supportLine = dialogue.lessonNudge,
                )

                // ── Horizontal lesson path ───────────────────────────────
                HorizontalLessonPath(
                    lessons = lessons,
                    currentLessonId = lesson?.id,
                    completedLessonIds = progress.completedLessonIds,
                )

                if (allLessonsComplete) {
                    TrackCompleteCard(
                        starter = starter,
                        localeTag = localeTag,
                        dialogueLine = dialogue.line,
                    )
                } else {
                    // ── Lore card — lesson context as story ─────────────
                    LessonLoreCard(lesson = lesson)

                    // ── Quiz challenge ───────────────────────────────────
                    QuizChallengeSection(
                        quizType = quiz?.type,
                        prompt = quiz?.prompt.orEmpty(),
                        options = quiz?.options.orEmpty(),
                        selectedAnswerIndex = selectedAnswerIndex,
                        evaluatedState = learnUiState,
                        onSelectAnswer = { index ->
                            if (alreadyCompleted) return@QuizChallengeSection
                            selectedAnswerIndex = index
                            feedbackMessage = null
                            if (learnUiState != LearnUiState.IDLE) learnUiState = LearnUiState.IDLE
                        },
                    )

                    // ── Reward pills ─────────────────────────────────────
                    RewardPillsRow(
                        rewardXp = lesson?.rewardXp ?: 0,
                        alreadyCompleted = alreadyCompleted,
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Top progress strip ────────────────────────────────────────────────────────

@Composable
private fun LearnProgressStrip(
    progressValue: Float,
    lessonIndex: Int,
    lessonCount: Int,
    totalXp: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.learn_lesson_of, lessonIndex.coerceAtLeast(1), lessonCount),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("⚡", fontSize = 14.sp)
                Text(
                    stringResource(R.string.xp_value, totalXp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenBuddyColors.leafGold,
                )
            }
        }
        LinearProgressIndicator(
            progress = { progressValue },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
            strokeCap = StrokeCap.Round,
        )
    }
}

// ── Companion lesson stage ────────────────────────────────────────────────────

@Composable
private fun CompanionLessonStage(
    starter: StarterPlantOption,
    localeTag: String,
    lessonTitle: String,
    companionEmotion: CompanionEmotion,
    supportLine: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompanionAvatarBubble(
            emoji = companionEmoji(companionEmotion),
            emotion = companionEmotion,
            size = 80,
        )
        Text(
            lessonTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "${starter.companion.name} · ${starter.localizedTitle(localeTag)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        if (supportLine.isNotBlank()) {
            Text(
                supportLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Horizontal scrollable lesson path ────────────────────────────────────────

@Composable
private fun HorizontalLessonPath(
    lessons: List<Lesson>,
    currentLessonId: String?,
    completedLessonIds: Set<String>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        lessons.forEachIndexed { index, lesson ->
            val isCompleted = lesson.id in completedLessonIds
            val isCurrent = lesson.id == currentLessonId

            // Connector line (before each node except the first)
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(
                            if (lessons[index - 1].id in completedLessonIds)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(1.dp),
                        ),
                )
            }

            // Node
            val nodeSize = if (isCurrent) 40.dp else 32.dp
            val nodeScale by animateFloatAsState(
                targetValue = if (isCurrent) 1f else 0.85f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "nodeScale$index",
            )
            val nodeBg by animateColorAsState(
                targetValue = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isCurrent -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
                animationSpec = tween(300),
                label = "nodeBg$index",
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(nodeSize)
                        .scale(nodeScale)
                        .clip(CircleShape)
                        .background(nodeBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isCompleted) "✓" else "${index + 1}",
                        style = if (isCurrent) MaterialTheme.typography.labelLarge
                        else MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted || isCurrent) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = when {
                        isCompleted -> stringResource(R.string.learn_path_done)
                        isCurrent -> stringResource(R.string.learn_path_current)
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.secondary
                        else -> Color.Transparent
                    },
                    fontSize = 9.sp,
                )
            }
        }
    }
}

// ── Lesson lore card ──────────────────────────────────────────────────────────

@Composable
private fun LessonLoreCard(lesson: Lesson?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("📖", fontSize = 18.sp)
            Text(
                stringResource(R.string.learn_lore_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
        if (!lesson?.summary.isNullOrBlank()) {
            Text(
                lesson!!.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
        if (!lesson?.concept.isNullOrBlank()) {
            Text(
                lesson!!.concept,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
            )
        }
        if (!lesson?.keyTakeaway.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("💡", fontSize = 14.sp)
                Text(
                    lesson!!.keyTakeaway,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Quiz challenge section ────────────────────────────────────────────────────

@Composable
private fun QuizChallengeSection(
    quizType: QuizType?,
    prompt: String,
    options: List<String>,
    selectedAnswerIndex: Int,
    evaluatedState: LearnUiState,
    onSelectAnswer: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section header
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🎯", fontSize = 16.sp)
            Text(
                stringResource(R.string.learn_challenge_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = when (quizType) {
                    QuizType.TRUE_FALSE -> stringResource(R.string.quiz_type_true_false)
                    QuizType.SCENARIO_CHOICE -> stringResource(R.string.quiz_type_scenario_choice)
                    else -> stringResource(R.string.quiz_type_multiple_choice)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Speech-bubble style prompt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(GreenBuddyColors.companionBubble)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.shapes.extraLarge)
                .padding(16.dp),
        ) {
            Text(
                prompt,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Answer options
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

// ── Reward pills row ──────────────────────────────────────────────────────────

@Composable
private fun RewardPillsRow(
    rewardXp: Int,
    alreadyCompleted: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RewardPill(
            text = stringResource(R.string.learn_xp_earn, rewardXp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.secondary,
        )
        RewardPill(
            text = if (alreadyCompleted) stringResource(R.string.reward_already_claimed)
            else stringResource(R.string.answer_to_unlock),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RewardPill(text: String, containerColor: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = contentColor, fontWeight = FontWeight.SemiBold)
    }
}

// ── Track complete card ───────────────────────────────────────────────────────

@Composable
private fun TrackCompleteCard(
    starter: StarterPlantOption,
    localeTag: String,
    dialogueLine: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("🎉", fontSize = 40.sp)
        Text(
            stringResource(R.string.you_did_it),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.track_complete_message, starter.localizedTitle(localeTag)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.next_greenhouse_unlock),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
        )
        Text(
            dialogueLine,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
    }
}

// ── Healing bottom bar ────────────────────────────────────────────────────────

@Composable
private fun HealingLearnBottomBar(
    uiState: LearnUiState,
    feedbackMessage: String?,
    buttonEnabled: Boolean,
    onPrimaryAction: () -> Unit,
) {
    val barColor by animateColorAsState(
        targetValue = when (uiState) {
            LearnUiState.EVALUATED_CORRECT -> MaterialTheme.colorScheme.primaryContainer
            LearnUiState.EVALUATED_INCORRECT -> MaterialTheme.colorScheme.errorContainer
            LearnUiState.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
            LearnUiState.IDLE -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(400),
        label = "bottomBarColor",
    )
    val primaryButtonColor by animateColorAsState(
        targetValue = when (uiState) {
            LearnUiState.EVALUATED_CORRECT, LearnUiState.COMPLETED -> MaterialTheme.colorScheme.primary
            LearnUiState.EVALUATED_INCORRECT -> MaterialTheme.colorScheme.error
            LearnUiState.IDLE -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(400),
        label = "bottomButtonColor",
    )

    val statusLabel = when (uiState) {
        LearnUiState.EVALUATED_CORRECT -> stringResource(R.string.learn_status_complete)
        LearnUiState.EVALUATED_INCORRECT -> stringResource(R.string.learn_status_retry)
        LearnUiState.COMPLETED -> stringResource(R.string.you_did_it)
        LearnUiState.IDLE -> stringResource(R.string.learn_status_ready)
    }
    val primaryLabel = when (uiState) {
        LearnUiState.EVALUATED_CORRECT -> stringResource(R.string.learn_continue)
        LearnUiState.COMPLETED -> stringResource(R.string.lesson_completed)
        else -> stringResource(R.string.check_answer)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(barColor)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        feedbackMessage?.let { msg ->
            Text(
                msg,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = when (uiState) {
                    LearnUiState.EVALUATED_CORRECT, LearnUiState.COMPLETED -> MaterialTheme.colorScheme.primary
                    LearnUiState.EVALUATED_INCORRECT -> MaterialTheme.colorScheme.error
                    LearnUiState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.weight(1f),
                shape = CircleShape,
            ) {
                Text(statusLabel, style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = onPrimaryAction,
                enabled = buttonEnabled,
                modifier = Modifier.weight(2f),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = primaryButtonColor),
            ) {
                Text(primaryLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun companionEmoji(emotion: CompanionEmotion): String = when (emotion) {
    CompanionEmotion.PROUD -> "🌟"
    CompanionEmotion.WORRIED -> "🌧️"
    CompanionEmotion.CURIOUS -> "🌱"
    CompanionEmotion.CALM -> "🍃"
    CompanionEmotion.EXCITED -> "✨"
}
