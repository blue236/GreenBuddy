package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.FeedbackEvent
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.StarterPlantOption

interface RewardEngineContract {
    fun lessonFeedback(rewardXp: Int, missionOutcome: MissionRewardOutcome): String
    fun careFeedback(action: CareAction, languageTag: String, wasHelpful: Boolean, missionOutcome: MissionRewardOutcome): String
    fun greenhouseUnlockFeedback(baseFeedback: String, unlockedStarter: StarterPlantOption?, languageTag: String): String
}

interface GrowthUnlockContract {
    fun didUnlock(starterId: String, lessonProgress: LessonProgress, careState: PlantCareState, previousGrowthStageRank: Int): Boolean
}

data class ActionUiOutcome(
    val rewardFeedback: String,
    val feedbackEvent: FeedbackEvent?,
)

class ActionUiCoordinator(
    private val rewardEngine: RewardEngineContract,
    private val feedbackCoordinator: FeedbackCoordinator,
    private val growthEngine: GrowthUnlockContract,
) {
    fun lessonOutcome(
        starterId: String,
        previousGrowthStage: Int,
        languageTag: String,
        rewardXp: Int,
        missionOutcome: MissionRewardOutcome,
        unlockedStarter: StarterPlantOption?,
        updatedLessonProgress: LessonProgress,
        currentCareState: PlantCareState,
    ): ActionUiOutcome {
        val baseFeedback = rewardEngine.lessonFeedback(rewardXp, missionOutcome)
        val rewardFeedback = rewardEngine.greenhouseUnlockFeedback(baseFeedback, unlockedStarter, languageTag)
        val unlockedGrowth = growthEngine.didUnlock(
            starterId,
            updatedLessonProgress,
            currentCareState,
            previousGrowthStage,
        )
        return ActionUiOutcome(
            rewardFeedback = rewardFeedback,
            feedbackEvent = feedbackCoordinator.lessonEvent(unlockedGrowth),
        )
    }

    fun careOutcome(
        starterId: String,
        previousGrowthStage: Int,
        languageTag: String,
        action: CareAction,
        wasHelpful: Boolean,
        missionOutcome: MissionRewardOutcome,
        currentLessonProgress: LessonProgress,
        updatedCareState: PlantCareState,
    ): ActionUiOutcome {
        val rewardFeedback = rewardEngine.careFeedback(
            action = action,
            languageTag = languageTag,
            wasHelpful = wasHelpful,
            missionOutcome = missionOutcome,
        )
        val feedbackEvent = feedbackCoordinator.careEvent(
            wasHelpful = wasHelpful,
            unlockedGrowth = growthEngine.didUnlock(
                starterId,
                currentLessonProgress,
                updatedCareState,
                previousGrowthStage,
            ),
        )
        return ActionUiOutcome(
            rewardFeedback = rewardFeedback,
            feedbackEvent = feedbackEvent,
        )
    }
}
