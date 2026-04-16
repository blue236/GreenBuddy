package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RewardState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActionPersistenceCoordinatorTest {
    private val coordinator = ActionPersistenceCoordinator()

    @Test
    fun lessonOutcome_buildsAnalyticsAndPersistsUnlockInfo() {
        val lessonProgress = LessonProgress(totalXp = 20)
        val missionProgress = DailyMissionProgress(completedLessonsToday = 1)
        val rewardState = RewardState(leafTokens = 12)

        val outcome = coordinator.lessonOutcome(
            starterId = "monstera",
            lessonId = "monstera_light",
            lessonProgress = lessonProgress,
            missionProgress = missionProgress,
            rewardState = rewardState,
            unlockedStarterId = "basil",
        )

        assertEquals("lesson_completed", outcome.analyticsEvent.name)
        assertEquals("monstera_light", outcome.analyticsEvent.params["lesson_id"])
        assertEquals("monstera", outcome.analyticsEvent.params["starter_id"])
        assertEquals(lessonProgress, outcome.lessonProgress)
        assertEquals(missionProgress, outcome.missionProgress)
        assertEquals(rewardState, outcome.rewardState)
        assertEquals("basil", outcome.unlockedStarterId)
    }

    @Test
    fun careOutcome_buildsAnalyticsAndPersistsUpdatedCareState() {
        val careState = PlantCareState(70, 60, 50)
        val missionProgress = DailyMissionProgress(completedCareActionsToday = 1)
        val rewardState = RewardState(leafTokens = 3)

        val outcome = coordinator.careOutcome(
            starterId = "monstera",
            actionName = "WATER",
            wasHelpful = true,
            updatedCareState = careState,
            missionProgress = missionProgress,
            rewardState = rewardState,
        )

        assertEquals("care_action", outcome.analyticsEvent.name)
        assertEquals("WATER", outcome.analyticsEvent.params["action"])
        assertEquals("monstera", outcome.analyticsEvent.params["starter_id"])
        assertEquals("true", outcome.analyticsEvent.params["helpful"])
        assertEquals(careState, outcome.updatedCareState)
        assertEquals(missionProgress, outcome.missionProgress)
        assertEquals(rewardState, outcome.rewardState)
    }
}
