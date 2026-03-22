package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GrowthStageStateTest {
    @Test
    fun resolveGrowthStageState_requiresBothXpAndCareForUnlock() {
        val state = resolveGrowthStageState(
            starterId = "monstera",
            progress = LessonProgress(totalXp = 45),
            careState = PlantCareState(hydration = 50, sunlight = 50, nutrition = 50),
            seenStageRank = 0,
        )

        assertEquals("Sprout", state.currentStage.title)
        assertEquals("Juvenile", state.nextStage?.title)
        assertTrue(state.requirementSummary.contains("care points more"))
        assertEquals("Reach 20 XP and care score 60+ to unlock Juvenile.", state.unlockHint)
    }

    @Test
    fun resolveGrowthStageState_usesSpeciesSpecificThresholds() {
        val basilState = resolveGrowthStageState(
            starterId = "basil",
            progress = LessonProgress(totalXp = 20),
            careState = PlantCareState(hydration = 58, sunlight = 58, nutrition = 58),
            seenStageRank = 0,
        )
        val tomatoState = resolveGrowthStageState(
            starterId = "tomato",
            progress = LessonProgress(totalXp = 20),
            careState = PlantCareState(hydration = 58, sunlight = 58, nutrition = 58),
            seenStageRank = 0,
        )

        assertEquals("Bushy", basilState.currentStage.title)
        assertEquals("Starter", tomatoState.currentStage.title)
    }

    @Test
    fun resolveGrowthStageState_marksNewUnlockWhenStageExceedsSeenRank() {
        val state = resolveGrowthStageState(
            starterId = "tomato",
            progress = LessonProgress(totalXp = 45),
            careState = PlantCareState(hydration = 84, sunlight = 82, nutrition = 82),
            seenStageRank = 1,
        )

        assertEquals("Fruiting", state.currentStage.title)
        assertTrue(state.newlyUnlocked)
        assertFalse(state.requirementSummary.contains("needs"))
    }

    @Test
    fun resolveGrowthStageState_downgradesWhenCareDropsBelowThreshold() {
        val state = resolveGrowthStageState(
            starterId = "tomato",
            progress = LessonProgress(totalXp = 45),
            careState = PlantCareState(hydration = 68, sunlight = 68, nutrition = 68),
            seenStageRank = 2,
        )

        assertEquals("Starter", state.currentStage.title)
        assertEquals("Flowering", state.nextStage?.title)
        assertFalse(state.newlyUnlocked)
        assertTrue(state.requirementSummary.contains("care points more"))
    }

    @Test
    fun resolveGrowthStageState_clearsNewUnlockAfterStageIsAcknowledged() {
        val state = resolveGrowthStageState(
            starterId = "basil",
            progress = LessonProgress(totalXp = 20),
            careState = PlantCareState(hydration = 58, sunlight = 58, nutrition = 58),
            seenStageRank = 1,
        )

        assertEquals("Bushy", state.currentStage.title)
        assertFalse(state.newlyUnlocked)
    }

    @Test
    fun resolveGrowthStageState_finalStageUsesUnlockedMessageAndFullProgress() {
        val state = resolveGrowthStageState(
            starterId = "monstera",
            progress = LessonProgress(totalXp = 80),
            careState = PlantCareState(hydration = 90, sunlight = 90, nutrition = 90),
            seenStageRank = 1,
        )

        assertEquals("Climbing", state.currentStage.title)
        assertEquals(null, state.nextStage)
        assertEquals(1f, state.progressToNextStage, 0.0001f)
        assertEquals(100, state.readinessPercent)
        assertEquals("All growth goals met for this starter.", state.requirementSummary)
        assertEquals(state.currentStage.unlockedMessage, state.unlockHint)
        assertTrue(state.newlyUnlocked)
    }
}
