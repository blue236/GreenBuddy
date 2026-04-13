package com.blue236.greenbuddy.ui.state

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.data.content.LessonContentLoader
import com.blue236.greenbuddy.domain.AnalyticsEvent
import com.blue236.greenbuddy.domain.AndroidAnalyticsLogger
import com.blue236.greenbuddy.domain.CareEngine
import com.blue236.greenbuddy.domain.CompanionCoordinator
import com.blue236.greenbuddy.domain.GrowthEngine
import com.blue236.greenbuddy.domain.LessonEngine
import com.blue236.greenbuddy.domain.MissionEngine
import com.blue236.greenbuddy.domain.RealPlantCoordinator
import com.blue236.greenbuddy.domain.RewardEngine
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.AppLanguage
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.FeedbackEvent
import com.blue236.greenbuddy.model.FeedbackEventType
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.Tab
import com.blue236.greenbuddy.model.WeatherAdviceGenerator
import com.blue236.greenbuddy.model.SeasonalWeatherProvider
import com.blue236.greenbuddy.model.asLocaleListCompat
import com.blue236.greenbuddy.model.normalizedFor
import com.blue236.greenbuddy.model.recordCareAction
import com.blue236.greenbuddy.model.recordLessonCompletion
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
    private val lessonContentLoader = LessonContentLoader(application)
    private val analyticsLogger = AndroidAnalyticsLogger()
    private val missionEngine = MissionEngine()
    private val growthEngine = GrowthEngine()
    private val rewardEngine = RewardEngine(application)
    private val lessonEngine = LessonEngine(missionEngine)
    private val careEngine = CareEngine(missionEngine)
    private val companionCoordinator = CompanionCoordinator()
    private val realPlantCoordinator = RealPlantCoordinator()
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
        val lessonsByStarterId = StarterPlants.options.associate { it.id to lessonContentLoader.lessonsFor(it.companion.species, localeTag) }
        val normalizedLessonProgressByStarterId = preferences.lessonProgressByStarterId.mapValues { (starterId, progress) ->
            progress.normalizedFor(lessonsByStarterId[starterId].orEmpty())
        }
        val selectedLessons = lessonsByStarterId[preferences.selectedStarter.id].orEmpty()
        val selectedLessonProgress = normalizedLessonProgressByStarterId[preferences.selectedStarter.id] ?: LessonProgress()
        val selectedCareState = preferences.plantCareState
        val normalizedDailyMissionProgress = preferences.dailyMissionProgress.normalizedFor(LocalDate.now())
        val todayMissions = missionEngine.resolveToday(normalizedDailyMissionProgress, selectedLessonProgress, selectedCareState, LocalDate.now())
        val growthStageState = growthEngine.resolve(preferences.selectedStarter.id, selectedLessonProgress, selectedCareState, preferences.seenGrowthStageRank)
        val weatherSnapshot = SeasonalWeatherProvider.snapshotFor(preferences.selectedWeatherCityId, LocalDate.now())
        val weatherAdvice = WeatherAdviceGenerator.adviceFor(preferences.selectedStarter, weatherSnapshot, localeTag)
        val companionSnapshot = companionCoordinator.snapshot(
            starter = preferences.selectedStarter,
            careState = selectedCareState,
            growthStageState = growthStageState,
            dailyMissionSet = todayMissions,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = preferences.realPlantModeState,
            recentConversationMemory = preferences.companionConversationMemory,
            languageTag = localeTag,
        )
        GreenBuddyUiState(
            selectedTab = tab,
            selectedStarterId = preferences.selectedStarter.id,
            ownedStarterIds = preferences.ownedStarterIds,
            onboardingComplete = preferences.onboardingComplete,
            lessons = selectedLessons,
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
            companionStateSnapshot = companionSnapshot,
            companionHomeCheckIn = companionCoordinator.homeCheckIn(companionSnapshot, localeTag),
            appLanguage = preferences.appLanguage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GreenBuddyUiState())

    fun onAppVisible() {
        analyticsLogger.log(AnalyticsEvent("app_visible"))
        viewModelScope.launch { repository.recordAppOpen(System.currentTimeMillis()) }
    }
    fun clearFeedbackEvent(eventId: Long) { if (feedbackEvent.value?.id == eventId) feedbackEvent.value = null }
    fun selectTab(tab: Tab) {
        analyticsLogger.log(AnalyticsEvent("select_tab", mapOf("tab" to tab.name)))
        selectedTab.value = tab
    }
    fun selectStarter(starterId: String) { viewModelScope.launch { repository.setSelectedStarter(starterId) } }
    fun completeOnboarding() { viewModelScope.launch { repository.completeOnboarding(uiState.value.selectedStarterId); repository.recordAppOpen(System.currentTimeMillis()) } }

    fun submitCurrentLessonAnswer(selectedAnswerIndex: Int): Boolean {
        val state = uiState.value
        val languageTag = state.appLanguage.languageTag ?: currentLanguageTag()
        val today = LocalDate.now()
        val previousGrowthStage = state.growthStageState.currentStage.rank
        val result = lessonEngine.submitAnswer(
            selectedAnswerIndex = selectedAnswerIndex,
            lessons = state.lessons,
            lessonProgress = state.lessonProgress,
            missionProgress = state.dailyMissionProgress,
            rewardState = state.rewardState,
            careState = state.plantCareState,
            today = today,
            ownedStarterIds = state.ownedStarterIds,
        )
        val currentLesson = result.lesson ?: return false
        if (!result.accepted) {
            analyticsLogger.log(AnalyticsEvent("lesson_answer_incorrect", mapOf("lesson_id" to currentLesson.id)))
            return false
        }
        val unlockedStarter = result.unlockedStarterId?.let { unlockedId -> StarterPlants.options.firstOrNull { it.id == unlockedId } }
        val missionOutcome = result.missionRewardOutcome ?: return false
        val baseFeedback = rewardEngine.lessonFeedback(currentLesson.rewardXp, missionOutcome)
        rewardFeedback.value = rewardEngine.greenhouseUnlockFeedback(baseFeedback, unlockedStarter, languageTag)
        val unlockedGrowth = growthEngine.didUnlock(state.selectedStarterId, result.lessonProgress, state.plantCareState, previousGrowthStage)
        emitFeedback(if (unlockedGrowth) FeedbackEventType.GROWTH_UNLOCKED else FeedbackEventType.LESSON_SUCCESS)
        analyticsLogger.log(AnalyticsEvent("lesson_completed", mapOf("lesson_id" to currentLesson.id, "starter_id" to state.selectedStarterId)))
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveLessonAndMissionProgress(state.selectedStarterId, result.lessonProgress, missionOutcome.progress)
            repository.saveRewardState(missionOutcome.rewardState)
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
        val result = careEngine.performAction(
            action = action,
            currentCareState = state.plantCareState,
            currentLessonProgress = state.lessonProgress,
            currentMissionProgress = state.dailyMissionProgress,
            currentRewardState = state.rewardState,
            today = today,
        )
        rewardFeedback.value = rewardEngine.careFeedback(
            action,
            state.appLanguage.languageTag ?: currentLanguageTag(),
            result.wasHelpful,
            result.missionRewardOutcome,
        )
        if (result.wasHelpful) {
            emitFeedback(
                if (growthEngine.didUnlock(state.selectedStarterId, state.lessonProgress, result.updatedCareState, previousGrowthStage)) {
                    FeedbackEventType.GROWTH_UNLOCKED
                } else {
                    FeedbackEventType.CARE_SUCCESS
                },
            )
        }
        analyticsLogger.log(AnalyticsEvent("care_action", mapOf("action" to action.name, "starter_id" to state.selectedStarterId, "helpful" to result.wasHelpful.toString())))
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveCareStateAndMissionProgress(state.selectedStarterId, result.updatedCareState, result.missionRewardOutcome.progress)
            repository.saveRewardState(result.missionRewardOutcome.rewardState)
            repository.recordCareAction(now)
            repository.recordAppOpen(now)
        }
    }

    fun setRealPlantModeEnabled(enabled: Boolean) {
        analyticsLogger.log(AnalyticsEvent("real_plant_mode_toggled", mapOf("enabled" to enabled.toString())))
        viewModelScope.launch { repository.saveRealPlantModeState(uiState.value.selectedStarterId, uiState.value.realPlantModeState.copy(enabled = enabled)) }
    }
    fun submitCompanionChatMessage(message: String) {
        val state = uiState.value
        val languageTag = state.appLanguage.languageTag ?: currentLanguageTag()
        val reply = companionCoordinator.reply(
            message = message,
            snapshot = state.companionStateSnapshot,
            languageTag = languageTag,
        )
        val updatedMemory = companionCoordinator.updatedMemoryFor(reply, state.companionStateSnapshot)
        analyticsLogger.log(AnalyticsEvent("companion_message_sent", mapOf("starter_id" to state.selectedStarterId)))
        viewModelScope.launch { repository.saveCompanionConversationMemory(state.selectedStarterId, updatedMemory) }
    }
    fun logRealPlantCare(action: RealPlantCareAction) {
        val state = uiState.value
        val result = realPlantCoordinator.logAction(
            action = action,
            currentRealPlantModeState = state.realPlantModeState,
            currentPlantCareState = state.plantCareState,
            nowMillis = System.currentTimeMillis(),
            zoneId = ZoneId.systemDefault(),
        )
        if (!result.accepted) return
        analyticsLogger.log(AnalyticsEvent("real_plant_action_logged", mapOf("action" to action.name, "starter_id" to state.selectedStarterId)))
        viewModelScope.launch { repository.saveRealPlantModeAndPlantCareState(state.selectedStarterId, result.realPlantModeState, result.plantCareState) }
    }
    fun purchaseCosmetic(item: CosmeticItem) {
        val s = uiState.value
        if (!s.rewardState.canPurchase(item)) return
        val updated = s.rewardState.purchase(item)
        rewardFeedback.value = rewardEngine.cosmeticFeedback(item, s.appLanguage.languageTag ?: currentLanguageTag())
        analyticsLogger.log(AnalyticsEvent("cosmetic_purchased", mapOf("item_id" to item.id)))
        viewModelScope.launch { repository.saveRewardState(updated) }
    }
    fun equipCosmetic(itemId: String) { val s = uiState.value; val updated = s.rewardState.equip(itemId); if (updated != s.rewardState) viewModelScope.launch { repository.saveRewardState(updated) } }
    fun acknowledgeGrowthStage() {
        analyticsLogger.log(AnalyticsEvent("growth_acknowledged", mapOf("starter_id" to uiState.value.selectedStarterId)))
        viewModelScope.launch { repository.saveSeenGrowthStageRank(uiState.value.selectedStarterId, uiState.value.growthStageState.currentStage.rank) }
    }
    fun setSelectedWeatherCity(cityId: String) {
        analyticsLogger.log(AnalyticsEvent("weather_city_selected", mapOf("city_id" to cityId)))
        viewModelScope.launch { repository.saveSelectedWeatherCity(cityId) }
    }
    fun setAppLanguage(appLanguage: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(appLanguage.asLocaleListCompat())
        analyticsLogger.log(AnalyticsEvent("language_selected", mapOf("language" to appLanguage.name)))
        viewModelScope.launch { repository.saveAppLanguage(appLanguage) }
    }

    private fun emitFeedback(type: FeedbackEventType) {
        feedbackEvent.value = FeedbackEvent(id = System.nanoTime(), type = type)
    }

    private fun currentLanguageTag(): String = getApplication<Application>().resources.configuration.locales[0]?.toLanguageTag().orEmpty().ifBlank { "en" }
}

