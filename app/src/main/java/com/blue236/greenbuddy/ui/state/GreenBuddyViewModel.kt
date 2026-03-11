package com.blue236.greenbuddy.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.Tab
import com.blue236.greenbuddy.model.advanceWith
import com.blue236.greenbuddy.model.claimStreakRewardIfEligible
import com.blue236.greenbuddy.model.completeDailyMissions
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.normalizedFor
import com.blue236.greenbuddy.model.recordCareAction
import com.blue236.greenbuddy.model.recordLessonCompletion
import com.blue236.greenbuddy.model.resolveForToday
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GreenBuddyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GreenBuddyPreferencesRepository(application)
    private val selectedTab = MutableStateFlow(Tab.HOME)

    init {
        viewModelScope.launch {
            repository.preferences.collect { preferences ->
                val normalizedMissionProgress = preferences.dailyMissionProgress.normalizedFor(LocalDate.now())
                if (normalizedMissionProgress != preferences.dailyMissionProgress) {
                    repository.saveDailyMissionProgress(preferences.selectedStarterId, normalizedMissionProgress)
                }
            }
        }
    }

    val uiState: StateFlow<GreenBuddyUiState> = combine(
        repository.preferences,
        selectedTab,
    ) { preferences, tab ->
        val lessons = LessonCatalog.forSpecies(preferences.selectedStarter.companion.species)
        val normalizedLessonProgress = preferences.lessonProgress.normalizedFor(lessons)
        val normalizedDailyMissionProgress = preferences.dailyMissionProgress.normalizedFor(LocalDate.now())
        val todayMissions = normalizedDailyMissionProgress.resolveForToday(
            today = LocalDate.now(),
            lessonProgress = normalizedLessonProgress,
            careState = preferences.plantCareState,
        )

        GreenBuddyUiState(
            selectedTab = tab,
            selectedStarterId = preferences.selectedStarterId,
            onboardingComplete = preferences.onboardingComplete,
            lessonProgress = normalizedLessonProgress,
            plantCareState = preferences.plantCareState,
            dailyMissionProgress = normalizedDailyMissionProgress,
            dailyMissionSet = todayMissions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GreenBuddyUiState(),
    )

    fun selectTab(tab: Tab) {
        selectedTab.value = tab
    }

    fun selectStarter(starterId: String) {
        viewModelScope.launch {
            repository.setSelectedStarter(id = starterId)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding(uiState.value.selectedStarterId)
        }
    }

    fun submitCurrentLessonAnswer(selectedAnswerIndex: Int): Boolean {
        val state = uiState.value
        val lessons = LessonCatalog.forSpecies(state.selectedStarter.companion.species)
        val currentLesson = state.lessonProgress.currentLessonOrNull(lessons) ?: return false
        if (selectedAnswerIndex != currentLesson.correctAnswerIndex) return false

        val today = LocalDate.now()
        val updatedLessonProgress = state.lessonProgress.advanceWith(
            completedLessonId = currentLesson.id,
            rewardXp = currentLesson.rewardXp,
            totalLessons = lessons.size,
        )
        val updatedMissionProgress = rewardIfMissionSetCompleted(
            progress = state.dailyMissionProgress.recordLessonCompletion(today),
            lessonProgress = updatedLessonProgress,
            careState = state.plantCareState,
            today = today,
        )

        viewModelScope.launch {
            repository.saveLessonAndMissionProgress(
                starterId = state.selectedStarterId,
                lessonProgress = updatedLessonProgress,
                missionProgress = updatedMissionProgress,
            )
        }
        return true
    }

    fun performCareAction(action: CareAction) {
        val state = uiState.value
        val today = LocalDate.now()
        val updatedCareState = state.plantCareState.apply(action)
        val updatedMissionProgress = rewardIfMissionSetCompleted(
            progress = state.dailyMissionProgress.recordCareAction(today),
            lessonProgress = state.lessonProgress,
            careState = updatedCareState,
            today = today,
        )
        viewModelScope.launch {
            repository.saveCareStateAndMissionProgress(
                starterId = state.selectedStarterId,
                careState = updatedCareState,
                missionProgress = updatedMissionProgress,
            )
        }
    }

    private fun rewardIfMissionSetCompleted(
        progress: DailyMissionProgress,
        lessonProgress: LessonProgress,
        careState: PlantCareState,
        today: LocalDate,
    ): DailyMissionProgress {
        val missionSet = progress.resolveForToday(today, lessonProgress, careState)
        if (!missionSet.allCompletedToday) return progress.normalizedFor(today)
        return progress
            .completeDailyMissions(today)
            .claimStreakRewardIfEligible(today)
    }
}
