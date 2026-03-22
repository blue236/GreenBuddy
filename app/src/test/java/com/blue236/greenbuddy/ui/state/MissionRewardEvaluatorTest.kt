package com.blue236.greenbuddy.ui.state

import com.blue236.greenbuddy.R
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

class MissionRewardEvaluatorTest {
    private val today = LocalDate.of(2026, 3, 11)
    private val completedCareState = PlantCareState(hydration = 80, sunlight = 80, nutrition = 80)

    @Test
    fun grantsDailyRewardOnlyOnceWhenMissionSetIsRetriedSameDay() {
        val progress = DailyMissionProgress(
            missionDate = today.toString(),
            completedCareActionsToday = 1,
            completedLessonsToday = 1,
        )
        val initialRewardState = RewardState()

        val first = evaluateMissionCompletionRewards(
            progress = progress,
            rewardState = initialRewardState,
            lessonProgress = LessonProgress(),
            careState = completedCareState,
            today = today,
        )
        val second = evaluateMissionCompletionRewards(
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
        assertEquals(R.string.reward_feedback_daily_mission_complete, first.feedbackMessageRes)

        assertFalse(second.dailyAwarded)
        assertFalse(second.streakAwarded)
        assertEquals(0, second.dailyRewardTokensAwarded)
        assertEquals(0, second.streakRewardTokensAwarded)
        assertEquals(first.rewardState.leafTokens, second.rewardState.leafTokens)
        assertEquals(first.progress, second.progress)
    }

    @Test
    fun threeDayMilestoneGrantsDailyAndStreakExactlyOnce() {
        val progress = DailyMissionProgress(
            missionDate = today.toString(),
            completedCareActionsToday = 1,
            completedLessonsToday = 1,
            currentStreak = 2,
            longestStreak = 2,
            lastCompletedDate = today.minusDays(1).toString(),
        )

        val first = evaluateMissionCompletionRewards(
            progress = progress,
            rewardState = RewardState(),
            lessonProgress = LessonProgress(),
            careState = completedCareState,
            today = today,
        )
        val second = evaluateMissionCompletionRewards(
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
        assertEquals(DailyMissionSet.DAILY_REWARD_TOKENS, first.dailyRewardTokensAwarded)
        assertEquals(DailyMissionSet.STREAK_REWARD_TOKENS, first.streakRewardTokensAwarded)
        assertEquals(
            DailyMissionSet.DAILY_REWARD_TOKENS + DailyMissionSet.STREAK_REWARD_TOKENS,
            first.rewardState.leafTokens,
        )
        assertEquals(R.string.reward_feedback_daily_mission_complete_with_streak, first.feedbackMessageRes)

        assertFalse(second.dailyAwarded)
        assertFalse(second.streakAwarded)
        assertEquals(first.rewardState.leafTokens, second.rewardState.leafTokens)
        assertEquals(first.progress, second.progress)
    }
}
