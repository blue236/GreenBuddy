package com.blue236.greenbuddy.ui.state

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.data.content.CompanionCopyLoader
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
import com.blue236.greenbuddy.domain.MiscActionCoordinator
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
    private val companionCopyLoader = CompanionCopyLoader(application)
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
    private val miscActionCoordinator = MiscActionCoordinator(rewardEngine)
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
        val localeTag = preferences.appLanguage.languageTag ?: currentLanguageTag()
        uiStateAssembler.assemble(
            preferences = preferences,
            selectedTab = tab,
            rewardFeedback = feedback,
            feedbackEvent = pendingFeedback,
            localeTag = localeTag,
            companionCopy = companionCopyLoader.copyFor(localeTag),
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
        val state = uiState.value
        val outcome = miscActionCoordinator.realPlantModeToggle(state.selectedStarterId, state.realPlantModeState, enabled)
        analyticsLogger.log(outcome.analyticsEvent)
        viewModelScope.launch { repository.saveRealPlantModeState(outcome.starterId, outcome.updatedState) }
    }
    fun submitCompanionChatMessage(message: String) {
        val state = uiState.value
        val languageTag = state.appLanguage.languageTag ?: currentLanguageTag()
        val result = companionCoordinator.handleMessage(
            message = message,
            snapshot = state.companionStateSnapshot,
            languageTag = languageTag,
            copy = companionCopyLoader.copyFor(languageTag),
        )
        val outcome = miscActionCoordinator.companionMessage(state.selectedStarterId, result.updatedMemory)
        analyticsLogger.log(outcome.analyticsEvent)
        viewModelScope.launch { repository.saveCompanionConversationMemory(outcome.starterId, outcome.updatedMemory) }
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
        val outcome = miscActionCoordinator.realPlantLog(state.selectedStarterId, action.name, result.realPlantModeState, result.plantCareState)
        analyticsLogger.log(outcome.analyticsEvent)
        viewModelScope.launch { repository.saveRealPlantModeAndPlantCareState(outcome.starterId, outcome.realPlantModeState, outcome.plantCareState) }
    }
    fun purchaseCosmetic(item: CosmeticItem) {
        val s = uiState.value
        val result = cosmeticCoordinator.purchase(item, s.rewardState)
        if (!result.accepted) return
        val outcome = miscActionCoordinator.cosmeticPurchase(item, s.appLanguage.languageTag ?: currentLanguageTag(), result.updatedRewardState)
        rewardFeedback.value = outcome.rewardFeedback
        analyticsLogger.log(outcome.analyticsEvent)
        viewModelScope.launch { repository.saveRewardState(outcome.updatedRewardState) }
    }
    fun equipCosmetic(itemId: String) {
        val s = uiState.value
        val updated = cosmeticCoordinator.equip(itemId, s.rewardState)
        val outcome = miscActionCoordinator.cosmeticEquip(changed = updated != s.rewardState, updatedRewardState = updated)
        outcome.analyticsEvent?.let(analyticsLogger::log)
        if (updated != s.rewardState) viewModelScope.launch { repository.saveRewardState(outcome.updatedRewardState) }
    }
    fun acknowledgeGrowthStage() {
        val state = uiState.value
        val outcome = miscActionCoordinator.growthAcknowledge(state.selectedStarterId, state.growthStageState.currentStage.rank)
        analyticsLogger.log(outcome.analyticsEvent)
        viewModelScope.launch { repository.saveSeenGrowthStageRank(outcome.starterId, outcome.seenGrowthStageRank) }
    }
    fun setSelectedWeatherCity(cityId: String) {
        val outcome = miscActionCoordinator.weatherCity(cityId)
        analyticsLogger.log(outcome.analyticsEvent)
        viewModelScope.launch { repository.saveSelectedWeatherCity(outcome.cityId) }
    }
    fun setAppLanguage(appLanguage: AppLanguage) {
        val outcome = miscActionCoordinator.appLanguage(appLanguage)
        AppCompatDelegate.setApplicationLocales(outcome.appLanguage.asLocaleListCompat())
        analyticsLogger.log(outcome.analyticsEvent)
        viewModelScope.launch { repository.saveAppLanguage(outcome.appLanguage) }
    }

    private fun currentLanguageTag(): String = getApplication<Application>().resources.configuration.locales[0]?.toLanguageTag().orEmpty().ifBlank { "en" }
}

