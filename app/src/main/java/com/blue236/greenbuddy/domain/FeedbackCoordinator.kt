package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.FeedbackEvent
import com.blue236.greenbuddy.model.FeedbackEventType

class FeedbackCoordinator(
    private val nextEventId: () -> Long = { System.nanoTime() },
) {
    fun lessonEvent(unlockedGrowth: Boolean): FeedbackEvent =
        build(if (unlockedGrowth) FeedbackEventType.GROWTH_UNLOCKED else FeedbackEventType.LESSON_SUCCESS)

    fun careEvent(wasHelpful: Boolean, unlockedGrowth: Boolean): FeedbackEvent? {
        if (!wasHelpful) return null
        return build(if (unlockedGrowth) FeedbackEventType.GROWTH_UNLOCKED else FeedbackEventType.CARE_SUCCESS)
    }

    private fun build(type: FeedbackEventType): FeedbackEvent =
        FeedbackEvent(
            id = nextEventId(),
            type = type,
        )
}
