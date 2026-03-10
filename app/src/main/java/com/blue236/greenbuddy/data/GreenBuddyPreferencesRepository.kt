package com.blue236.greenbuddy.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.blue236.greenbuddy.model.AppPreferences
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
}
