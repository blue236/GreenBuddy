package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.recordCareAction
import java.time.LocalDate

data class CareActionResult(
    val updatedCareState: PlantCareState,
    val wasHelpful: Boolean,
    val missionRewardOutcome: MissionRewardOutcome,
)

class CareEngine(
    private val missionEngine: MissionEngine,
) {
    fun performAction(
        action: CareAction,
        currentCareState: PlantCareState,
        currentLessonProgress: LessonProgress,
        currentMissionProgress: DailyMissionProgress,
        currentRewardState: RewardState,
        today: LocalDate,
    ): CareActionResult {
        val updatedCareState = currentCareState.apply(action)
        val wasHelpful = updatedCareState.isMeaningfullyImprovedFrom(currentCareState)
        val missionOutcome = missionEngine.evaluateCompletionRewards(
            progress = currentMissionProgress.recordCareAction(today),
            rewardState = currentRewardState.rewardForCareAction(wasHelpful),
            lessonProgress = currentLessonProgress,
            careState = updatedCareState,
            today = today,
        )
        return CareActionResult(
            updatedCareState = updatedCareState,
            wasHelpful = wasHelpful,
            missionRewardOutcome = missionOutcome,
        )
    }
}
