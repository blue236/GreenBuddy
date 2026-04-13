package com.blue236.greenbuddy.model

import com.blue236.greenbuddy.data.content.ReminderCopy
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

    @Test
    fun notificationFor_usesInjectedCopyForLessonReminder() {
        val reminder = ReminderDecider.notificationFor(
            snapshot = baseSnapshot(),
            nowMillis = now,
            copy = mapOf(
                ReminderType.LESSON_READY to ReminderCopy(
                    title = "Custom lesson title",
                    messageWithLesson = "Start '{lessonTitle}' now.",
                    messageWithoutLesson = "Start your next lesson now.",
                ),
            ),
        )

        assertEquals(ReminderType.LESSON_READY, reminder?.type)
        assertEquals("Custom lesson title", reminder?.title)
        assertEquals("Start 'Reading Monstera leaves' now.", reminder?.message)
    }

    @Test
    fun notificationFor_usesInjectedStarterPlaceholderCopyForStreakReminder() {
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
            copy = mapOf(
                ReminderType.STREAK_WARNING to ReminderCopy(
                    title = "Stay in rhythm",
                    message = "{starterName} is waiting for you.",
                ),
            ),
        )

        assertEquals(ReminderType.STREAK_WARNING, reminder?.type)
        assertEquals("Stay in rhythm", reminder?.title)
        assertEquals("Mochi is waiting for you.", reminder?.message)
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
