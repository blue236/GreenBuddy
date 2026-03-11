package com.blue236.greenbuddy.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.Tab
import com.blue236.greenbuddy.model.advanceWith
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.normalizedFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GreenBuddyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GreenBuddyPreferencesRepository(application)
    private val selectedTab = MutableStateFlow(Tab.HOME)

    val uiState: StateFlow<GreenBuddyUiState> = combine(
        repository.preferences,
        selectedTab,
    ) { preferences, tab ->
        val lessons = LessonCatalog.forSpecies(
            preferences.selectedStarter.companion.species,
        )

        GreenBuddyUiState(
            selectedTab = tab,
            selectedStarterId = preferences.selectedStarterId,
            onboardingComplete = preferences.onboardingComplete,
            lessonProgress = preferences.lessonProgress.normalizedFor(lessons),
            plantCareState = preferences.plantCareState,
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
            repository.setSelectedStarter(starterId)
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
        if (selectedAnswerIndex != currentLesson.quiz.correctAnswerIndex) return false

        val updatedProgress = state.lessonProgress.advanceWith(
            completedLessonId = currentLesson.id,
            rewardXp = currentLesson.rewardXp,
            totalLessons = lessons.size,
        )
        viewModelScope.launch {
            repository.saveLessonProgress(state.selectedStarterId, updatedProgress)
        }
        return true
    }

    fun performCareAction(action: CareAction) {
        val state = uiState.value
        val updatedCareState = state.plantCareState.apply(action)
        viewModelScope.launch {
            repository.savePlantCareState(state.selectedStarterId, updatedCareState)
        }
    }
}
