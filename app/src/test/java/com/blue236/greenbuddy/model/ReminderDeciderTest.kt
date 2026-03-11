package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderDeciderTest {
    private val now = 7L * 24L * 60L * 60L * 1000L

    @Test
    fun notificationFor_skipsReminderWhenAppWasOpenedRecently() {
        val reminder = ReminderDecider.notificationFor(
            snapshot = baseSnapshot(
                reminderState = ReminderState(lastAppOpenAtMillis = now - 2L * 60L * 60L * 1000L),
            ),
            nowMillis = now,
        )

        assertNull(reminder)
    }

    @Test
    fun notificationFor_prioritizesStreakWarningAfterLongAbsence() {
        val reminder = ReminderDecider.notificationFor(
            snapshot = baseSnapshot(
                careState = PlantCareState(hydration = 20, sunlight = 30, nutrition = 40),
                reminderState = ReminderState(
                    lastAppOpenAtMillis = now - 50L * 60L * 60L * 1000L,
                    lastCareActionAtMillis = now - 50L * 60L * 60L * 1000L,
                    lastLessonCompletedAtMillis = now - 50L * 60L * 60L * 1000L,
                ),
            ),
            nowMillis = now,
        )

        assertEquals(ReminderType.STREAK_WARNING, reminder?.type)
    }

    @Test
    fun notificationFor_sendsCareReminderBeforeLessonWhenCareIsLow() {
        val reminder = ReminderDecider.notificationFor(
            snapshot = baseSnapshot(
                careState = PlantCareState(hydration = 25, sunlight = 70, nutrition = 72),
                reminderState = ReminderState(
                    lastAppOpenAtMillis = now - 30L * 60L * 60L * 1000L,
                    lastCareActionAtMillis = now - 30L * 60L * 60L * 1000L,
                    lastLessonCompletedAtMillis = now - 30L * 60L * 60L * 1000L,
                ),
            ),
            nowMillis = now,
        )

        assertEquals(ReminderType.CARE, reminder?.type)
    }

    @Test
    fun notificationFor_sendsLessonReminderWhenTrackStillHasLessons() {
        val reminder = ReminderDecider.notificationFor(
            snapshot = baseSnapshot(
                reminderState = ReminderState(
                    lastAppOpenAtMillis = now - 30L * 60L * 60L * 1000L,
                    lastLessonCompletedAtMillis = now - 30L * 60L * 60L * 1000L,
                ),
            ),
            nowMillis = now,
        )

        assertEquals(ReminderType.LESSON_READY, reminder?.type)
    }

    @Test
    fun notificationFor_respectsNotificationCooldown() {
        val reminder = ReminderDecider.notificationFor(
            snapshot = baseSnapshot(
                reminderState = ReminderState(
                    lastAppOpenAtMillis = now - 30L * 60L * 60L * 1000L,
                    lastLessonCompletedAtMillis = now - 30L * 60L * 60L * 1000L,
                    lastNotificationSentAtMillis = now - 4L * 60L * 60L * 1000L,
                ),
            ),
            nowMillis = now,
        )

        assertNull(reminder)
    }

    private fun baseSnapshot(
        onboardingComplete: Boolean = true,
        careState: PlantCareState = PlantCareState(hydration = 80, sunlight = 76, nutrition = 72),
        reminderState: ReminderState = ReminderState(),
    ) = ReminderSnapshot(
        onboardingComplete = onboardingComplete,
        starterName = "Mochi",
        currentLessonTitle = "Reading Monstera leaves",
        hasIncompleteLessons = true,
        careState = careState,
        reminderState = reminderState,
    )
}
