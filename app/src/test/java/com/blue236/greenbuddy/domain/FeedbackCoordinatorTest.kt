package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.FeedbackEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeedbackCoordinatorTest {
    @Test
    fun lessonEvent_returnsLessonSuccessWhenGrowthNotUnlocked() {
        val coordinator = FeedbackCoordinator(nextEventId = { 101L })

        val event = coordinator.lessonEvent(unlockedGrowth = false)

        assertEquals(101L, event.id)
        assertEquals(FeedbackEventType.LESSON_SUCCESS, event.type)
    }

    @Test
    fun lessonEvent_returnsGrowthUnlockedWhenGrowthUnlocked() {
        val coordinator = FeedbackCoordinator(nextEventId = { 102L })

        val event = coordinator.lessonEvent(unlockedGrowth = true)

        assertEquals(102L, event.id)
        assertEquals(FeedbackEventType.GROWTH_UNLOCKED, event.type)
    }

    @Test
    fun careEvent_returnsNullWhenActionWasNotHelpful() {
        val coordinator = FeedbackCoordinator(nextEventId = { 103L })

        val event = coordinator.careEvent(wasHelpful = false, unlockedGrowth = true)

        assertNull(event)
    }

    @Test
    fun careEvent_returnsCareSuccessWhenHelpfulWithoutGrowthUnlock() {
        val coordinator = FeedbackCoordinator(nextEventId = { 104L })

        val event = coordinator.careEvent(wasHelpful = true, unlockedGrowth = false)

        requireNotNull(event)
        assertEquals(104L, event.id)
        assertEquals(FeedbackEventType.CARE_SUCCESS, event.type)
    }

    @Test
    fun careEvent_returnsGrowthUnlockedWhenHelpfulAndGrowthUnlocked() {
        val coordinator = FeedbackCoordinator(nextEventId = { 105L })

        val event = coordinator.careEvent(wasHelpful = true, unlockedGrowth = true)

        requireNotNull(event)
        assertEquals(105L, event.id)
        assertEquals(FeedbackEventType.GROWTH_UNLOCKED, event.type)
    }
}
