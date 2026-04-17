package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.FeedbackEvent
import com.blue236.greenbuddy.model.FeedbackEventType
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.StarterPlants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActionUiCoordinatorTest {
    private val missionOutcome = MissionRewardOutcome(
        progress = com.blue236.greenbuddy.model.DailyMissionProgress(),
        rewardState = com.blue236.greenbuddy.model.RewardState(),
        dailyAwarded = false,
        streakAwarded = false,
    )

    @Test
    fun lessonOutcome_combinesRewardFeedbackAndLessonEvent() {
        val coordinator = ActionUiCoordinator(
            rewardEngine = FakeRewardEngine(),
            feedbackCoordinator = FeedbackCoordinator(nextEventId = { 11L }),
            growthEngine = FakeGrowthEngine(unlocked = false),
        )

        val outcome = coordinator.lessonOutcome(
            starterId = "monstera",
            previousGrowthStage = 0,
            languageTag = "en",
            rewardXp = 20,
            missionOutcome = missionOutcome,
            unlockedStarter = StarterPlants.options.firstOrNull { it.id == "basil" },
            updatedLessonProgress = LessonProgress(),
            currentCareState = PlantCareState(60, 60, 60),
        )

        assertEquals("lesson:20+unlock:basil", outcome.rewardFeedback)
        assertEquals(FeedbackEvent(11L, FeedbackEventType.LESSON_SUCCESS), outcome.feedbackEvent)
    }

    @Test
    fun careOutcome_returnsNullEventWhenNotHelpful() {
        val coordinator = ActionUiCoordinator(
            rewardEngine = FakeRewardEngine(),
            feedbackCoordinator = FeedbackCoordinator(nextEventId = { 12L }),
            growthEngine = FakeGrowthEngine(unlocked = true),
        )

        val outcome = coordinator.careOutcome(
            starterId = "monstera",
            previousGrowthStage = 0,
            languageTag = "en",
            action = CareAction.WATER,
            wasHelpful = false,
            missionOutcome = missionOutcome,
            currentLessonProgress = LessonProgress(),
            updatedCareState = PlantCareState(60, 60, 60),
        )

        assertEquals("care:WATER:false", outcome.rewardFeedback)
        assertNull(outcome.feedbackEvent)
    }

    private class FakeGrowthEngine(private val unlocked: Boolean) : GrowthUnlockContract {
        override fun didUnlock(
            starterId: String,
            lessonProgress: LessonProgress,
            careState: PlantCareState,
            previousGrowthStageRank: Int,
        ): Boolean = unlocked
    }

    private class FakeRewardEngine : RewardEngineContract {
        override fun lessonFeedback(rewardXp: Int, missionOutcome: MissionRewardOutcome): String = "lesson:$rewardXp"

        override fun greenhouseUnlockFeedback(baseFeedback: String, unlockedStarter: com.blue236.greenbuddy.model.StarterPlantOption?, languageTag: String): String =
            if (unlockedStarter != null) "$baseFeedback+unlock:${unlockedStarter.id}" else baseFeedback

        override fun careFeedback(action: CareAction, languageTag: String, wasHelpful: Boolean, missionOutcome: MissionRewardOutcome): String =
            "care:${action.name}:$wasHelpful"

        override fun cosmeticFeedback(item: com.blue236.greenbuddy.model.CosmeticItem, languageTag: String): String = "unused"
    }
}
