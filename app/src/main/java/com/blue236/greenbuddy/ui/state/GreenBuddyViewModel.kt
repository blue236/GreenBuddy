package com.blue236.greenbuddy.ui.state

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.data.content.LessonContentLoader
import com.blue236.greenbuddy.domain.AnalyticsEvent
import com.blue236.greenbuddy.domain.ActionPersistenceCoordinator
import com.blue236.greenbuddy.domain.ActionUiCoordinator
import com.blue236.greenbuddy.domain.AndroidAnalyticsLogger
import com.blue236.greenbuddy.domain.CareEngine
import com.blue236.greenbuddy.domain.CompanionCoordinator
import com.blue236.greenbuddy.domain.CosmeticCoordinator
import com.blue236.greenbuddy.domain.FeedbackCoordinator
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
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.Tab
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
    private val cosmeticCoordinator = CosmeticCoordinator()
    private val feedbackCoordinator = FeedbackCoordinator()
    private val actionUiCoordinator = ActionUiCoordinator(rewardEngine, feedbackCoordinator, growthEngine)
    private val actionPersistenceCoordinator = ActionPersistenceCoordinator()
    private val realPlantCoordinator = RealPlantCoordinator()
    private val uiStateAssembler = UiStateAssembler(
        missionEngine = missionEngine,
        growthEngine = growthEngine,
        companionCoordinator = companionCoordinator,
        lessonsForStarter = { starterId, localeTag ->
            val starter = StarterPlants.options.first { it.id == starterId }
            lessonContentLoader.lessonsFor(starter.companion.species, localeTag)
        },
    )
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
        uiStateAssembler.assemble(
            preferences = preferences,
            selectedTab = tab,
            rewardFeedback = feedback,
            feedbackEvent = pendingFeedback,
            localeTag = preferences.appLanguage.languageTag ?: currentLanguageTag(),
            today = LocalDate.now(),
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
        val uiOutcome = actionUiCoordinator.lessonOutcome(
            starterId = state.selectedStarterId,
            previousGrowthStage = previousGrowthStage,
            languageTag = languageTag,
            rewardXp = currentLesson.rewardXp,
            missionOutcome = missionOutcome,
            unlockedStarter = unlockedStarter,
            updatedLessonProgress = result.lessonProgress,
            currentCareState = state.plantCareState,
        )
        rewardFeedback.value = uiOutcome.rewardFeedback
        feedbackEvent.value = uiOutcome.feedbackEvent
        val persistenceOutcome = actionPersistenceCoordinator.lessonOutcome(
            starterId = state.selectedStarterId,
            lessonId = currentLesson.id,
            lessonProgress = result.lessonProgress,
            missionProgress = missionOutcome.progress,
            rewardState = missionOutcome.rewardState,
            unlockedStarterId = unlockedStarter?.id,
        )
        analyticsLogger.log(persistenceOutcome.analyticsEvent)
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveLessonAndMissionProgress(persistenceOutcome.starterId, persistenceOutcome.lessonProgress, persistenceOutcome.missionProgress)
            repository.saveRewardState(persistenceOutcome.rewardState)
            repository.recordLessonCompleted(now)
            repository.recordAppOpen(now)
            persistenceOutcome.unlockedStarterId?.let { repository.unlockStarter(it) }
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
        val uiOutcome = actionUiCoordinator.careOutcome(
            starterId = state.selectedStarterId,
            previousGrowthStage = previousGrowthStage,
            languageTag = state.appLanguage.languageTag ?: currentLanguageTag(),
            action = action,
            wasHelpful = result.wasHelpful,
            missionOutcome = result.missionRewardOutcome,
            currentLessonProgress = state.lessonProgress,
            updatedCareState = result.updatedCareState,
        )
        rewardFeedback.value = uiOutcome.rewardFeedback
        if (uiOutcome.feedbackEvent != null) {
            feedbackEvent.value = uiOutcome.feedbackEvent
        }
        val persistenceOutcome = actionPersistenceCoordinator.careOutcome(
            starterId = state.selectedStarterId,
            actionName = action.name,
            wasHelpful = result.wasHelpful,
            updatedCareState = result.updatedCareState,
            missionProgress = result.missionRewardOutcome.progress,
            rewardState = result.missionRewardOutcome.rewardState,
        )
        analyticsLogger.log(persistenceOutcome.analyticsEvent)
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveCareStateAndMissionProgress(persistenceOutcome.starterId, persistenceOutcome.updatedCareState, persistenceOutcome.missionProgress)
            repository.saveRewardState(persistenceOutcome.rewardState)
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
        val result = companionCoordinator.handleMessage(
            message = message,
            snapshot = state.companionStateSnapshot,
            languageTag = languageTag,
        )
        analyticsLogger.log(AnalyticsEvent("companion_message_sent", mapOf("starter_id" to state.selectedStarterId)))
        viewModelScope.launch { repository.saveCompanionConversationMemory(state.selectedStarterId, result.updatedMemory) }
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
        val result = cosmeticCoordinator.purchase(item, s.rewardState)
        if (!result.accepted) return
        rewardFeedback.value = rewardEngine.cosmeticFeedback(item, s.appLanguage.languageTag ?: currentLanguageTag())
        analyticsLogger.log(AnalyticsEvent("cosmetic_purchased", mapOf("item_id" to item.id)))
        viewModelScope.launch { repository.saveRewardState(result.updatedRewardState) }
    }
    fun equipCosmetic(itemId: String) {
        val s = uiState.value
        val updated = cosmeticCoordinator.equip(itemId, s.rewardState)
        if (updated != s.rewardState) viewModelScope.launch { repository.saveRewardState(updated) }
    }
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

    private fun currentLanguageTag(): String = getApplication<Application>().resources.configuration.locales[0]?.toLanguageTag().orEmpty().ifBlank { "en" }
}

