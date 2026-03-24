package com.blue236.greenbuddy.ui.state

fun composeGreenhouseUnlockFeedback(
    baseFeedback: String,
    unlockFeedback: String?,
): String = unlockFeedback?.let { "$baseFeedback · $it" } ?: baseFeedback
