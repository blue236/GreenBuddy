package com.blue236.greenbuddy.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.data.content.LessonContentLoader
import com.blue236.greenbuddy.data.content.ReminderCopyLoader
import com.blue236.greenbuddy.domain.CompanionCoordinator
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
        val localeTag = applicationContext.resources.configuration.locales[0]?.toLanguageTag().orEmpty()
        val lessonContentLoader = LessonContentLoader(applicationContext)
        val reminderCopyLoader = ReminderCopyLoader(applicationContext)
        val lessons = lessonContentLoader.lessonsFor(preferences.selectedStarter.companion.species, localeTag)
        val progress = preferences.lessonProgress
        val reminderSnapshot = ReminderSnapshot(
            onboardingComplete = preferences.onboardingComplete,
            starterName = preferences.selectedStarter.companion.name,
            currentLessonTitle = progress.currentLessonOrNull(lessons)?.title,
            hasIncompleteLessons = !progress.isComplete(lessons),
            careState = preferences.plantCareState,
            reminderState = preferences.reminderState,
        )
        val reminderType = ReminderDecider.notificationFor(
            snapshot = reminderSnapshot,
            nowMillis = System.currentTimeMillis(),
            copy = reminderCopyLoader.copyFor(localeTag),
        )?.type ?: return Result.success()

        val reminder = CompanionCoordinator().reminderCopy(
            type = reminderType,
            starter = preferences.selectedStarter,
            careState = preferences.plantCareState,
            lessonTitle = progress.currentLessonOrNull(lessons)?.title,
            languageTag = localeTag,
        )

        val shown = ReminderNotifier.show(applicationContext, reminder)
        if (shown) {
            repository.recordNotificationSent(System.currentTimeMillis())
        }
        return Result.success()
    }
}
