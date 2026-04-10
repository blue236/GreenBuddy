package com.blue236.greenbuddy.domain

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

class MissionEngineTest {
    private val engine = MissionEngine()
    private val today = LocalDate.of(2026, 3, 11)
    private val completedCareState = PlantCareState(hydration = 80, sunlight = 80, nutrition = 80)

    @Test
    fun evaluateCompletionRewards_grantsDailyRewardOnlyOnce() {
        val progress = DailyMissionProgress(
            missionDate = today.toString(),
            completedCareActionsToday = 1,
            completedLessonsToday = 1,
        )

        val first = engine.evaluateCompletionRewards(
            progress = progress,
            rewardState = RewardState(),
            lessonProgress = LessonProgress(),
            careState = completedCareState,
            today = today,
        )
        val second = engine.evaluateCompletionRewards(
            progress = first.progress,
            rewardState = first.rewardState,
            lessonProgress = LessonProgress(),
            careState = completedCareState,
            today = today,
        )

        assertTrue(first.dailyAwarded)
        assertFalse(first.streakAwarded)
        assertEquals(DailyMissionSet.DAILY_REWARD_TOKENS, first.dailyRewardTokensAwarded)
        assertEquals(DailyMissionSet.DAILY_REWARD_TOKENS, first.rewardState.leafTokens)

        assertFalse(second.dailyAwarded)
        assertFalse(second.streakAwarded)
        assertEquals(first.rewardState.leafTokens, second.rewardState.leafTokens)
        assertEquals(first.progress, second.progress)
    }

    @Test
    fun evaluateCompletionRewards_grantsStreakRewardAtMilestoneOnlyOnce() {
        val progress = DailyMissionProgress(
            missionDate = today.toString(),
            completedCareActionsToday = 1,
            completedLessonsToday = 1,
            currentStreak = 2,
            longestStreak = 2,
            lastCompletedDate = today.minusDays(1).toString(),
        )

        val first = engine.evaluateCompletionRewards(
            progress = progress,
            rewardState = RewardState(),
            lessonProgress = LessonProgress(),
            careState = completedCareState,
            today = today,
        )
        val second = engine.evaluateCompletionRewards(
            progress = first.progress,
            rewardState = first.rewardState,
            lessonProgress = LessonProgress(),
            careState = completedCareState,
            today = today,
        )

        assertTrue(first.dailyAwarded)
        assertTrue(first.streakAwarded)
        assertEquals(3, first.progress.currentStreak)
        assertEquals(3, first.progress.streakRewardClaimedForStreak)
        assertEquals(
            DailyMissionSet.DAILY_REWARD_TOKENS + DailyMissionSet.STREAK_REWARD_TOKENS,
            first.rewardState.leafTokens,
        )

        assertFalse(second.dailyAwarded)
        assertFalse(second.streakAwarded)
        assertEquals(first.rewardState.leafTokens, second.rewardState.leafTokens)
    }
}
