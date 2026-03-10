package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeCompanionInsightsTest {
    private val lessons = listOf(
        Lesson("l1", "Light basics", "", "", "", listOf("A"), 0, 10),
        Lesson("l2", "Watering rhythm", "", "", "", listOf("A"), 0, 10),
        Lesson("l3", "Feeding time", "", "", "", listOf("A"), 0, 10),
        Lesson("l4", "Growth signals", "", "", "", listOf("A"), 0, 10),
    )

    @Test
    fun growthStageVisual_advancesWithLessonCompletion() {
        val progress = LessonProgress(completedLessonIds = setOf("l1", "l2", "l3"), totalXp = 30)

        val visual = progress.growthStageVisual(lessons)

        assertEquals("Young Plant", visual.title)
        assertEquals("🪴", visual.emoji)
        assertTrue(visual.progress > 0.7f)
    }

    @Test
    fun companionFeedback_prioritizesUrgentCareNeed() {
        val feedback = companionFeedback(
            plantName = "Leafling",
            careState = PlantCareState(hydration = 24, sunlight = 80, nutrition = 70),
            progress = LessonProgress(completedLessonIds = setOf("l1"), totalXp = 10),
            lessons = lessons,
        )

        assertTrue(feedback.title.contains("needs a drink"))
        assertEquals("Water now", feedback.focusLabel)
    }
}
