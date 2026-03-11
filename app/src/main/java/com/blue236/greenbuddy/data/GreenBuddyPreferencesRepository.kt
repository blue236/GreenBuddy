package com.blue236.greenbuddy.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.blue236.greenbuddy.model.AppPreferences
import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.StarterPlants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GreenBuddyPreferencesRepository(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) },
    )

    val preferences: Flow<AppPreferences> = dataStore.data.map { prefs ->
        val selectedStarterId = prefs[SelectedStarterIdKey] ?: StarterPlants.options.first().id
        AppPreferences(
            onboardingComplete = prefs[OnboardingCompleteKey] ?: false,
            selectedStarterId = selectedStarterId,
            lessonProgress = readLessonProgress(prefs, selectedStarterId),
            plantCareState = readPlantCareState(prefs, selectedStarterId),
            dailyMissionProgress = readDailyMissionProgress(prefs, selectedStarterId),
        )
    }

    suspend fun setSelectedStarter(id: String) {
        dataStore.edit { prefs ->
            prefs[SelectedStarterIdKey] = id
        }
    }

    suspend fun completeOnboarding(selectedStarterId: String) {
        dataStore.edit { prefs ->
            prefs[SelectedStarterIdKey] = selectedStarterId
            prefs[OnboardingCompleteKey] = true
        }
    }

    suspend fun saveLessonProgress(starterId: String, progress: LessonProgress) {
        dataStore.edit { prefs ->
            prefs[currentLessonIndexKey(starterId)] = progress.currentLessonIndex
            prefs[completedLessonIdsKey(starterId)] = progress.completedLessonIds.joinToString(COMPLETED_IDS_SEPARATOR)
            prefs[totalXpKey(starterId)] = progress.totalXp
        }
    }

    suspend fun savePlantCareState(starterId: String, careState: PlantCareState) {
        dataStore.edit { prefs ->
            prefs[hydrationKey(starterId)] = careState.hydration
            prefs[sunlightKey(starterId)] = careState.sunlight
            prefs[nutritionKey(starterId)] = careState.nutrition
        }
    }

    suspend fun saveLessonAndMissionProgress(
        starterId: String,
        lessonProgress: LessonProgress,
        missionProgress: DailyMissionProgress,
    ) {
        dataStore.edit { prefs ->
            writeLessonProgress(prefs, starterId, lessonProgress)
            writeDailyMissionProgress(prefs, starterId, missionProgress)
        }
    }

    suspend fun saveCareStateAndMissionProgress(
        starterId: String,
        careState: PlantCareState,
        missionProgress: DailyMissionProgress,
    ) {
        dataStore.edit { prefs ->
            writePlantCareState(prefs, starterId, careState)
            writeDailyMissionProgress(prefs, starterId, missionProgress)
        }
    }

    suspend fun saveDailyMissionProgress(starterId: String, progress: DailyMissionProgress) {
        dataStore.edit { prefs ->
            writeDailyMissionProgress(prefs, starterId, progress)
        }
    }

    companion object {
        private const val DATASTORE_NAME = "greenbuddy_preferences"
        private const val COMPLETED_IDS_SEPARATOR = ","

        private val OnboardingCompleteKey = booleanPreferencesKey("onboarding_complete")
        private val SelectedStarterIdKey = stringPreferencesKey("selected_starter_id")

        private fun currentLessonIndexKey(starterId: String) = intPreferencesKey("${starterId}_current_lesson_index")
        private fun completedLessonIdsKey(starterId: String) = stringPreferencesKey("${starterId}_completed_lesson_ids")
        private fun totalXpKey(starterId: String) = intPreferencesKey("${starterId}_total_xp")
        private fun hydrationKey(starterId: String) = intPreferencesKey("${starterId}_hydration")
        private fun sunlightKey(starterId: String) = intPreferencesKey("${starterId}_sunlight")
        private fun nutritionKey(starterId: String) = intPreferencesKey("${starterId}_nutrition")
        private fun missionDateKey(starterId: String) = stringPreferencesKey("${starterId}_mission_date")
        private fun completedCareActionsTodayKey(starterId: String) = intPreferencesKey("${starterId}_completed_care_actions_today")
        private fun completedLessonsTodayKey(starterId: String) = intPreferencesKey("${starterId}_completed_lessons_today")
        private fun claimedDailyRewardDateKey(starterId: String) = stringPreferencesKey("${starterId}_claimed_daily_reward_date")
        private fun currentStreakKey(starterId: String) = intPreferencesKey("${starterId}_current_streak")
        private fun longestStreakKey(starterId: String) = intPreferencesKey("${starterId}_longest_streak")
        private fun lastCompletedDateKey(starterId: String) = stringPreferencesKey("${starterId}_last_completed_date")
        private fun leafTokensKey(starterId: String) = intPreferencesKey("${starterId}_leaf_tokens")
        private fun streakRewardClaimedForStreakKey(starterId: String) = intPreferencesKey("${starterId}_streak_reward_claimed_for_streak")
    }

    private fun readLessonProgress(prefs: Preferences, starterId: String): LessonProgress = LessonProgress(
        currentLessonIndex = prefs[currentLessonIndexKey(starterId)] ?: 0,
        completedLessonIds = prefs[completedLessonIdsKey(starterId)]
            ?.split(COMPLETED_IDS_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet(),
        totalXp = prefs[totalXpKey(starterId)] ?: 0,
    )

    private fun readPlantCareState(prefs: Preferences, starterId: String): PlantCareState {
        val starter = StarterPlants.options.firstOrNull { it.id == starterId } ?: StarterPlants.options.first()
        val defaultCare = PlantCareState.from(starter.companion)
        return PlantCareState(
            hydration = prefs[hydrationKey(starterId)] ?: defaultCare.hydration,
            sunlight = prefs[sunlightKey(starterId)] ?: defaultCare.sunlight,
            nutrition = prefs[nutritionKey(starterId)] ?: defaultCare.nutrition,
        )
    }

    private fun readDailyMissionProgress(prefs: Preferences, starterId: String): DailyMissionProgress = DailyMissionProgress(
        missionDate = prefs[missionDateKey(starterId)] ?: "",
        completedCareActionsToday = prefs[completedCareActionsTodayKey(starterId)] ?: 0,
        completedLessonsToday = prefs[completedLessonsTodayKey(starterId)] ?: 0,
        claimedDailyRewardDate = prefs[claimedDailyRewardDateKey(starterId)],
        currentStreak = prefs[currentStreakKey(starterId)] ?: 0,
        longestStreak = prefs[longestStreakKey(starterId)] ?: 0,
        lastCompletedDate = prefs[lastCompletedDateKey(starterId)],
        leafTokens = prefs[leafTokensKey(starterId)] ?: 0,
        streakRewardClaimedForStreak = prefs[streakRewardClaimedForStreakKey(starterId)],
    )

    private fun writeLessonProgress(prefs: MutablePreferences, starterId: String, progress: LessonProgress) {
        prefs[currentLessonIndexKey(starterId)] = progress.currentLessonIndex
        prefs[completedLessonIdsKey(starterId)] = progress.completedLessonIds.joinToString(COMPLETED_IDS_SEPARATOR)
        prefs[totalXpKey(starterId)] = progress.totalXp
    }

    private fun writePlantCareState(prefs: MutablePreferences, starterId: String, careState: PlantCareState) {
        prefs[hydrationKey(starterId)] = careState.hydration
        prefs[sunlightKey(starterId)] = careState.sunlight
        prefs[nutritionKey(starterId)] = careState.nutrition
    }

    private fun writeDailyMissionProgress(prefs: MutablePreferences, starterId: String, progress: DailyMissionProgress) {
        prefs[missionDateKey(starterId)] = progress.missionDate
        prefs[completedCareActionsTodayKey(starterId)] = progress.completedCareActionsToday
        prefs[completedLessonsTodayKey(starterId)] = progress.completedLessonsToday
        progress.claimedDailyRewardDate?.let { prefs[claimedDailyRewardDateKey(starterId)] = it } ?: prefs.remove(claimedDailyRewardDateKey(starterId))
        prefs[currentStreakKey(starterId)] = progress.currentStreak
        prefs[longestStreakKey(starterId)] = progress.longestStreak
        progress.lastCompletedDate?.let { prefs[lastCompletedDateKey(starterId)] = it } ?: prefs.remove(lastCompletedDateKey(starterId))
        prefs[leafTokensKey(starterId)] = progress.leafTokens
        progress.streakRewardClaimedForStreak?.let { prefs[streakRewardClaimedForStreakKey(starterId)] = it } ?: prefs.remove(streakRewardClaimedForStreakKey(starterId))
    }
}
