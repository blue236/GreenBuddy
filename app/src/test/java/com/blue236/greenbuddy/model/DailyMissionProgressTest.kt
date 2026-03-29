package com.blue236.greenbuddy.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
    fun completeDailyMissions_updatesStreakAndClaimsOnlyOnce() {
        val base = DailyMissionProgress(
            missionDate = today.toString(),
            currentStreak = 2,
            longestStreak = 2,
            lastCompletedDate = today.minusDays(1).toString(),
        )

        val rewarded = base.completeDailyMissions(today).claimStreakRewardIfEligible(today)
        val rewardedAgain = rewarded.completeDailyMissions(today).claimStreakRewardIfEligible(today)

        assertEquals(3, rewarded.currentStreak)
        assertEquals(today.toString(), rewarded.claimedDailyRewardDate)
        assertEquals(3, rewarded.streakRewardClaimedForStreak)
        assertEquals(rewarded, rewardedAgain)
    }

    @Test
    fun normalizedFor_resetsDailyProgressOnNewDay() {
        val previousDay = today.minusDays(1)
        val progress = DailyMissionProgress(
            missionDate = previousDay.toString(),
            completedCareActionsToday = 1,
            completedLessonsToday = 1,
            currentStreak = 4,
            longestStreak = 4,
            lastCompletedDate = previousDay.toString(),
            streakRewardClaimedForStreak = 3,
        )

        val normalized = progress.normalizedFor(today)

        assertEquals(today.toString(), normalized.missionDate)
        assertEquals(0, normalized.completedCareActionsToday)
        assertEquals(0, normalized.completedLessonsToday)
        assertEquals(4, normalized.currentStreak)
        assertEquals(3, normalized.streakRewardClaimedForStreak)
    }

    @Test
    fun normalizedFor_resetsBrokenStreakAfterMissedDay() {
        val progress = DailyMissionProgress(
            missionDate = today.minusDays(2).toString(),
            currentStreak = 4,
            longestStreak = 4,
            lastCompletedDate = today.minusDays(2).toString(),
            streakRewardClaimedForStreak = 3,
        )

        val normalized = progress.normalizedFor(today)

        assertEquals(0, normalized.currentStreak)
        assertEquals(4, normalized.longestStreak)
        assertNull(normalized.streakRewardClaimedForStreak)
    }

    @Test
    fun resolveForToday_marksWhenStreakWasRecentlyBroken() {
        val missionSet = DailyMissionProgress(
            missionDate = today.minusDays(2).toString(),
            currentStreak = 4,
            longestStreak = 4,
            lastCompletedDate = today.minusDays(2).toString(),
        ).resolveForToday(
            today = today,
            lessonProgress = LessonProgress(),
            careState = PlantCareState(hydration = 30, sunlight = 30, nutrition = 30),
        )

        assertTrue(missionSet.streakWasRecentlyBroken)
        assertEquals(0, missionSet.currentStreak)
    }

    @Test
    fun completeDailyMissions_restartsStreakAtOneAfterMissedDay() {
        val progress = DailyMissionProgress(
            missionDate = today.minusDays(2).toString(),
            currentStreak = 4,
            longestStreak = 4,
            lastCompletedDate = today.minusDays(2).toString(),
            streakRewardClaimedForStreak = 3,
        )

        val completed = progress.completeDailyMissions(today)

        assertEquals(1, completed.currentStreak)
        assertEquals(4, completed.longestStreak)
        assertEquals(today.toString(), completed.claimedDailyRewardDate)
        assertEquals(today.toString(), completed.lastCompletedDate)
        assertNull(completed.streakRewardClaimedForStreak)
    }

    @Test
    fun claimStreakRewardIfEligible_onlyClaimsOnMilestones() {
        val nonMilestone = DailyMissionProgress(
            missionDate = today.toString(),
            currentStreak = 2,
            longestStreak = 2,
            lastCompletedDate = today.toString(),
        )

        val claimed = nonMilestone.claimStreakRewardIfEligible(today)

        assertNull(claimed.streakRewardClaimedForStreak)
    }

    @Test
    fun resolveForToday_alwaysBuildsThreeChecklistItemsWithMatchingCompletedCount() {
        val missionSet = DailyMissionProgress(
            missionDate = today.toString(),
            completedCareActionsToday = 1,
            completedLessonsToday = 1,
        ).resolveForToday(
            today = today,
            lessonProgress = LessonProgress(),
            careState = PlantCareState(hydration = 20, sunlight = 20, nutrition = 20),
        )

        assertEquals(DailyMissionType.entries.size, missionSet.missions.size)
        assertEquals(
            missionSet.missions.count { it.isCompleted },
            missionSet.completedCount,
        )
        assertEquals(
            DailyMissionType.entries.toSet(),
            missionSet.missions.map { it.type }.toSet(),
        )
        assertFalse(missionSet.allCompletedToday)
    }
}
