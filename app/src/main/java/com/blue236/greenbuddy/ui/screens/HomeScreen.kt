package com.blue236.greenbuddy.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CareStatType
import com.blue236.greenbuddy.model.CompanionEmotion
import com.blue236.greenbuddy.model.CompanionHomeCheckIn
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
import com.blue236.greenbuddy.model.CompanionStateSnapshot
import com.blue236.greenbuddy.model.DailyMission
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.DailyMissionType
import com.blue236.greenbuddy.model.GrowthStageState
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.WeatherAdvice
import com.blue236.greenbuddy.model.WeatherSnapshot
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.model.localizedGrowthTitle
import com.blue236.greenbuddy.model.localizedLabel
import com.blue236.greenbuddy.model.localizedUnlockHint
import com.blue236.greenbuddy.ui.components.CareActionButton
import com.blue236.greenbuddy.ui.components.CompanionAvatarBubble
import com.blue236.greenbuddy.ui.components.EmotionBanner
import com.blue236.greenbuddy.ui.components.GreenBuddyHeroCard
import com.blue236.greenbuddy.ui.components.LeafTokenDisplay
import com.blue236.greenbuddy.ui.components.StatCard
import com.blue236.greenbuddy.ui.components.StreakBadge
import com.blue236.greenbuddy.ui.theme.GreenBuddyColors

private enum class HomeSection { CARE, MISSIONS, LESSON }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    starter: StarterPlantOption,
    lessons: List<Lesson>,
    progress: LessonProgress,
    careState: PlantCareState,
    dailyMissionSet: DailyMissionSet? = null,
    growthStageState: GrowthStageState,
    greenhouseCount: Int,
    rewardState: RewardState,
    rewardFeedback: String?,
    realPlantModeState: RealPlantModeState,
    weatherSnapshot: WeatherSnapshot,
    weatherAdvice: WeatherAdvice,
    companionStateSnapshot: CompanionStateSnapshot,
    companionHomeCheckIn: CompanionHomeCheckIn,
    onPerformCareAction: (CareAction) -> Unit,
    onSubmitCompanionChatMessage: (String) -> Unit,
    onAcknowledgeGrowthStage: () -> Unit,
    onSetRealPlantModeEnabled: (Boolean) -> Unit,
    onLogRealPlantCare: (RealPlantCareAction) -> Unit,
    onOpenTodayLesson: () -> Unit,
) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val dialogue = CompanionPersonalitySystem.dialogueFor(starter, careState, progress, lessons, localeTag)
    val currentLesson = progress.currentLessonOrNull(lessons)

    var selectedSection by rememberSaveable { mutableStateOf<HomeSection?>(null) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top strip: title + wallet
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.greenhouse_size, starter.companion.name, greenhouseCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LeafTokenDisplay(amount = rewardState.leafTokens)
        }

        // Compact companion hero card
        CompactHomeHeroCard(
            starter = starter,
            companionHomeCheckIn = companionHomeCheckIn,
            headline = dialogue.headline,
            supportLine = dialogue.line,
        )

        // Plant status cards — 3 stat slots + growth
        PlantStatusSection(careState = careState, growthStageState = growthStageState, localeTag = localeTag)

        // Section icon bar
        HomeSectionIconBar(
            selectedSection = selectedSection,
            hasMissions = dailyMissionSet != null,
            onSelectSection = { section ->
                selectedSection = if (selectedSection == section) null else section
            },
        )

        // Selected section detail panel
        AnimatedVisibility(
            visible = selectedSection != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            when (selectedSection) {
                HomeSection.CARE -> CarePanel(
                    careState = careState,
                    careGuidance = dialogue.careGuidance,
                    localeTag = localeTag,
                    onPerformCareAction = onPerformCareAction,
                )
                HomeSection.MISSIONS -> dailyMissionSet?.let { missionSet ->
                    DailyMissionCard(missionSet = missionSet, localeTag = localeTag)
                }
                HomeSection.LESSON -> TodaysLessonPrimaryCard(
                    currentLesson = currentLesson,
                    lessonComplete = progress.isComplete(lessons),
                    lessonNudge = dialogue.lessonNudge,
                    onOpenTodayLesson = onOpenTodayLesson,
                )
                null -> Spacer(Modifier.height(0.dp))
            }
        }
    }
}

// ── Compact companion hero card ──────────────────────────────────────────────

@Composable
private fun CompactHomeHeroCard(
    starter: StarterPlantOption,
    companionHomeCheckIn: CompanionHomeCheckIn,
    headline: String,
    supportLine: String,
) {
    GreenBuddyHeroCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompanionAvatarBubble(
                emoji = companionEmotionEmoji(companionHomeCheckIn.emotion),
                emotion = companionHomeCheckIn.emotion,
                size = 64,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    supportLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
        EmotionBanner(
            emotion = companionHomeCheckIn.emotion,
            emotionLabel = companionHomeCheckIn.emotionLabel,
            familiarityLabel = companionHomeCheckIn.familiarityLabel,
        )
    }
}

