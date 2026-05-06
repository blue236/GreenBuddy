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
import androidx.compose.ui.platform.LocalContext
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

private enum class LearnUiState { IDLE, EVALUATED_CORRECT, EVALUATED_INCORRECT, COMPLETED }

private const val LORE_PREFS_NAME = "learn_lore_prefs"
private const val LORE_DISMISSED_PREFIX = "lore_dismissed_"

private fun readLoreDismissedDate(context: android.content.Context, lessonKey: String): String? =
    context.getSharedPreferences(LORE_PREFS_NAME, android.content.Context.MODE_PRIVATE)
        .getString("$LORE_DISMISSED_PREFIX$lessonKey", null)

private fun persistLoreDismissedDate(context: android.content.Context, lessonKey: String, date: String) {
    context.getSharedPreferences(LORE_PREFS_NAME, android.content.Context.MODE_PRIVATE)
        .edit()
        .putString("$LORE_DISMISSED_PREFIX$lessonKey", date)
        .apply()
}

private fun clearLoreDismissedDate(context: android.content.Context, lessonKey: String) {
    context.getSharedPreferences(LORE_PREFS_NAME, android.content.Context.MODE_PRIVATE)
        .edit()
        .remove("$LORE_DISMISSED_PREFIX$lessonKey")
        .apply()
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
    val context = LocalContext.current
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val lesson = progress.currentLessonOrNull(lessons)
    val allLessonsComplete = progress.isComplete(lessons)
    val dialogue = CompanionPersonalitySystem.dialogueFor(starter, careState, progress, lessons, localeTag)

    // Group lessons into daily sessions (4 per day)
    val dailySessions = remember(lessons) { if (lessons.isEmpty()) emptyList() else lessons.chunked(4) }

    // Find the current (first incomplete) session
    val currentSessionIndex = dailySessions.indexOfFirst { session ->
        session.any { it.id !in progress.completedLessonIds }
    }.takeIf { it >= 0 } ?: (dailySessions.size - 1).coerceAtLeast(0)
    val currentSession = dailySessions.getOrNull(currentSessionIndex) ?: emptyList()
    val currentSessionKey = currentSession.firstOrNull()?.id ?: "session_$currentSessionIndex"
    // Only pass uncompleted lessons into the quiz flow so the dialog starts at the right question
    val currentSessionQuizLessons = currentSession.filter { it.id !in progress.completedLessonIds }

    val progressValue = if (lessons.isEmpty()) 1f
    else (progress.completedCount.toFloat() / lessons.size.toFloat()).coerceIn(0f, 1f)

    // Lore popup state
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var lorePopupDismissed by remember(currentSessionKey, today) {
        mutableStateOf(readLoreDismissedDate(context, currentSessionKey) == today)
    }
    // Tracks the checkbox inside the lore popup; scoped to current session so it doesn't reset per question
    var dontShowAgainChecked by remember(currentSessionKey) { mutableStateOf(false) }
    val showLorePopup = !lorePopupDismissed && lesson != null && !allLessonsComplete

    // Quiz dialog state is scoped to the current daily session, not the current lesson.
    // That keeps the popup open while progressing through all questions in the session.
    var showQuizDialog by remember(currentSessionKey) { mutableStateOf(false) }
    var activeSessionLessons by remember(currentSessionKey) { mutableStateOf(currentSessionQuizLessons) }

    // ── Lore popup — auto-shows on screen entry ────────────────────────────
    if (showLorePopup) {
        LessonLoreDialog(
            lesson = lesson,
            dontShowAgainChecked = dontShowAgainChecked,
            onDontShowAgainChange = { dontShowAgainChecked = it },
            onDismiss = {
                lorePopupDismissed = true
                if (dontShowAgainChecked) {
                    persistLoreDismissedDate(context, currentSessionKey, today)
                } else {
                    clearLoreDismissedDate(context, currentSessionKey)
                }
                // reset so re-entering after a different dismiss path starts fresh
                dontShowAgainChecked = false
            },
        )
    }

    // ── Quiz popup — opens when tapping the current daily session node ─────
    if (showQuizDialog) {
        QuizDialog(
            sessionLessons = activeSessionLessons,
            onSubmitAnswer = onSubmitAnswer,
            onDismiss = { showQuizDialog = false },
        )
    }

    // ── Main screen ─────────────────────────────────────────────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        DuolingoLearnTopBar(
            progressValue = progressValue,
            completedCount = progress.completedCount,
            totalCount = lessons.size,
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
                companionEmotion = CompanionEmotion.CALM,
                supportLine = dialogue.lessonNudge,
            )

            SectionBanner(
                trackName = starter.localizedTitle(localeTag),
                unitLabel = stringResource(R.string.starter_focus),
            )

            DuolingoVerticalLessonPath(
                dailySessions = dailySessions,
                completedLessonIds = progress.completedLessonIds,
                onCurrentNodeTap = {
                    activeSessionLessons = currentSessionQuizLessons
                    showQuizDialog = true
                },
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
    completedCount: Int,
    totalCount: Int,
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
            stringResource(R.string.learn_lesson_of, completedCount, totalCount),
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
        CompanionAvatarBubble(emoji = companionEmoji(companionEmotion), emotion = companionEmotion, size = 80)
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

// ── Section banner ────────────────────────────────────────────────────────────

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
            Text(trackName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("📋", fontSize = 20.sp)
        }
    }
}

