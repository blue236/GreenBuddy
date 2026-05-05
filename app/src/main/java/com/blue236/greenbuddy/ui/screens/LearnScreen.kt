package com.blue236.greenbuddy.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.blue236.greenbuddy.ui.components.QuizOptionState
import com.blue236.greenbuddy.ui.components.QuizOptionTile
import com.blue236.greenbuddy.ui.theme.GreenBuddyColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// In-memory store for "don't show lore today" — persists across tab switches within the app session
private val loreDismissedForDate = mutableMapOf<String, String>() // lessonKey -> date string

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
    leafTokens: Int = 0,
    currentStreak: Int = 0,
    onSubmitAnswer: (Int) -> Boolean,
) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val lesson = progress.currentLessonOrNull(lessons)
    val allLessonsComplete = progress.isComplete(lessons)
    val alreadyCompleted = lesson?.id in progress.completedLessonIds
    val lessonKey = lesson?.id ?: "track_complete"
    val dialogue = CompanionPersonalitySystem.dialogueFor(starter, careState, progress, lessons, localeTag)
    val lessonIndex = lesson?.let { current ->
        lessons.indexOfFirst { it.id == current.id }.takeIf { it >= 0 }?.plus(1)
    } ?: lessons.size
    val progressValue = if (lessons.isEmpty()) 1f
    else (progress.completedCount.coerceAtLeast(0).toFloat() / lessons.size.toFloat()).coerceIn(0f, 1f)

    // Quiz state
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

    val companionEmotion = when (learnUiState) {
        LearnUiState.EVALUATED_CORRECT, LearnUiState.COMPLETED -> CompanionEmotion.PROUD
        LearnUiState.EVALUATED_INCORRECT -> CompanionEmotion.CURIOUS
        LearnUiState.IDLE -> CompanionEmotion.CALM
    }

    // Lore popup state
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    // lorePopupDismissed resets when lesson changes; initialises from in-memory map so it survives tab switches
    var lorePopupDismissed by remember(lessonKey) {
        mutableStateOf(loreDismissedForDate[lessonKey] == today)
    }
    var dontShowAgainChecked by remember(lessonKey) { mutableStateOf(false) }
    val showLorePopup = !lorePopupDismissed && lesson != null && !allLessonsComplete

    // Quiz dialog state
    var showQuizDialog by remember(lessonKey) { mutableStateOf(false) }

    // ── Lore popup — auto-shows on screen entry ────────────────────────────
    if (showLorePopup) {
        LessonLoreDialog(
            lesson = lesson,
            dontShowAgainChecked = dontShowAgainChecked,
            onDontShowAgainChange = { dontShowAgainChecked = it },
            onDismiss = {
                lorePopupDismissed = true
                if (dontShowAgainChecked) loreDismissedForDate[lessonKey] = today
                dontShowAgainChecked = false
            },
        )
    }

    // ── Quiz popup — opens when tapping the current lesson node ───────────
    if (showQuizDialog) {
        QuizDialog(
            lesson = lesson,
            selectedAnswerIndex = selectedAnswerIndex,
            learnUiState = learnUiState,
            feedbackMessage = feedbackMessage,
            alreadyCompleted = alreadyCompleted,
            onSelectAnswer = { index ->
                if (!alreadyCompleted) {
                    selectedAnswerIndex = index
                    feedbackMessage = null
                    if (learnUiState != LearnUiState.IDLE) learnUiState = LearnUiState.IDLE
                }
            },
            onSubmit = {
                if (selectedAnswerIndex < 0) {
                    feedbackMessage = pickAnswerFirstText
                    return@QuizDialog
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
            onDismiss = { showQuizDialog = false },
        )
    }

    // ── Main screen: path-focused layout ──────────────────────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        DuolingoLearnTopBar(
            progressValue = progressValue,
            lessonIndex = lessonIndex,
            lessonCount = lessons.size,
            currentStreak = currentStreak,
            leafTokens = leafTokens,
            totalXp = progress.totalXp,
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            CompanionLessonStage(
                starter = starter,
                localeTag = localeTag,
                lessonTitle = if (allLessonsComplete) stringResource(R.string.all_lessons_complete)
                else lesson?.title.orEmpty(),
                companionEmotion = companionEmotion,
                supportLine = dialogue.lessonNudge,
            )

            SectionBanner(
                trackName = starter.localizedTitle(localeTag),
                unitLabel = stringResource(R.string.starter_focus),
            )

            DuolingoVerticalLessonPath(
                lessons = lessons,
                currentLessonId = lesson?.id,
                completedLessonIds = progress.completedLessonIds,
                onCurrentNodeTap = { showQuizDialog = true },
            )

            if (allLessonsComplete) {
                TrackCompleteCard(
                    starter = starter,
                    localeTag = localeTag,
                    dialogueLine = dialogue.line,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Duolingo-style top stat bar ───────────────────────────────────────────────

@Composable
private fun DuolingoLearnTopBar(
    progressValue: Float,
    lessonIndex: Int,
    lessonCount: Int,
    currentStreak: Int,
    leafTokens: Int,
    totalXp: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.learn_lesson_of, lessonIndex.coerceAtLeast(1), lessonCount),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        LinearProgressIndicator(
            progress = { progressValue },
            modifier = Modifier.weight(1f).height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
            strokeCap = StrokeCap.Round,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🔥", fontSize = 16.sp)
            Text("$currentStreak", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = GreenBuddyColors.streakFlame)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🍃", fontSize = 14.sp)
            Text("$leafTokens", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⚡", fontSize = 13.sp)
            Text("$totalXp", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = GreenBuddyColors.leafGold)
        }
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

// ── Section banner (Duolingo-style orange pill) ───────────────────────────────

@Composable
private fun SectionBanner(trackName: String, unitLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(GreenBuddyColors.leafGold)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                unitLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.80f),
                letterSpacing = 1.sp,
            )
            Text(
                trackName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("📋", fontSize = 20.sp)
        }
    }
}

// ── Duolingo-style vertical winding lesson path ───────────────────────────────

@Composable
private fun DuolingoVerticalLessonPath(
    lessons: List<Lesson>,
    currentLessonId: String?,
    completedLessonIds: Set<String>,
    onCurrentNodeTap: () -> Unit,
) {
    val nodeSize = 60.dp
    val xFractions = listOf(0.05f, 0.28f, 0.52f, 0.75f, 0.95f, 0.75f, 0.52f, 0.28f)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val containerW = maxWidth
        val availableX = containerW - nodeSize

        Column(modifier = Modifier.fillMaxWidth()) {
            lessons.forEachIndexed { index, lesson ->
                val isCompleted = lesson.id in completedLessonIds
                val isCurrent = lesson.id == currentLessonId
                val isNextUp = !isCompleted && !isCurrent &&
                    index > 0 && lessons.getOrNull(index - 1)?.id in completedLessonIds

                val xFrac = xFractions[index % xFractions.size]
                val nodeX = availableX * xFrac

                val nodeScale by animateFloatAsState(
                    targetValue = if (isCurrent) 1.12f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "duoNodeScale$index",
                )
                val nodeBg by animateColorAsState(
                    targetValue = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.secondary
                        isNextUp -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(300),
                    label = "duoNodeBg$index",
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isCurrent) MaterialTheme.colorScheme.secondary else Color.Transparent,
                    animationSpec = tween(300),
                    label = "duoNodeBorder$index",
                )

                // Connector dots between adjacent nodes
                if (index > 0) {
                    val prevXFrac = xFractions[(index - 1) % xFractions.size]
                    val prevX = availableX * prevXFrac
                    val dotX = (prevX + nodeX) / 2 + nodeSize / 2 - 3.dp
                    val prevCompleted = lessons.getOrNull(index - 1)?.id in completedLessonIds
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Spacer(Modifier.width(dotX))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(3) {
                                Box(
                                    Modifier.size(5.dp).clip(CircleShape).background(
                                        if (prevCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.outlineVariant,
                                    ),
                                )
                            }
                        }
                    }
                }

                // Node row
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.width(nodeX))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(nodeSize)
                                .shadow(
                                    elevation = if (isCurrent) 8.dp else if (isCompleted) 4.dp else 1.dp,
                                    shape = CircleShape,
                                )
                                .scale(nodeScale)
                                .clip(CircleShape)
                                .background(nodeBg)
                                .border(3.dp, borderColor, CircleShape)
                                .then(
                                    if (isCurrent) Modifier.clickable(onClick = onCurrentNodeTap)
                                    else Modifier,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                isCompleted -> Text("✓", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                isCurrent -> Text("${index + 1}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                                isNextUp -> Text("${index + 1}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                else -> Text("🔒", fontSize = 20.sp)
                            }
                        }
                        // Label below node
                        when {
                            isCurrent -> Text(
                                stringResource(R.string.learn_node_tap_hint),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 9.sp,
                            )
                            isCompleted -> Text(
                                stringResource(R.string.learn_path_done),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Lore popup dialog ─────────────────────────────────────────────────────────

@Composable
private fun LessonLoreDialog(
    lesson: Lesson?,
    dontShowAgainChecked: Boolean,
    onDontShowAgainChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("📖", fontSize = 20.sp)
                        Text(
                            stringResource(R.string.learn_lore_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✕", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Lore content
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!lesson?.summary.isNullOrBlank()) {
                        Text(lesson!!.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!lesson?.concept.isNullOrBlank()) {
                        Text(
                            lesson!!.concept,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!lesson?.keyTakeaway.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Don't show again checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onDontShowAgainChange(!dontShowAgainChecked) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Checkbox(
                        checked = dontShowAgainChecked,
                        onCheckedChange = onDontShowAgainChange,
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    )
                    Text(
                        stringResource(R.string.learn_lore_dont_show_today),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(stringResource(R.string.learn_continue), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// ── Quiz popup dialog ─────────────────────────────────────────────────────────

@Composable
private fun QuizDialog(
    lesson: Lesson?,
    selectedAnswerIndex: Int,
    learnUiState: LearnUiState,
    feedbackMessage: String?,
    alreadyCompleted: Boolean,
    onSelectAnswer: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 12.dp, vertical = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Dialog top bar ─────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✕", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        lesson?.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    )
                    Spacer(Modifier.size(36.dp))
                }

                // ── Scrollable quiz content ────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Quiz challenge
                    QuizChallengeSection(
                        quizType = lesson?.quiz?.type,
                        prompt = lesson?.quiz?.prompt.orEmpty(),
                        options = lesson?.quiz?.options.orEmpty(),
                        selectedAnswerIndex = selectedAnswerIndex,
                        evaluatedState = learnUiState,
                        onSelectAnswer = onSelectAnswer,
                    )

                    // Reward pills
                    RewardPillsRow(
                        rewardXp = lesson?.rewardXp ?: 0,
                        rewardLabel = lesson?.rewardLabel.orEmpty(),
                        alreadyCompleted = alreadyCompleted,
                    )

                    Spacer(Modifier.height(4.dp))
                }

                // ── Bottom action bar (embedded in dialog) ─────────────────
                QuizDialogBottomBar(
                    uiState = learnUiState,
                    feedbackMessage = feedbackMessage,
                    buttonEnabled = !alreadyCompleted,
                    onAction = {
                        when (learnUiState) {
                            LearnUiState.EVALUATED_CORRECT, LearnUiState.COMPLETED -> onDismiss()
                            else -> onSubmit()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun QuizDialogBottomBar(
    uiState: LearnUiState,
    feedbackMessage: String?,
    buttonEnabled: Boolean,
    onAction: () -> Unit,
) {
    val barColor by animateColorAsState(
        targetValue = when (uiState) {
            LearnUiState.EVALUATED_CORRECT -> MaterialTheme.colorScheme.primaryContainer
            LearnUiState.EVALUATED_INCORRECT -> MaterialTheme.colorScheme.errorContainer
            LearnUiState.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
            LearnUiState.IDLE -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(400),
        label = "quizBarColor",
    )
    val buttonColor by animateColorAsState(
        targetValue = when (uiState) {
            LearnUiState.EVALUATED_INCORRECT -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(400),
        label = "quizButtonColor",
    )
    val primaryLabel = when (uiState) {
        LearnUiState.EVALUATED_CORRECT -> stringResource(R.string.learn_continue)
        LearnUiState.COMPLETED -> stringResource(R.string.lesson_completed)
        else -> stringResource(R.string.check_answer)
    }
    val isClosingAction = uiState == LearnUiState.EVALUATED_CORRECT || uiState == LearnUiState.COMPLETED

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(barColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!feedbackMessage.isNullOrBlank() && uiState != LearnUiState.IDLE) {
            Text(
                feedbackMessage,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = when (uiState) {
                    LearnUiState.EVALUATED_CORRECT, LearnUiState.COMPLETED -> MaterialTheme.colorScheme.primary
                    LearnUiState.EVALUATED_INCORRECT -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Button(
            onClick = onAction,
            enabled = isClosingAction || buttonEnabled,
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isClosingAction && uiState == LearnUiState.COMPLETED)
                    MaterialTheme.colorScheme.surfaceVariant else buttonColor,
                contentColor = if (isClosingAction && uiState == LearnUiState.COMPLETED)
                    MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(primaryLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
    rewardLabel: String,
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
        if (rewardLabel.isNotBlank()) {
            RewardPill(
                text = rewardLabel,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
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

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun companionEmoji(emotion: CompanionEmotion): String = when (emotion) {
    CompanionEmotion.PROUD -> "🌟"
    CompanionEmotion.WORRIED -> "🌧️"
    CompanionEmotion.CURIOUS -> "🌱"
    CompanionEmotion.CALM -> "🍃"
    CompanionEmotion.EXCITED -> "✨"
}