// ── Plant status section ─────────────────────────────────────────────────────

@Composable
private fun PlantStatusSection(
    careState: PlantCareState,
    growthStageState: GrowthStageState,
    localeTag: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlantStatMiniCard(
                emoji = "💧",
                label = stringResource(R.string.home_stat_water),
                value = careState.hydration,
                modifier = Modifier.weight(1f),
            )
            PlantStatMiniCard(
                emoji = "☀️",
                label = stringResource(R.string.home_stat_sun),
                value = careState.sunlight,
                modifier = Modifier.weight(1f),
            )
            PlantStatMiniCard(
                emoji = "🌿",
                label = stringResource(R.string.home_stat_feed),
                value = careState.nutrition,
                modifier = Modifier.weight(1f),
            )
        }
        GrowthMiniCard(growthStageState = growthStageState, localeTag = localeTag)
    }
}

@Composable
private fun PlantStatMiniCard(
    emoji: String,
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val barColor = when {
        value >= 65 -> MaterialTheme.colorScheme.primary
        value >= 35 -> GreenBuddyColors.leafGold
        else -> MaterialTheme.colorScheme.error
    }
    val containerColor = when {
        value >= 65 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        value >= 35 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(
            stringResource(R.string.home_stat_percent, value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = barColor,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = barColor,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun GrowthMiniCard(
    growthStageState: GrowthStageState,
    localeTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(growthStageState.currentStage.emoji, fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(
                    R.string.home_growth_mini,
                    growthStageState.currentStage.localizedGrowthTitle(localeTag),
                    growthStageState.readinessPercent,
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            LinearProgressIndicator(
                progress = { growthStageState.progressToNextStage },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
        if (growthStageState.nextStage != null) {
            Text(
                "${growthStageState.readinessPercent}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

// ── Section icon bar ─────────────────────────────────────────────────────────

@Composable
private fun HomeSectionIconBar(
    selectedSection: HomeSection?,
    hasMissions: Boolean,
    onSelectSection: (HomeSection) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionIconButton(
            icon = Icons.Outlined.WaterDrop,
            label = stringResource(R.string.home_section_care),
            selected = selectedSection == HomeSection.CARE,
            onClick = { onSelectSection(HomeSection.CARE) },
        )
        SectionIconButton(
            icon = Icons.Outlined.TaskAlt,
            label = stringResource(R.string.home_section_missions),
            selected = selectedSection == HomeSection.MISSIONS,
            enabled = hasMissions,
            onClick = { onSelectSection(HomeSection.MISSIONS) },
        )
        SectionIconButton(
            icon = Icons.Outlined.MenuBook,
            label = stringResource(R.string.home_section_lesson),
            selected = selectedSection == HomeSection.LESSON,
            onClick = { onSelectSection(HomeSection.LESSON) },
        )
        SectionIconButton(
            icon = Icons.Outlined.Chat,
            label = stringResource(R.string.home_section_chat),
            selected = false,
            enabled = false,
            onClick = {},
        )
        SectionIconButton(
            icon = Icons.Outlined.Spa,
            label = stringResource(R.string.home_section_real_plant),
            selected = false,
            enabled = false,
            onClick = {},
        )
    }
}

@Composable
private fun SectionIconButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        animationSpec = tween(200),
        label = "sectionIconBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "sectionIconColor",
    )

    Column(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.38f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = contentColor,
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

// ── Detail panels ─────────────────────────────────────────────────────────────

@Composable
private fun CarePanel(
    careState: PlantCareState,
    careGuidance: String,
    localeTag: String,
    onPerformCareAction: (CareAction) -> Unit,
) {
    StatCard(stringResource(R.string.care_actions)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CareAction.entries.forEach { action ->
                CareActionButton(
                    action = action,
                    emoji = careActionEmoji(action),
                    label = action.localizedLabel(localeTag),
                    onClick = { onPerformCareAction(action) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Text(
            careGuidance,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun TodaysLessonPrimaryCard(
    currentLesson: Lesson?,
    lessonComplete: Boolean,
    lessonNudge: String,
    onOpenTodayLesson: () -> Unit,
) {
    StatCard(stringResource(R.string.todays_lesson)) {
        Text(
            text = if (lessonComplete) stringResource(R.string.track_complete) else currentLesson?.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(lessonNudge, color = MaterialTheme.colorScheme.primary)
        if (!lessonComplete) {
            Button(onClick = onOpenTodayLesson, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.home_best_action_cta_lesson))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyMissionCard(
    missionSet: DailyMissionSet,
    localeTag: String,
) {
    val progressArcText = if (!missionSet.allCompletedToday && missionSet.completedCount == missionSet.totalCount - 1) {
        stringResource(R.string.daily_mission_progress_arc_one_left, missionSet.completedCount, missionSet.totalCount)
    } else {
        stringResource(R.string.daily_mission_progress_arc_general, missionSet.completedCount, missionSet.totalCount)
    }
    StatCard(
        title = stringResource(R.string.daily_missions),
        trailingContent = { StreakBadge(streakCount = missionSet.currentStreak) },
    ) {
        Text(progressArcText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(
            progress = { missionSet.completedCount.toFloat() / missionSet.totalCount.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            strokeCap = StrokeCap.Round,
        )

        val firstIncompleteMissionId = missionSet.missions.firstOrNull { !it.isCompleted }?.id
        missionSet.missions.forEach { mission ->
            MissionChecklistRow(
                mission = mission,
                localeTag = localeTag,
                rewardTokens = missionRewardTokens(missionSet),
                emphasizeNext = mission.id == firstIncompleteMissionId,
                showRewardCue = mission.id == firstIncompleteMissionId,
            )
        }

        val summaryRes = when {
            missionSet.allCompletedToday && missionSet.pendingStreakReward -> R.string.daily_mission_reward_summary_with_streak
            missionSet.allCompletedToday -> R.string.daily_mission_reward_summary_complete
            else -> R.string.daily_mission_reward_summary_incomplete
        }
        Text(
            stringResource(summaryRes, missionSet.dailyRewardTokens, missionSet.streakRewardTokens, DailyMissionSet.STREAK_REWARD_EVERY_DAYS),
            color = MaterialTheme.colorScheme.primary,
        )

        if (missionSet.pendingStreakReward) {
            Text(
                stringResource(R.string.daily_mission_streak_milestone, missionSet.currentStreak, missionSet.streakRewardTokens),
                fontWeight = FontWeight.SemiBold,
            )
        } else if (missionSet.streakWasRecentlyBroken) {
            Text(
                stringResource(R.string.daily_mission_streak_reset_gentle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun MissionChecklistRow(
    mission: DailyMission,
    localeTag: String,
    rewardTokens: Int,
    emphasizeNext: Boolean,
    showRewardCue: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (emphasizeNext && !mission.isCompleted) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                else Color.Transparent,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
                .background(
                    color = if (mission.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (mission.isCompleted) "✓" else missionIconFor(mission),
                color = if (mission.isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(localizedMissionTitle(mission, localeTag), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (showRewardCue) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.daily_mission_reward_chip, rewardTokens)) })
                }
            }
            Text(localizedMissionDescription(mission, localeTag), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun localizedMissionTitle(mission: DailyMission, localeTag: String): String = when (mission.type) {
    DailyMissionType.COMPLETE_LESSON -> stringResource(R.string.mission_complete_lesson_title)
    DailyMissionType.PERFORM_CARE_ACTION -> stringResource(R.string.mission_care_action_title)
    DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD -> stringResource(R.string.mission_keep_stat_title, mission.statType.localizedStatLabel(localeTag), mission.threshold ?: 0)
}

@Composable
private fun localizedMissionDescription(mission: DailyMission, localeTag: String): String = when (mission.type) {
    DailyMissionType.COMPLETE_LESSON -> stringResource(R.string.mission_complete_lesson_description)
    DailyMissionType.PERFORM_CARE_ACTION -> stringResource(R.string.mission_care_action_description)
    DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD -> stringResource(R.string.mission_keep_stat_description, mission.statType.localizedStatLabel(localeTag), mission.threshold ?: 0)
}

private fun missionRewardTokens(missionSet: DailyMissionSet): Int =
    (missionSet.dailyRewardTokens / missionSet.totalCount).coerceAtLeast(1)

private fun missionIconFor(mission: DailyMission): String = when (mission.type) {
    DailyMissionType.COMPLETE_LESSON -> "📘"
    DailyMissionType.PERFORM_CARE_ACTION -> "💧"
    DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD -> "☀️"
}

private fun companionEmotionEmoji(emotion: CompanionEmotion): String = when (emotion) {
    CompanionEmotion.PROUD -> "🌟"
    CompanionEmotion.WORRIED -> "🌧️"
    CompanionEmotion.CURIOUS -> "🌱"
    CompanionEmotion.CALM -> "🍃"
    CompanionEmotion.EXCITED -> "✨"
}

private fun careActionEmoji(action: CareAction): String = when (action) {
    CareAction.WATER -> "💧"
    CareAction.MOVE_TO_SUNLIGHT -> "☀️"
    CareAction.FERTILIZE -> "🌿"
}

private fun CareStatType?.localizedStatLabel(localeTag: String): String = when (this) {
    CareStatType.HYDRATION -> when {
        localeTag.startsWith("ko") -> "수분"
        localeTag.startsWith("de") -> "Wasser"
        else -> "Hydration"
    }
    CareStatType.SUNLIGHT -> when {
        localeTag.startsWith("ko") -> "햇빛"
        localeTag.startsWith("de") -> "Sonnenlicht"
        else -> "Sunlight"
    }
    CareStatType.NUTRITION -> when {
        localeTag.startsWith("ko") -> "영양"
        localeTag.startsWith("de") -> "Nährstoffe"
        else -> "Nutrition"
    }
    null -> ""
}
