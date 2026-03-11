package com.blue236.greenbuddy.model

private const val HOUR_MS = 60L * 60L * 1000L
private const val DAY_MS = 24L * HOUR_MS
private const val RECENT_APP_OPEN_WINDOW_MS = 18L * HOUR_MS
private const val NOTIFICATION_COOLDOWN_MS = 20L * HOUR_MS
private const val LESSON_REMINDER_DELAY_MS = 20L * HOUR_MS
private const val CARE_REMINDER_DELAY_MS = 16L * HOUR_MS
private const val STREAK_WARNING_DELAY_MS = 42L * HOUR_MS
private const val CARE_NEEDS_ATTENTION_THRESHOLD = 45

data class ReminderState(
    val lastAppOpenAtMillis: Long? = null,
    val lastLessonCompletedAtMillis: Long? = null,
    val lastCareActionAtMillis: Long? = null,
    val lastNotificationSentAtMillis: Long? = null,
)

enum class ReminderType {
    LESSON_READY,
    CARE,
    STREAK_WARNING,
}

data class ReminderNotification(
    val type: ReminderType,
    val title: String,
    val message: String,
)

data class ReminderSnapshot(
    val onboardingComplete: Boolean,
    val starterName: String,
    val currentLessonTitle: String?,
    val hasIncompleteLessons: Boolean,
    val careState: PlantCareState,
    val reminderState: ReminderState,
)

object ReminderDecider {
    fun notificationFor(snapshot: ReminderSnapshot, nowMillis: Long): ReminderNotification? {
        if (!snapshot.onboardingComplete) return null

        val lastAppOpenAt = snapshot.reminderState.lastAppOpenAtMillis
        if (lastAppOpenAt != null && nowMillis - lastAppOpenAt < RECENT_APP_OPEN_WINDOW_MS) return null

        val lastNotificationSentAt = snapshot.reminderState.lastNotificationSentAtMillis
        if (lastNotificationSentAt != null && nowMillis - lastNotificationSentAt < NOTIFICATION_COOLDOWN_MS) return null

        return when {
            shouldSendStreakWarning(snapshot, nowMillis) -> ReminderNotification(
                type = ReminderType.STREAK_WARNING,
                title = "Keep your GreenBuddy routine alive",
                message = "${snapshot.starterName} has missed you. Drop in today so the habit doesn’t go cold.",
            )

            shouldSendCareReminder(snapshot, nowMillis) -> ReminderNotification(
                type = ReminderType.CARE,
                title = "${snapshot.starterName} needs a quick care check",
                message = careMessageFor(snapshot.careState),
            )

            shouldSendLessonReminder(snapshot, nowMillis) -> ReminderNotification(
                type = ReminderType.LESSON_READY,
                title = "A GreenBuddy lesson is ready",
                message = snapshot.currentLessonTitle
                    ?.let { "Your next lesson, '$it', is waiting whenever you have a minute." }
                    ?: "Your next GreenBuddy lesson is ready whenever you have a minute.",
            )

            else -> null
        }
    }

    private fun shouldSendLessonReminder(snapshot: ReminderSnapshot, nowMillis: Long): Boolean {
        if (!snapshot.hasIncompleteLessons) return false
        val anchor = maxOfNonNull(
            snapshot.reminderState.lastLessonCompletedAtMillis,
            snapshot.reminderState.lastAppOpenAtMillis,
        ) ?: return true
        return nowMillis - anchor >= LESSON_REMINDER_DELAY_MS
    }

    private fun shouldSendCareReminder(snapshot: ReminderSnapshot, nowMillis: Long): Boolean {
        if (snapshot.careState.lowestStat > CARE_NEEDS_ATTENTION_THRESHOLD) return false
        val anchor = maxOfNonNull(
            snapshot.reminderState.lastCareActionAtMillis,
            snapshot.reminderState.lastAppOpenAtMillis,
        ) ?: return true
        return nowMillis - anchor >= CARE_REMINDER_DELAY_MS
    }

    private fun shouldSendStreakWarning(snapshot: ReminderSnapshot, nowMillis: Long): Boolean {
        val engagementAt = maxOfNonNull(
            snapshot.reminderState.lastAppOpenAtMillis,
            snapshot.reminderState.lastLessonCompletedAtMillis,
            snapshot.reminderState.lastCareActionAtMillis,
        ) ?: return false
        return nowMillis - engagementAt >= STREAK_WARNING_DELAY_MS
    }

    private fun careMessageFor(careState: PlantCareState): String = when (careState.lowestNeed) {
        CareAction.WATER -> "Hydration is dipping. A quick watering check would help a lot."
        CareAction.MOVE_TO_SUNLIGHT -> "Light levels are getting low. A brighter spot would perk things up."
        CareAction.FERTILIZE -> "Nutrition is running thin. A little fertilizer would help."
    }

    private fun maxOfNonNull(vararg values: Long?): Long? = values.filterNotNull().maxOrNull()
}
