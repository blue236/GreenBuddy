package com.blue236.greenbuddy.model

import android.view.HapticFeedbackConstants

enum class FeedbackEventType {
    LESSON_SUCCESS,
    CARE_SUCCESS,
    GROWTH_UNLOCKED,
}

data class FeedbackEvent(
    val id: Long,
    val type: FeedbackEventType,
)

fun FeedbackEventType.hapticConstant(): Int = when (this) {
    FeedbackEventType.LESSON_SUCCESS -> HapticFeedbackConstants.CONFIRM
    FeedbackEventType.CARE_SUCCESS -> HapticFeedbackConstants.KEYBOARD_TAP
    FeedbackEventType.GROWTH_UNLOCKED -> HapticFeedbackConstants.LONG_PRESS
}
