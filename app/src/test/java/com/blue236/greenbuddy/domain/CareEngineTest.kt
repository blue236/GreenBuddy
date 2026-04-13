package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RewardState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CareEngineTest {
    private val engine = CareEngine(MissionEngine())
    private val today = LocalDate.of(2026, 3, 11)

    @Test
    fun performAction_marksHelpfulAndUpdatesCareState() {
        val result = engine.performAction(
            action = CareAction.WATER,
            currentCareState = PlantCareState(hydration = 20, sunlight = 60, nutrition = 70),
            currentLessonProgress = LessonProgress(),
            currentMissionProgress = DailyMissionProgress(missionDate = today.toString()),
            currentRewardState = RewardState(),
            today = today,
        )

        assertTrue(result.wasHelpful)
        assertEquals(38, result.updatedCareState.hydration)
        assertEquals(57, result.updatedCareState.sunlight)
        assertFalse(result.missionRewardOutcome.dailyAwarded)
        assertEquals(RewardState.careTokenReward(), result.missionRewardOutcome.rewardState.leafTokens)
    }

    @Test
    fun performAction_carriesMissionRewardOutcomeWhenCareCompletesSet() {
        val result = engine.performAction(
            action = CareAction.WATER,
            currentCareState = PlantCareState(hydration = 62, sunlight = 80, nutrition = 82),
            currentLessonProgress = LessonProgress(),
            currentMissionProgress = DailyMissionProgress(
                missionDate = today.toString(),
                completedLessonsToday = 1,
            ),
            currentRewardState = RewardState(),
            today = today,
        )

        assertTrue(result.wasHelpful)
        assertTrue(result.missionRewardOutcome.dailyAwarded)
        assertFalse(result.missionRewardOutcome.streakAwarded)
        assertEquals(
            RewardState.careTokenReward() + DailyMissionSet.DAILY_REWARD_TOKENS,
            result.missionRewardOutcome.rewardState.leafTokens,
        )
    }
}
