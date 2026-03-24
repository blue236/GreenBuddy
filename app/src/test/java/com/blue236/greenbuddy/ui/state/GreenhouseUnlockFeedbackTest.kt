package com.blue236.greenbuddy.ui.state

import org.junit.Assert.assertEquals
import org.junit.Test

class GreenhouseUnlockFeedbackTest {
    @Test
    fun returnsBaseFeedbackWhenNoGreenhouseBuddyUnlocks() {
        val base = "Lesson complete · +20 XP · +10 leaf tokens"

        val result = composeGreenhouseUnlockFeedback(
            baseFeedback = base,
            unlockFeedback = null,
        )

        assertEquals(base, result)
    }

    @Test
    fun appendsUnlockFeedbackWhenGreenhouseBuddyUnlocks() {
        val base = "Lesson complete · +20 XP · +10 leaf tokens"
        val unlock = "New greenhouse buddy unlocked: Basil 🌱"

        val result = composeGreenhouseUnlockFeedback(
            baseFeedback = base,
            unlockFeedback = unlock,
        )

        assertEquals(
            "Lesson complete · +20 XP · +10 leaf tokens · New greenhouse buddy unlocked: Basil 🌱",
            result,
        )
    }

    @Test
    fun preservesLocalizedUnlockFeedbackWithoutChangingIt() {
        val base = "Lektion abgeschlossen · +20 XP · +10 Blatt-Token"
        val unlock = "Neuer Gewächshaus-Buddy freigeschaltet: Basilikum 🌱"

        val result = composeGreenhouseUnlockFeedback(
            baseFeedback = base,
            unlockFeedback = unlock,
        )

        assertEquals(
            "Lektion abgeschlossen · +20 XP · +10 Blatt-Token · Neuer Gewächshaus-Buddy freigeschaltet: Basilikum 🌱",
            result,
        )
    }
}
