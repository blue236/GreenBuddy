package com.blue236.greenbuddy.ui.state

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.AppLanguage
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.FeedbackEvent
import com.blue236.greenbuddy.model.FeedbackEventType
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.Tab
import com.blue236.greenbuddy.model.WeatherAdviceGenerator
import com.blue236.greenbuddy.model.SeasonalWeatherProvider
import com.blue236.greenbuddy.model.advanceWith
import com.blue236.greenbuddy.model.asLocaleListCompat
import com.blue236.greenbuddy.model.claimStreakRewardIfEligible
import com.blue236.greenbuddy.model.completeDailyMissions
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.model.nextUnlockableStarterId
import com.blue236.greenbuddy.model.normalizedFor
import com.blue236.greenbuddy.model.localizedLabel
import com.blue236.greenbuddy.model.localizedName
import com.blue236.greenbuddy.model.localizedTitle
import com.blue236.greenbuddy.model.recordCareAction
import com.blue236.greenbuddy.model.recordLessonCompletion
import com.blue236.greenbuddy.model.resolveForToday
import com.blue236.greenbuddy.model.resolveGrowthStageState
import com.blue236.greenbuddy.notifications.ReminderScheduler
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GreenBuddyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GreenBuddyPreferencesRepository(application)
    private val selectedTab = MutableStateFlow(Tab.HOME)
    private val rewardFeedback = MutableStateFlow<String?>(null)
    private val feedbackEvent = MutableStateFlow<FeedbackEvent?>(null)

    init {
        ReminderScheduler.schedule(application)
        viewModelScope.launch {
            repository.preferences.collect { preferences ->
                val normalizedMissionProgress = preferences.dailyMissionProgress.normalizedFor(LocalDate.now())
                if (normalizedMissionProgress != preferences.dailyMissionProgress) {
                    repository.saveDailyMissionProgress(preferences.selectedStarter.id, normalizedMissionProgress)
                }
            }
        }
    }

    val uiState: StateFlow<GreenBuddyUiState> = combine(repository.preferences, selectedTab, rewardFeedback, feedbackEvent) { preferences, tab, feedback, pendingFeedback ->
        val localeTag = preferences.appLanguage.languageTag ?: currentLanguageTag()
        val normalizedLessonProgressByStarterId = preferences.lessonProgressByStarterId.mapValues { (starterId, progress) ->
            val starter = StarterPlants.options.first { it.id == starterId }
            progress.normalizedFor(LessonCatalog.forSpecies(starter.companion.species, localeTag))
        }
        val selectedLessonProgress = normalizedLessonProgressByStarterId[preferences.selectedStarter.id] ?: LessonProgress()
        val selectedCareState = preferences.plantCareState
        val normalizedDailyMissionProgress = preferences.dailyMissionProgress.normalizedFor(LocalDate.now())
        val todayMissions = normalizedDailyMissionProgress.resolveForToday(LocalDate.now(), selectedLessonProgress, selectedCareState)
        val growthStageState = resolveGrowthStageState(preferences.selectedStarter.id, selectedLessonProgress, selectedCareState, preferences.seenGrowthStageRank)
        val weatherSnapshot = SeasonalWeatherProvider.snapshotFor(preferences.selectedWeatherCityId, LocalDate.now())
        val weatherAdvice = WeatherAdviceGenerator.adviceFor(preferences.selectedStarter, weatherSnapshot, localeTag)
        GreenBuddyUiState(
            selectedTab = tab,
            selectedStarterId = preferences.selectedStarter.id,
            ownedStarterIds = preferences.ownedStarterIds,
            onboardingComplete = preferences.onboardingComplete,
            lessonProgressByStarterId = normalizedLessonProgressByStarterId,
            plantCareStateByStarterId = preferences.plantCareStateByStarterId,
            dailyMissionProgress = normalizedDailyMissionProgress,
            dailyMissionSet = todayMissions,
            growthStageState = growthStageState,
            rewardState = preferences.rewardState,
            rewardFeedback = feedback,
            feedbackEvent = pendingFeedback,
            realPlantModeState = preferences.realPlantModeState,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            companionStateSnapshot = com.blue236.greenbuddy.model.CompanionChatEngine.createSnapshot(
                starter = preferences.selectedStarter,
                careState = selectedCareState,
                growthStageState = growthStageState,
                dailyMissionSet = todayMissions,
                weatherSnapshot = weatherSnapshot,
                weatherAdvice = weatherAdvice,
                realPlantModeState = preferences.realPlantModeState,
                recentConversationMemory = preferences.companionConversationMemory,
                languageTag = localeTag,
            ),
            companionHomeCheckIn = com.blue236.greenbuddy.model.CompanionChatEngine.proactiveCheckIn(
                com.blue236.greenbuddy.model.CompanionChatEngine.createSnapshot(
                    starter = preferences.selectedStarter,
                    careState = selectedCareState,
                    growthStageState = growthStageState,
                    dailyMissionSet = todayMissions,
                    weatherSnapshot = weatherSnapshot,
                    weatherAdvice = weatherAdvice,
                    realPlantModeState = preferences.realPlantModeState,
                    recentConversationMemory = preferences.companionConversationMemory,
                    languageTag = localeTag,
                ),
                languageTag = localeTag,
            ),
            appLanguage = preferences.appLanguage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GreenBuddyUiState())

    fun onAppVisible() { viewModelScope.launch { repository.recordAppOpen(System.currentTimeMillis()) } }
    fun clearFeedbackEvent(eventId: Long) { if (feedbackEvent.value?.id == eventId) feedbackEvent.value = null }
    fun selectTab(tab: Tab) { selectedTab.value = tab }
    fun selectStarter(starterId: String) { viewModelScope.launch { repository.setSelectedStarter(starterId) } }
    fun completeOnboarding() { viewModelScope.launch { repository.completeOnboarding(uiState.value.selectedStarterId); repository.recordAppOpen(System.currentTimeMillis()) } }

    fun submitCurrentLessonAnswer(selectedAnswerIndex: Int): Boolean {
        val state = uiState.value
        val languageTag = state.appLanguage.languageTag ?: currentLanguageTag()
        val lessons = LessonCatalog.forSpecies(state.selectedStarter.companion.species, languageTag)
        val currentLesson = state.lessonProgress.currentLessonOrNull(lessons) ?: return false
        if (selectedAnswerIndex != currentLesson.quiz.correctAnswerIndex) return false
        val today = LocalDate.now()
        val previousGrowthStage = state.growthStageState.currentStage.rank
        val updatedLessonProgress = state.lessonProgress.advanceWith(currentLesson.id, currentLesson.rewardXp, lessons.size)
        val rewardOutcome = rewardIfMissionSetCompleted(state.dailyMissionProgress.recordLessonCompletion(today), state.rewardState.rewardForLesson(currentLesson.rewardXp), updatedLessonProgress, state.plantCareState, today)
        val unlockedStarter = if (updatedLessonProgress.isComplete(lessons)) {
            nextUnlockableStarterId(state.ownedStarterIds)?.let { unlockedId ->
                StarterPlants.options.firstOrNull { it.id == unlockedId }
            }
        } else {
            null
        }
        val baseFeedback = rewardOutcome.feedbackMessageRes?.let { feedbackRes ->
            getApplication<Application>().getString(
                feedbackRes,
                rewardOutcome.dailyRewardTokensAwarded,
                rewardOutcome.streakRewardTokensAwarded,
            )
        } ?: getApplication<Application>().getString(
            R.string.reward_feedback_lesson_complete,
            currentLesson.rewardXp,
            RewardState.lessonTokenReward(currentLesson.rewardXp),
        )
        val unlockFeedback = unlockedStarter?.let { starter ->
            getApplication<Application>().getString(
                R.string.reward_feedback_greenhouse_unlock,
                starter.localizedTitle(languageTag),
                starter.previewEmoji,
            )
        }
        rewardFeedback.value = composeGreenhouseUnlockFeedback(
            baseFeedback = baseFeedback,
            unlockFeedback = unlockFeedback,
        )
        emitFeedback(if (didUnlockGrowthStage(state.selectedStarterId, updatedLessonProgress, state.plantCareState, previousGrowthStage)) FeedbackEventType.GROWTH_UNLOCKED else FeedbackEventType.LESSON_SUCCESS)
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveLessonAndMissionProgress(state.selectedStarterId, updatedLessonProgress, rewardOutcome.progress)
            repository.saveRewardState(rewardOutcome.rewardState)
            repository.recordLessonCompleted(now)
            repository.recordAppOpen(now)
            unlockedStarter?.let { repository.unlockStarter(it.id) }
        }
        return true
    }

    fun performCareAction(action: CareAction) {
        val state = uiState.value
        val today = LocalDate.now()
        val previousGrowthStage = state.growthStageState.currentStage.rank
        val updatedCareState = state.plantCareState.apply(action)
        val wasHelpful = updatedCareState.isMeaningfullyImprovedFrom(state.plantCareState)
        val rewardOutcome = rewardIfMissionSetCompleted(state.dailyMissionProgress.recordCareAction(today), state.rewardState.rewardForCareAction(wasHelpful), state.lessonProgress, updatedCareState, today)
        rewardFeedback.value = rewardOutcome.feedbackMessageRes?.let { feedbackRes ->
            getApplication<Application>().getString(
                feedbackRes,
                rewardOutcome.dailyRewardTokensAwarded,
                rewardOutcome.streakRewardTokensAwarded,
            )
        } ?: if (wasHelpful) {
            getApplication<Application>().getString(
                R.string.reward_feedback_care_helped,
                action.localizedLabel(state.appLanguage.languageTag ?: currentLanguageTag()),
                RewardState.careTokenReward(),
            )
        } else {
            getApplication<Application>().getString(
                R.string.reward_feedback_care_no_effect,
                action.localizedLabel(state.appLanguage.languageTag ?: currentLanguageTag()),
            )
        }
        if (wasHelpful) {
            emitFeedback(if (didUnlockGrowthStage(state.selectedStarterId, state.lessonProgress, updatedCareState, previousGrowthStage)) FeedbackEventType.GROWTH_UNLOCKED else FeedbackEventType.CARE_SUCCESS)
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveCareStateAndMissionProgress(state.selectedStarterId, updatedCareState, rewardOutcome.progress)
            repository.saveRewardState(rewardOutcome.rewardState)
            repository.recordCareAction(now)
            repository.recordAppOpen(now)
        }
    }

    fun setRealPlantModeEnabled(enabled: Boolean) { viewModelScope.launch { repository.saveRealPlantModeState(uiState.value.selectedStarterId, uiState.value.realPlantModeState.copy(enabled = enabled)) } }
    fun submitCompanionChatMessage(message: String) {
        val state = uiState.value
        val languageTag = state.appLanguage.languageTag ?: currentLanguageTag()
        val reply = com.blue236.greenbuddy.model.CompanionChatEngine.replyTo(
            message = message,
            snapshot = state.companionStateSnapshot,
            languageTag = languageTag,
        )
        val updatedMemory = com.blue236.greenbuddy.model.CompanionChatEngine.updatedMemoryFor(reply, state.companionStateSnapshot)
        viewModelScope.launch { repository.saveCompanionConversationMemory(state.selectedStarterId, updatedMemory) }
    }
    fun logRealPlantCare(action: RealPlantCareAction) {
        val state = uiState.value
        val zoneId = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val date = Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate()
        if (!state.realPlantModeState.canLogActionOn(action, date, zoneId)) return
        val updatedReal = state.realPlantModeState.logAction(action, now, zoneId)
        val updatedCare = state.plantCareState.apply(action.linkedCareAction)
        viewModelScope.launch { repository.saveRealPlantModeAndPlantCareState(state.selectedStarterId, updatedReal, updatedCare) }
    }
    fun purchaseCosmetic(item: CosmeticItem) {
        val s = uiState.value
        if (!s.rewardState.canPurchase(item)) return
        val updated = s.rewardState.purchase(item)
        rewardFeedback.value = getApplication<Application>().getString(
            R.string.reward_feedback_cosmetic_unlocked,
            item.localizedName(s.appLanguage.languageTag ?: currentLanguageTag()),
            item.emoji,
        )
        viewModelScope.launch { repository.saveRewardState(updated) }
    }
    fun equipCosmetic(itemId: String) { val s = uiState.value; val updated = s.rewardState.equip(itemId); if (updated != s.rewardState) viewModelScope.launch { repository.saveRewardState(updated) } }
    fun acknowledgeGrowthStage() { viewModelScope.launch { repository.saveSeenGrowthStageRank(uiState.value.selectedStarterId, uiState.value.growthStageState.currentStage.rank) } }
    fun setSelectedWeatherCity(cityId: String) { viewModelScope.launch { repository.saveSelectedWeatherCity(cityId) } }
    fun setAppLanguage(appLanguage: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(appLanguage.asLocaleListCompat())
        viewModelScope.launch { repository.saveAppLanguage(appLanguage) }
    }

    private fun rewardIfMissionSetCompleted(progress: DailyMissionProgress, rewardState: RewardState, lessonProgress: LessonProgress, careState: PlantCareState, today: LocalDate): RewardOutcome =
        evaluateMissionCompletionRewards(progress, rewardState, lessonProgress, careState, today)

    private fun didUnlockGrowthStage(starterId: String, lessonProgress: LessonProgress, careState: PlantCareState, previousGrowthStageRank: Int): Boolean =
        resolveGrowthStageState(starterId, lessonProgress, careState, seenStageRank = previousGrowthStageRank).newlyUnlocked

    private fun emitFeedback(type: FeedbackEventType) {
        feedbackEvent.value = FeedbackEvent(id = System.nanoTime(), type = type)
    }

    private fun currentLanguageTag(): String = getApplication<Application>().resources.configuration.locales[0]?.toLanguageTag().orEmpty().ifBlank { "en" }
}

