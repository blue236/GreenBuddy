package com.blue236.greenbuddy.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.Tab
import com.blue236.greenbuddy.model.advanceWith
import com.blue236.greenbuddy.model.claimStreakRewardIfEligible
import com.blue236.greenbuddy.model.completeDailyMissions
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.model.nextUnlockableStarterId
import com.blue236.greenbuddy.model.normalizedFor
import com.blue236.greenbuddy.model.recordCareAction
import com.blue236.greenbuddy.model.recordLessonCompletion
import com.blue236.greenbuddy.model.resolveForToday
import com.blue236.greenbuddy.model.resolveGrowthStageState
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
    private val rewardFeedback = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.preferences.collect { preferences ->
                val normalizedMissionProgress = preferences.dailyMissionProgress.normalizedFor(LocalDate.now())
                if (normalizedMissionProgress != preferences.dailyMissionProgress) {
                    repository.saveDailyMissionProgress(preferences.selectedStarter.id, normalizedMissionProgress)
                }
            }
        }
    }

    val uiState: StateFlow<GreenBuddyUiState> = combine(
        repository.preferences,
        selectedTab,
        rewardFeedback,
    ) { preferences, tab, feedback ->
        val normalizedLessonProgressByStarterId = preferences.lessonProgressByStarterId.mapValues { (starterId, progress) ->
            val starter = StarterPlants.options.first { it.id == starterId }
            progress.normalizedFor(LessonCatalog.forSpecies(starter.companion.species))
        }
        val selectedLessonProgress = normalizedLessonProgressByStarterId[preferences.selectedStarter.id] ?: LessonProgress()
        val selectedCareState = preferences.plantCareState
        val normalizedDailyMissionProgress = preferences.dailyMissionProgress.normalizedFor(LocalDate.now())
        val todayMissions = normalizedDailyMissionProgress.resolveForToday(
            today = LocalDate.now(),
            lessonProgress = selectedLessonProgress,
            careState = selectedCareState,
        )
        val growthStageState = resolveGrowthStageState(
            starterId = preferences.selectedStarter.id,
            progress = selectedLessonProgress,
            careState = selectedCareState,
            seenStageRank = preferences.seenGrowthStageRank,
        )

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
        if (selectedAnswerIndex != currentLesson.quiz.correctAnswerIndex) return false

        val today = LocalDate.now()
        val updatedLessonProgress = state.lessonProgress.advanceWith(
            completedLessonId = currentLesson.id,
            rewardXp = currentLesson.rewardXp,
            totalLessons = lessons.size,
        )
        val lessonReward = RewardState.lessonTokenReward(currentLesson.rewardXp)
        val rewardOutcome = rewardIfMissionSetCompleted(
            progress = state.dailyMissionProgress.recordLessonCompletion(today),
            rewardState = state.rewardState.rewardForLesson(currentLesson.rewardXp),
            lessonProgress = updatedLessonProgress,
            careState = state.plantCareState,
            today = today,
        )
        val shouldUnlockNextPlant = updatedLessonProgress.isComplete(lessons) && nextUnlockableStarterId(state.ownedStarterIds) != null
        rewardFeedback.value = buildString {
            append("Lesson complete · +${currentLesson.rewardXp} XP · +$lessonReward leaf tokens")
            if (rewardOutcome.dailyAwarded) append(" · daily missions cleared +${rewardOutcome.dailyRewardTokensAwarded}")
            if (rewardOutcome.streakAwarded) append(" · streak bonus +${rewardOutcome.streakRewardTokensAwarded}")
        }

        viewModelScope.launch {
            repository.saveLessonAndMissionProgress(
                starterId = state.selectedStarterId,
                lessonProgress = updatedLessonProgress,
                missionProgress = rewardOutcome.progress,
            )
            repository.saveRewardState(rewardOutcome.rewardState)
            if (shouldUnlockNextPlant) {
                nextUnlockableStarterId(state.ownedStarterIds)?.let { repository.unlockStarter(it) }
            }
        }
        return true
    }

    fun performCareAction(action: CareAction) {
        val state = uiState.value
        val today = LocalDate.now()
        val updatedCareState = state.plantCareState.apply(action)
        val wasHelpful = updatedCareState.isMeaningfullyImprovedFrom(state.plantCareState)
        val careReward = if (wasHelpful) RewardState.careTokenReward() else 0
        val rewardOutcome = rewardIfMissionSetCompleted(
            progress = state.dailyMissionProgress.recordCareAction(today),
            rewardState = state.rewardState.rewardForCareAction(wasHelpful),
            lessonProgress = state.lessonProgress,
            careState = updatedCareState,
            today = today,
        )
        rewardFeedback.value = buildString {
            append(
                if (wasHelpful) {
                    "${action.label} helped · +$careReward leaf tokens"
                } else {
                    "${action.label} had no helpful effect · no leaf tokens"
                },
            )
            if (rewardOutcome.dailyAwarded) append(" · daily missions cleared +${rewardOutcome.dailyRewardTokensAwarded}")
            if (rewardOutcome.streakAwarded) append(" · streak bonus +${rewardOutcome.streakRewardTokensAwarded}")
        }

        viewModelScope.launch {
            repository.saveCareStateAndMissionProgress(
                starterId = state.selectedStarterId,
                careState = updatedCareState,
                missionProgress = rewardOutcome.progress,
            )
            repository.saveRewardState(rewardOutcome.rewardState)
        }
    }

    fun purchaseCosmetic(item: CosmeticItem) {
        val state = uiState.value
        if (!state.rewardState.canPurchase(item)) return

        val updatedRewardState = state.rewardState.purchase(item)
        val spent = state.rewardState.leafTokens - updatedRewardState.leafTokens
        val autoEquipped = updatedRewardState.equippedCosmeticId == item.id && state.rewardState.equippedCosmeticId == null
        rewardFeedback.value = buildString {
            append("Unlocked ${item.name} ${item.emoji} · -$spent leaf tokens")
            append(" · ${updatedRewardState.leafTokens} left")
            if (autoEquipped) {
                append(" · auto-equipped")
            }
        }
        viewModelScope.launch {
            repository.saveRewardState(updatedRewardState)
        }
    }

    fun equipCosmetic(itemId: String) {
        val state = uiState.value
        val updatedRewardState = state.rewardState.equip(itemId)
        if (updatedRewardState == state.rewardState) return

        val item = updatedRewardState.equippedCosmetic
        rewardFeedback.value = "Equipped ${item?.name ?: "new cosmetic"} ${item?.emoji.orEmpty()}"
        viewModelScope.launch {
            repository.saveRewardState(updatedRewardState)
        }
    }

    fun acknowledgeGrowthStage() {
        val state = uiState.value
        viewModelScope.launch {
            repository.saveSeenGrowthStageRank(
                starterId = state.selectedStarterId,
                seenGrowthStageRank = state.growthStageState.currentStage.rank,
            )
        }
    }

    private fun rewardIfMissionSetCompleted(
        progress: DailyMissionProgress,
        rewardState: RewardState,
        lessonProgress: LessonProgress,
        careState: PlantCareState,
        today: LocalDate,
    ): RewardOutcome {
        val missionSet = progress.resolveForToday(today, lessonProgress, careState)
        if (!missionSet.allCompletedToday) {
            return RewardOutcome(progress = progress.normalizedFor(today), rewardState = rewardState)
        }

        var updatedProgress = progress.completeDailyMissions(today)
        var updatedRewardState = rewardState
        var dailyAwarded = false
        var streakAwarded = false

        if (updatedProgress.claimedDailyRewardDate == today.toString() && progress.claimedDailyRewardDate != today.toString()) {
            updatedRewardState = updatedRewardState.rewardForDailyMissionCompletion()
            dailyAwarded = true
        }

        val beforeStreakClaim = updatedProgress.streakRewardClaimedForStreak
        updatedProgress = updatedProgress.claimStreakRewardIfEligible(today)
        if (updatedProgress.streakRewardClaimedForStreak != beforeStreakClaim) {
            updatedRewardState = updatedRewardState.rewardForStreakBonus()
            streakAwarded = true
        }

        return RewardOutcome(
            progress = updatedProgress,
            rewardState = updatedRewardState,
            dailyAwarded = dailyAwarded,
            streakAwarded = streakAwarded,
        )
    }
}

private data class RewardOutcome(
    val progress: DailyMissionProgress,
    val rewardState: RewardState,
    val dailyAwarded: Boolean = false,
    val streakAwarded: Boolean = false,
) {
    val dailyRewardTokensAwarded: Int = if (dailyAwarded) com.blue236.greenbuddy.model.DailyMissionSet.DAILY_REWARD_TOKENS else 0
    val streakRewardTokensAwarded: Int = if (streakAwarded) com.blue236.greenbuddy.model.DailyMissionSet.STREAK_REWARD_TOKENS else 0
}