// ── Duolingo-style vertical daily path ───────────────────────────────────────
// One node per daily session (group of 4 lessons). Fewer nodes = cleaner path.

@Composable
private fun DuolingoVerticalLessonPath(
    dailySessions: List<List<Lesson>>,
    completedLessonIds: Set<String>,
    onCurrentNodeTap: () -> Unit,
) {
    val nodeSize = 68.dp
    val xFractions = listOf(0.05f, 0.30f, 0.55f, 0.80f, 0.95f, 0.75f, 0.50f, 0.25f)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val availableX = maxWidth - nodeSize

        Column(modifier = Modifier.fillMaxWidth()) {
            dailySessions.forEachIndexed { dayIndex, sessionLessons ->
                val isCompleted = sessionLessons.all { it.id in completedLessonIds }
                val isCurrent = !isCompleted && (dayIndex == 0 ||
                    dailySessions[dayIndex - 1].all { it.id in completedLessonIds })
                val isNextUp = !isCompleted && !isCurrent &&
                    dayIndex > 0 && dailySessions[dayIndex - 1].all { it.id in completedLessonIds }

                val xFrac = xFractions[dayIndex % xFractions.size]
                val nodeX = availableX * xFrac

                val nodeScale by animateFloatAsState(
                    targetValue = if (isCurrent) 1.14f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "dayNodeScale$dayIndex",
                )
                val nodeBg by animateColorAsState(
                    targetValue = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.secondary
                        isNextUp -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(300),
                    label = "dayNodeBg$dayIndex",
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isCurrent) MaterialTheme.colorScheme.secondary else Color.Transparent,
                    animationSpec = tween(300),
                    label = "dayNodeBorder$dayIndex",
                )

                // Connector dots between adjacent nodes
                if (dayIndex > 0) {
                    val prevXFrac = xFractions[(dayIndex - 1) % xFractions.size]
                    val prevX = availableX * prevXFrac
                    val dotX = (prevX + nodeX) / 2 + nodeSize / 2 - 3.dp
                    val prevCompleted = dailySessions[dayIndex - 1].all { it.id in completedLessonIds }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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
                                    elevation = if (isCurrent) 10.dp else if (isCompleted) 4.dp else 1.dp,
                                    shape = CircleShape,
                                )
                                .scale(nodeScale)
                                .clip(CircleShape)
                                .background(nodeBg)
                                .border(3.dp, borderColor, CircleShape)
                                .then(if (isCurrent) Modifier.clickable(onClick = onCurrentNodeTap) else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                isCompleted -> Text(
                                    "✓",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                isCurrent -> Text("📗", fontSize = 26.sp)
                                else -> Text("🔒", fontSize = 22.sp)
                            }
                        }

                        // Day label below node
                        Text(
                            stringResource(R.string.learn_day_label, dayIndex + 1),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.secondary
                                isCompleted -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            },
                            fontSize = 10.sp,
                        )

                        // "Tap to start" hint only for current node
                        if (isCurrent) {
                            Text(
                                stringResource(R.string.learn_node_tap_hint),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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

                // "Don't show again today" — Row handles click; Checkbox is display-only (onCheckedChange=null)
                // to prevent double-toggle when both Row.clickable and Checkbox fire on the same tap.
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
                        onCheckedChange = null, // Row.clickable owns the toggle; null prevents double-fire
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    )
                    Text(
                        stringResource(R.string.learn_lore_dont_show_today),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

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

// ── Quiz popup dialog — sequential multi-question flow ────────────────────────

@Composable
private fun QuizDialog(
    sessionLessons: List<Lesson>, // uncompleted lessons in the current daily session
    onSubmitAnswer: (Int) -> Boolean,
    onDismiss: () -> Unit,
) {
    // Which question within the session we're on (0-based)
    var questionIndex by rememberSaveable { mutableIntStateOf(0) }
    // Flips to true after the last question is answered correctly
    var showCompletion by rememberSaveable { mutableStateOf(sessionLessons.isEmpty()) }

    val lesson = sessionLessons.getOrNull(questionIndex)
    val totalQuestions = sessionLessons.size

    // Per-question quiz state — reset when question advances
    var selectedAnswerIndex by rememberSaveable(questionIndex) { mutableIntStateOf(-1) }
    var learnUiState by rememberSaveable(questionIndex) { mutableStateOf(LearnUiState.IDLE) }
    var feedbackMessage by rememberSaveable(questionIndex) { mutableStateOf<String?>(null) }

    val pickAnswerFirstText = stringResource(R.string.pick_answer_first)
    val tryAgainText = stringResource(R.string.learn_try_again)
    val correctExclaimText = stringResource(R.string.learn_correct_exclaim)

    fun handleAction() {
        when (learnUiState) {
            LearnUiState.EVALUATED_CORRECT -> {
                if (questionIndex + 1 < totalQuestions) {
                    questionIndex++
                } else {
                    showCompletion = true
                }
            }
            LearnUiState.COMPLETED -> onDismiss()
            else -> {
                if (selectedAnswerIndex < 0) {
                    feedbackMessage = pickAnswerFirstText
                    return
                }
                val isCorrect = onSubmitAnswer(selectedAnswerIndex)
                if (isCorrect) {
                    learnUiState = LearnUiState.EVALUATED_CORRECT
                    feedbackMessage = correctExclaimText
                } else {
                    learnUiState = LearnUiState.EVALUATED_INCORRECT
                    feedbackMessage = tryAgainText
                }
            }
        }
    }

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
            if (showCompletion) {
                SessionCompleteContent(questionCount = totalQuestions, onClose = onDismiss)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ── Dialog top bar ─────────────────────────────────────
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

                    // ── Question progress bar ──────────────────────────────
                    if (totalQuestions > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.learn_lesson_of, questionIndex + 1, totalQuestions),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            LinearProgressIndicator(
                                progress = { (questionIndex + 1).toFloat() / totalQuestions.toFloat() },
                                modifier = Modifier.weight(1f).height(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer,
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    }

                    // ── Scrollable quiz content ────────────────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        QuizChallengeSection(
                            quizType = lesson?.quiz?.type,
                            prompt = lesson?.quiz?.prompt.orEmpty(),
                            options = lesson?.quiz?.options.orEmpty(),
                            selectedAnswerIndex = selectedAnswerIndex,
                            evaluatedState = learnUiState,
                            onSelectAnswer = { index -> selectedAnswerIndex = index; feedbackMessage = null },
                        )
                        RewardPillsRow(
                            rewardXp = lesson?.rewardXp ?: 0,
                            rewardLabel = lesson?.rewardLabel.orEmpty(),
                            alreadyCompleted = false,
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    // ── Bottom action bar ──────────────────────────────────
                    QuizDialogBottomBar(
                        uiState = learnUiState,
                        feedbackMessage = feedbackMessage,
                        isLastQuestion = questionIndex + 1 >= totalQuestions,
                        onAction = ::handleAction,
                    )
                }
            }
        }
    }
}

// ── Session complete content ──────────────────────────────────────────────────

@Composable
private fun SessionCompleteContent(questionCount: Int, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎉", fontSize = 72.sp)
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.you_did_it),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.learn_session_complete_message, questionCount),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.next_greenhouse_unlock),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(stringResource(R.string.learn_continue), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Quiz dialog bottom bar ────────────────────────────────────────────────────

@Composable
private fun QuizDialogBottomBar(
    uiState: LearnUiState,
    feedbackMessage: String?,
    isLastQuestion: Boolean,
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
        targetValue = if (uiState == LearnUiState.EVALUATED_INCORRECT) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        animationSpec = tween(400),
        label = "quizButtonColor",
    )
    val primaryLabel = when (uiState) {
        LearnUiState.EVALUATED_CORRECT -> if (isLastQuestion) stringResource(R.string.learn_continue) else stringResource(R.string.learn_continue)
        LearnUiState.COMPLETED -> stringResource(R.string.lesson_completed)
        else -> stringResource(R.string.check_answer)
    }

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
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = MaterialTheme.colorScheme.onPrimary),
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
private fun RewardPillsRow(rewardXp: Int, rewardLabel: String, alreadyCompleted: Boolean) {
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
private fun TrackCompleteCard(starter: StarterPlantOption, localeTag: String, dialogueLine: String) {
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
