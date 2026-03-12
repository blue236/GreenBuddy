package com.blue236.greenbuddy.model

import android.view.HapticFeedbackConstants
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackModelsTest {
    @Test
    fun lessonSuccess_usesConfirmHaptic() {
        assertEquals(HapticFeedbackConstants.CONFIRM, FeedbackEventType.LESSON_SUCCESS.hapticConstant())
    }

    @Test
    fun careSuccess_usesLightTapHaptic() {
        assertEquals(HapticFeedbackConstants.KEYBOARD_TAP, FeedbackEventType.CARE_SUCCESS.hapticConstant())
    }

    @Test
    fun growthUnlocked_usesCelebrationStyleHaptic() {
        assertEquals(HapticFeedbackConstants.LONG_PRESS, FeedbackEventType.GROWTH_UNLOCKED.hapticConstant())
    }
}
