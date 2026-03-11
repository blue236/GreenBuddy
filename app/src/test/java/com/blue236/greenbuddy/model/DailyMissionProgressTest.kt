package com.blue236.greenbuddy.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyMissionProgressTest {
    private val today = LocalDate.of(2026, 3, 11)

    @Test
    fun resolveForToday_marksCareAndThresholdCompletionFromLiveState() {
        val progress = DailyMissionProgress(
            missionDate = today.toString(),
            completedCareActionsToday = 1,
        )

        val missionSet = progress.resolveForToday(
            today = today,
            lessonProgress = LessonProgress(),
            careState = PlantCareState(hydration = 80, sunlight = 80, nutrition = 80),
        )

        assertEquals(2, missionSet.completedCount)
        assertTrue(missionSet.missions.any { it.type == DailyMissionType.PERFORM_CARE_ACTION && it.isCompleted })
        assertTrue(missionSet.missions.any { it.type == DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD && it.isCompleted })
        assertFalse(missionSet.allCompletedToday)
    }

    @Test
    fun completeDailyMissions_awardsDailyAndStreakRewardOnlyOnce() {
        val base = DailyMissionProgress(
            missionDate = today.toString(),
            currentStreak = 2,
            longestStreak = 2,
            lastCompletedDate = today.minusDays(1).toString(),
        )

        val rewarded = base.completeDailyMissions(today).claimStreakRewardIfEligible(today)
        val rewardedAgain = rewarded.completeDailyMissions(today).claimStreakRewardIfEligible(today)

        assertEquals(3, rewarded.currentStreak)
        assertEquals(25, rewarded.leafTokens)
        assertEquals(25, rewardedAgain.leafTokens)
        assertEquals(3, rewardedAgain.streakRewardClaimedForStreak)
    }

    @Test
    fun normalizedFor_resetsDailyProgressOnNewDay() {
        val previousDay = today.minusDays(1)
        val progress = DailyMissionProgress(
            missionDate = previousDay.toString(),
            completedMissionIds = setOf("old"),
            completedCareActionsToday = 1,
            completedLessonsToday = 1,
            currentStreak = 4,
            longestStreak = 4,
            lastCompletedDate = previousDay.toString(),
            streakRewardClaimedForStreak = 3,
        )

        val normalized = progress.normalizedFor(today)

        assertEquals(today.toString(), normalized.missionDate)
        assertTrue(normalized.completedMissionIds.isEmpty())
        assertEquals(0, normalized.completedCareActionsToday)
        assertEquals(0, normalized.completedLessonsToday)
        assertEquals(3, normalized.streakRewardClaimedForStreak)
    }
}
