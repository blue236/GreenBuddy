package com.blue236.greenbuddy.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.ReminderDecider
import com.blue236.greenbuddy.model.ReminderSnapshot
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val repository = GreenBuddyPreferencesRepository(applicationContext)
        val preferences = repository.currentPreferences()
        val lessons = LessonCatalog.forSpecies(preferences.selectedStarter.companion.species)
        val progress = preferences.lessonProgress
        val reminder = ReminderDecider.notificationFor(
            snapshot = ReminderSnapshot(
                onboardingComplete = preferences.onboardingComplete,
                starterName = preferences.selectedStarter.companion.name,
                currentLessonTitle = progress.currentLessonOrNull(lessons)?.title,
                hasIncompleteLessons = !progress.isComplete(lessons),
                careState = preferences.plantCareState,
                reminderState = preferences.reminderState,
            ),
            nowMillis = System.currentTimeMillis(),
        ) ?: return Result.success()

        ReminderNotifier.show(applicationContext, reminder)
        if (ReminderNotifier.canPostNotifications(applicationContext)) {
            repository.recordNotificationSent(System.currentTimeMillis())
        }
        return Result.success()
    }
}
