package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CareStatType
import com.blue236.greenbuddy.model.CompanionChatEngine
import com.blue236.greenbuddy.model.CompanionConversationMemory
import com.blue236.greenbuddy.model.CompanionEmotion
import com.blue236.greenbuddy.model.CompanionHomeCheckIn
import com.blue236.greenbuddy.model.CompanionMessageRole
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
import com.blue236.greenbuddy.model.localizedGrowthAccentLabel
import com.blue236.greenbuddy.model.localizedGrowthTitle
import com.blue236.greenbuddy.model.localizedHealth
import com.blue236.greenbuddy.model.localizedRequirementSummary
import com.blue236.greenbuddy.model.localizedUnlockHint
import com.blue236.greenbuddy.model.localizedUnlockedMessage
import com.blue236.greenbuddy.model.localizedMood
import com.blue236.greenbuddy.model.localizedLabel
import com.blue236.greenbuddy.model.localizedName
import com.blue236.greenbuddy.ui.components.CareActionButton
import com.blue236.greenbuddy.ui.components.CompanionAvatarBubble
import com.blue236.greenbuddy.ui.components.EmotionBanner
import com.blue236.greenbuddy.ui.components.GreenBuddyButton
import com.blue236.greenbuddy.ui.components.GreenBuddyButtonVariant
import com.blue236.greenbuddy.ui.components.GreenBuddyHeroCard
import com.blue236.greenbuddy.ui.components.LeafTokenDisplay
import com.blue236.greenbuddy.ui.components.MissionRowItem
import com.blue236.greenbuddy.ui.components.SectionTitle
import com.blue236.greenbuddy.ui.components.StatCard
import com.blue236.greenbuddy.ui.components.StreakBadge
import java.time.LocalDate
import java.time.ZoneId

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
    val zoneId = ZoneId.systemDefault()
    val completedToday = realPlantModeState.completedActionsOn(LocalDate.now(zoneId), zoneId)
    val bestNextAction = when {
        dailyMissionSet != null && !dailyMissionSet.allCompletedToday && dailyMissionSet.completedCount == dailyMissionSet.totalCount - 1 ->
            stringResource(R.string.home_best_action_finish_reward)
        careState.lowestStat <= 45 ->
            stringResource(R.string.home_best_action_low_need, careState.lowestNeed.localizedLabel(localeTag))
        currentLesson != null -> dialogue.lessonNudge
        growthStageState.nextStage != null && growthStageState.readinessPercent >= 75 -> growthStageState.localizedUnlockHint(localeTag)
        else -> dialogue.careGuidance
    }
    val bestNextActionLabel = when {
        dailyMissionSet != null && !dailyMissionSet.allCompletedToday -> stringResource(R.string.home_best_action_cta_lesson)
        careState.lowestStat <= 45 -> stringResource(R.string.home_best_action_cta_care, careState.lowestNeed.localizedLabel(localeTag))
        currentLesson != null -> stringResource(R.string.home_best_action_cta_lesson)
        else -> stringResource(R.string.home_best_action_cta_growth)
    }
    var isCompanionChatOpen by rememberSaveable { mutableStateOf(false) }
    var areExtrasExpanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(rewardFeedback) {
        if (rewardFeedback != null) areExtrasExpanded = true
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Top strip: title + wallet
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.greenhouse_size, starter.companion.name, greenhouseCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LeafTokenDisplay(amount = rewardState.leafTokens)
        }

        // Hero companion card — focal point
        HomeHeroCard(
            starter = starter,
            companionHomeCheckIn = companionHomeCheckIn,
            companionStateSnapshot = companionStateSnapshot,
            headline = dialogue.headline,
            supportLine = dialogue.line,
            bestNextAction = bestNextAction,
            bestNextActionLabel = bestNextActionLabel,
            localeTag = localeTag,
            isCompanionChatOpen = isCompanionChatOpen,
            onToggleCompanionChat = { isCompanionChatOpen = !isCompanionChatOpen },
            onSubmitCompanionChatMessage = onSubmitCompanionChatMessage,
            onOpenCompanionPrompt = { prompt ->
                isCompanionChatOpen = true
                onSubmitCompanionChatMessage(prompt)
            },
            onRunBestNextAction = {
                when {
                    careState.lowestStat <= 45 -> onPerformCareAction(careState.lowestNeed)
                    else -> isCompanionChatOpen = true
                }
            },
        )

        // Care actions — 3 tile buttons
        SectionTitle(stringResource(R.string.care_actions))
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
        Text(dialogue.careGuidance, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

        // Daily missions — always visible
        dailyMissionSet?.let { missionSet ->
            SectionTitle(
                stringResource(R.string.daily_missions),
                trailingContent = { StreakBadge(streakCount = missionSet.currentStreak) },
            )
            DailyMissionCard(missionSet = missionSet, localeTag = localeTag)
        }

        // Today's lesson nudge
        SectionTitle(stringResource(R.string.todays_lesson))
        TodaysLessonPrimaryCard(
            currentLesson = currentLesson,
            lessonComplete = progress.isComplete(lessons),
            lessonNudge = dialogue.lessonNudge,
            onOpenTodayLesson = onOpenTodayLesson,
        )

        // Expand/collapse extras
        GreenBuddyButton(
            onClick = { areExtrasExpanded = !areExtrasExpanded },
            text = stringResource(if (areExtrasExpanded) R.string.home_extras_hide else R.string.home_extras_show),
            modifier = Modifier.fillMaxWidth(),
            variant = GreenBuddyButtonVariant.Ghost,
        )
        if (areExtrasExpanded) {
            GrowthOverviewCard(
                growthStageState = growthStageState,
                localeTag = localeTag,
                onAcknowledgeGrowthStage = onAcknowledgeGrowthStage,
            )
            CompactRewardStrip(rewardState = rewardState, localeTag = localeTag)
            StatCard(stringResource(R.string.local_weather_title)) {
                Text(stringResource(R.string.weather_card_city, weatherSnapshot.city.defaultName), fontWeight = FontWeight.SemiBold)
                Text(weatherAdvice.summary)
                Text(weatherAdvice.starterAdvice, color = MaterialTheme.colorScheme.primary)
                Text(weatherAdvice.reminderHint, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatCard(stringResource(R.string.real_plant_mode)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mirror_real_world_care))
                    Switch(checked = realPlantModeState.enabled, onCheckedChange = onSetRealPlantModeEnabled)
                }
                if (realPlantModeState.enabled) {
                    Text(stringResource(R.string.today_real_plant, completedToday.size, RealPlantCareAction.entries.size))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RealPlantCareAction.entries.forEach { AssistChip(onClick = { onLogRealPlantCare(it) }, label = { Text(it.localizedLabel(localeTag)) }) }
                    }
                }
            }
        }
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
private fun HomeHeroCard(
    starter: StarterPlantOption,
    companionHomeCheckIn: CompanionHomeCheckIn,
    companionStateSnapshot: CompanionStateSnapshot,
    headline: String,
    supportLine: String,
    bestNextAction: String,
    bestNextActionLabel: String,
    localeTag: String,
    isCompanionChatOpen: Boolean,
    onToggleCompanionChat: () -> Unit,
    onSubmitCompanionChatMessage: (String) -> Unit,
    onOpenCompanionPrompt: (String) -> Unit,
    onRunBestNextAction: () -> Unit,
) {
    GreenBuddyHeroCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CompanionAvatarBubble(
                emoji = companionEmotionEmoji(companionHomeCheckIn.emotion),
                emotion = companionHomeCheckIn.emotion,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(supportLine, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
        EmotionBanner(
            emotion = companionHomeCheckIn.emotion,
            emotionLabel = companionHomeCheckIn.emotionLabel,
            familiarityLabel = companionHomeCheckIn.familiarityLabel,
        )
        // Best next action box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(bestNextAction, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            GreenBuddyButton(onClick = onRunBestNextAction, text = bestNextActionLabel)
            companionHomeCheckIn.suggestionChips.firstOrNull()?.let { prompt ->
                AssistChip(onClick = { onOpenCompanionPrompt(prompt) }, label = { Text(prompt, style = MaterialTheme.typography.labelMedium) })
            }
        }
        // Chat toggle
        GreenBuddyButton(
            onClick = onToggleCompanionChat,
            text = stringResource(if (isCompanionChatOpen) R.string.companion_chat_hide else R.string.companion_chat_open),
            variant = GreenBuddyButtonVariant.Secondary,
        )
        if (isCompanionChatOpen) {
            Text(
                stringResource(R.string.companion_chat_entry, starter.companion.name),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(companionStateSnapshot.personality.profileLabel, color = MaterialTheme.colorScheme.primary)
            CompanionChatCard(
                companionStateSnapshot = companionStateSnapshot,
                proactiveCheckIn = companionHomeCheckIn,
                languageTag = localeTag,
                onSubmitCompanionChatMessage = onSubmitCompanionChatMessage,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompanionChatCard(
    companionStateSnapshot: CompanionStateSnapshot,
    proactiveCheckIn: CompanionHomeCheckIn,
    languageTag: String,
    onSubmitCompanionChatMessage: (String) -> Unit,
) {
    var draftMessage by rememberSaveable { mutableStateOf("") }
    var optimisticMemory by remember { mutableStateOf<CompanionConversationMemory?>(null) }
    var optimisticSuggestionChips by remember { mutableStateOf<List<String>?>(null) }
    val displayedMemory = optimisticMemory ?: companionStateSnapshot.recentConversationMemory
    val displayedSuggestionChips = optimisticSuggestionChips
        ?: companionStateSnapshot.latestSuggestionChips(languageTag)
        ?: proactiveCheckIn.suggestionChips

    LaunchedEffect(companionStateSnapshot.recentConversationMemory.messages.size) {
        val pending = optimisticMemory ?: return@LaunchedEffect
        if (companionStateSnapshot.recentConversationMemory.messages.size >= pending.messages.size) {
            optimisticMemory = null
            optimisticSuggestionChips = null
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        val reply = CompanionChatEngine.replyTo(
            message = message,
            snapshot = companionStateSnapshot,
            languageTag = languageTag,
        )
        optimisticMemory = CompanionChatEngine.updatedMemoryFor(reply, companionStateSnapshot)
        optimisticSuggestionChips = reply.suggestionChips
        onSubmitCompanionChatMessage(message)
        draftMessage = ""
    }

    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(companionStateSnapshot.personality.homeHeadline, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(
                    R.string.companion_snapshot_summary,
                    companionStateSnapshot.careState.localizedMood(languageTag),
                    companionStateSnapshot.careState.localizedHealth(languageTag),
                    companionStateSnapshot.growthStageState.currentStage.localizedGrowthTitle(languageTag),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(
                    R.string.companion_continuity_summary,
                    companionStateSnapshot.continuity.emotionalSummary,
                    companionStateSnapshot.relationship.summary,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            companionStateSnapshot.realPlantSummary?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ConversationMemoryBlock(
                memory = displayedMemory,
                fallbackBubble = proactiveCheckIn.bubble,
            )
            OutlinedTextField(
                value = draftMessage,
                onValueChange = { newValue: String -> draftMessage = newValue },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.companion_chat_input_label)) },
                placeholder = { Text(text = stringResource(R.string.companion_chat_input_placeholder)) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { sendMessage(draftMessage) }) { Text(stringResource(R.string.companion_chat_send)) }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayedSuggestionChips.forEach { prompt ->
                    AssistChip(onClick = { sendMessage(prompt) }, label = { Text(prompt) })
                }
            }
        }
    }
}

@Composable
private fun ConversationMemoryBlock(
    memory: CompanionConversationMemory,
    fallbackBubble: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (memory.messages.isEmpty()) {
            Text(fallbackBubble)
        } else {
            memory.messages.takeLast(6).forEach { message ->
                val prefixRes = if (message.role == CompanionMessageRole.USER) R.string.companion_label_you else R.string.companion_label_buddy
                Text(
                    text = stringResource(prefixRes) + ": ${message.text}",
                    color = if (message.role == CompanionMessageRole.USER) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrowthOverviewCard(
    growthStageState: GrowthStageState,
    localeTag: String,
    onAcknowledgeGrowthStage: () -> Unit,
) {
    StatCard(stringResource(R.string.home_growth_title)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${growthStageState.currentStage.emoji} ${growthStageState.currentStage.localizedGrowthTitle(localeTag)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    growthStageState.currentStage.localizedGrowthAccentLabel(localeTag),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = { },
                label = { Text(stringResource(R.string.growth_readiness_chip, growthStageState.readinessPercent)) },
            )
        }

        if (growthStageState.newlyUnlocked) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.new_evolution_unlocked, growthStageState.currentStage.localizedGrowthTitle(localeTag)),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(growthStageState.currentStage.localizedUnlockedMessage(localeTag))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onAcknowledgeGrowthStage) { Text(stringResource(R.string.growth_cta_celebrate)) }
                        OutlinedButton(onClick = onAcknowledgeGrowthStage) { Text(stringResource(R.string.home_best_action_cta_growth)) }
                    }
                }
            }
        }

        growthStageState.nextStage?.let { nextStage ->
            LinearProgressIndicator(
                progress = { growthStageState.progressToNextStage },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(stringResource(R.string.growth_next_stage_chip, nextStage.localizedGrowthTitle(localeTag))) },
                )
                AssistChip(
                    onClick = { },
                    label = { Text(growthStageState.localizedRequirementSummary(localeTag)) },
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(growthStageState.localizedUnlockHint(localeTag), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.home_best_action_cta_growth),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } ?: Text(
            stringResource(R.string.growth_final_stage_home, growthStageState.currentStage.localizedGrowthTitle(localeTag)),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactRewardStrip(
    rewardState: RewardState,
    localeTag: String,
) {
    val nextCosmetic = rewardState.nextUnlockableCosmetic
    StatCard(stringResource(R.string.reward_pulse)) {
        Text(stringResource(R.string.wallet_value, rewardState.leafTokens), fontWeight = FontWeight.SemiBold)
        Text(
            stringResource(
                R.string.reward_token_purpose,
                RewardState.lessonTokenReward(24),
                RewardState.careTokenReward(),
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        nextCosmetic?.let { item ->
            val tokensNeeded = rewardState.tokensNeededFor(item)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(stringResource(R.string.reward_next_unlock_chip, item.emoji, item.localizedName(localeTag))) },
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            if (tokensNeeded == 0) {
                                stringResource(R.string.reward_ready_to_buy_chip)
                            } else {
                                stringResource(R.string.reward_tokens_needed_chip, tokensNeeded)
                            },
                        )
                    },
                )
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
    StatCard(stringResource(R.string.daily_missions)) {
        Text(progressArcText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(
            progress = { missionSet.completedCount.toFloat() / missionSet.totalCount.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        ) {
            AssistChip(
                onClick = { },
                label = { Text(stringResource(R.string.completed_of_total, missionSet.completedCount, missionSet.totalCount)) },
            )
            AssistChip(
                onClick = { },
                label = { Text(stringResource(R.string.daily_mission_streak_chip, missionSet.currentStreak)) },
            )
            AssistChip(
                onClick = { },
                label = { Text(stringResource(R.string.daily_mission_reward_chip, missionSet.dailyRewardTokens)) },
            )
            if (missionSet.pendingStreakReward) {
                AssistChip(
                    onClick = { },
                    label = { Text(stringResource(R.string.daily_mission_streak_bonus_chip, missionSet.streakRewardTokens)) },
                )
            }
        }

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
            stringResource(
                summaryRes,
                missionSet.dailyRewardTokens,
                missionSet.streakRewardTokens,
                DailyMissionSet.STREAK_REWARD_EVERY_DAYS,
            ),
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
                color = if (emphasizeNext && !mission.isCompleted) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(localizedMissionTitle(mission, localeTag), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (showRewardCue) {
                    AssistChip(
                        onClick = { },
                        label = { Text(stringResource(R.string.daily_mission_reward_chip, rewardTokens)) },
                    )
                }
            }
            Text(
                localizedMissionDescription(mission, localeTag),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun localizedMissionTitle(mission: DailyMission, localeTag: String): String {
    return when (mission.type) {
        DailyMissionType.COMPLETE_LESSON -> stringResource(R.string.mission_complete_lesson_title)
        DailyMissionType.PERFORM_CARE_ACTION -> stringResource(R.string.mission_care_action_title)
        DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD -> stringResource(
            R.string.mission_keep_stat_title,
            mission.statType.localizedStatLabel(localeTag),
            mission.threshold ?: 0,
        )
    }
}

@Composable
private fun localizedMissionDescription(mission: DailyMission, localeTag: String): String {
    return when (mission.type) {
        DailyMissionType.COMPLETE_LESSON -> stringResource(R.string.mission_complete_lesson_description)
        DailyMissionType.PERFORM_CARE_ACTION -> stringResource(R.string.mission_care_action_description)
        DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD -> stringResource(
            R.string.mission_keep_stat_description,
            mission.statType.localizedStatLabel(localeTag),
            mission.threshold ?: 0,
        )
    }
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

private fun CareStatType?.localizedStatLabel(localeTag: String): String {
    return when (this) {
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RewardOverviewCard(
    rewardState: RewardState,
    rewardFeedback: String?,
    localeTag: String,
) {
    val nextCosmetic = rewardState.nextUnlockableCosmetic
    StatCard(stringResource(R.string.reward_pulse)) {
        Text(stringResource(R.string.wallet_value, rewardState.leafTokens), fontWeight = FontWeight.SemiBold)
        Text(
            stringResource(
                R.string.reward_token_purpose,
                RewardState.lessonTokenReward(24),
                RewardState.careTokenReward(),
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        rewardState.equippedCosmetic?.let { equipped ->
            Text(
                stringResource(
                    R.string.reward_equipped_cosmetic,
                    equipped.emoji,
                    equipped.localizedName(localeTag),
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        nextCosmetic?.let { item ->
            val tokensNeeded = rewardState.tokensNeededFor(item)
            val canAfford = tokensNeeded == 0
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            stringResource(
                                R.string.reward_next_unlock_chip,
                                item.emoji,
                                item.localizedName(localeTag),
                            ),
                        )
                    },
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            if (canAfford) {
                                stringResource(R.string.reward_ready_to_buy_chip)
                            } else {
                                stringResource(R.string.reward_tokens_needed_chip, tokensNeeded)
                            },
                        )
                    },
                )
            }
            Text(
                if (canAfford) {
                    stringResource(R.string.reward_ready_to_buy_summary, item.localizedName(localeTag))
                } else {
                    stringResource(
                        R.string.reward_progress_summary,
                        item.localizedName(localeTag),
                        tokensNeeded,
                    )
                },
                color = MaterialTheme.colorScheme.primary,
            )
        } ?: Text(
            stringResource(R.string.reward_all_cosmetics_unlocked),
            color = MaterialTheme.colorScheme.primary,
        )
        rewardFeedback?.let {
            Text(
                it,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private fun CompanionStateSnapshot.latestSuggestionChips(languageTag: String): List<String>? {
    val latestIntent = recentConversationMemory.messages.lastOrNull()?.intent ?: return null
    return CompanionChatEngine.suggestionChipsForIntent(this, latestIntent, languageTag)
}
