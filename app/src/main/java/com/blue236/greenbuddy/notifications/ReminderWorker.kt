package com.blue236.greenbuddy.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blue236.greenbuddy.data.GreenBuddyPreferencesRepository
import com.blue236.greenbuddy.model.CompanionPersonalitySystem
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
        val reminderType = ReminderDecider.notificationFor(
            snapshot = ReminderSnapshot(
                onboardingComplete = preferences.onboardingComplete,
                starterName = preferences.selectedStarter.companion.name,
                currentLessonTitle = progress.currentLessonOrNull(lessons)?.title,
                hasIncompleteLessons = !progress.isComplete(lessons),
                careState = preferences.plantCareState,
                reminderState = preferences.reminderState,
            ),
            nowMillis = System.currentTimeMillis(),
        )?.type ?: return Result.success()

        val localeTag = applicationContext.resources.configuration.locales[0]?.toLanguageTag().orEmpty()
        val reminder = CompanionPersonalitySystem.reminderCopy(
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
