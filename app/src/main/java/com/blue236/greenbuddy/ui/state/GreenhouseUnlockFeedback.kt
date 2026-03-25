package com.blue236.greenbuddy.ui.state

fun composeGreenhouseUnlockFeedback(
    baseFeedback: String,
    unlockFeedback: String?,
): String = unlockFeedback
    ?.takeUnless { it.isBlank() }
    ?.let { "$baseFeedback · $it" }
    ?: baseFeedback
