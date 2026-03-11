package com.blue236.greenbuddy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.blue236.greenbuddy.model.AppLanguage
import com.blue236.greenbuddy.model.AppPreferences
import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.RealPlantLogEntry
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.ReminderState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.defaultOwnedStarterIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GreenBuddyPreferencesRepository(context: Context) {
    private val dataStore = getDataStore(context.applicationContext)

    val preferences: Flow<AppPreferences> = dataStore.data.map { prefs ->
        val storedSelectedStarterId = prefs[SelectedStarterIdKey] ?: StarterPlants.options.first().id
        val ownedStarterIds = prefs[OwnedStarterIdsKey]?.split(COMPLETED_IDS_SEPARATOR)?.filter { it.isNotBlank() }?.toSet()?.ifEmpty { defaultOwnedStarterIds(storedSelectedStarterId) }
            ?: defaultOwnedStarterIds(storedSelectedStarterId)
        val resolvedSelectedStarterId = if (storedSelectedStarterId in ownedStarterIds) storedSelectedStarterId else ownedStarterIds.firstOrNull() ?: StarterPlants.options.first().id
        AppPreferences(
            onboardingComplete = prefs[OnboardingCompleteKey] ?: false,
            selectedStarterId = resolvedSelectedStarterId,
            ownedStarterIds = ownedStarterIds,
            lessonProgressByStarterId = StarterPlants.options.associate { it.id to readLessonProgress(prefs, it.id) },
            plantCareStateByStarterId = StarterPlants.options.associate { it.id to readPlantCareState(prefs, it.id) },
            dailyMissionProgress = readDailyMissionProgress(prefs, resolvedSelectedStarterId),
            seenGrowthStageRank = prefs[seenGrowthStageRankKey(resolvedSelectedStarterId)] ?: 0,
            rewardState = readRewardState(prefs),
            reminderState = readReminderState(prefs),
            realPlantModeStateByStarterId = StarterPlants.options.associate { it.id to readRealPlantModeState(prefs, it.id) },
            appLanguage = AppLanguage.fromStorageValue(prefs[appLanguageKey]),
        )
    }

    suspend fun currentPreferences(): AppPreferences = preferences.first()
    suspend fun setSelectedStarter(id: String) { dataStore.edit { if (id in ((it[OwnedStarterIdsKey]?.split(COMPLETED_IDS_SEPARATOR)?.filter(String::isNotBlank)?.toSet()) ?: defaultOwnedStarterIds(it[SelectedStarterIdKey] ?: StarterPlants.options.first().id))) it[SelectedStarterIdKey] = id } }
    suspend fun completeOnboarding(selectedStarterId: String) { dataStore.edit { it[SelectedStarterIdKey] = selectedStarterId; it[OwnedStarterIdsKey] = defaultOwnedStarterIds(selectedStarterId).joinToString(COMPLETED_IDS_SEPARATOR); it[OnboardingCompleteKey] = true } }
    suspend fun unlockStarter(starterId: String) { dataStore.edit { prefs -> val owned = ((prefs[OwnedStarterIdsKey]?.split(COMPLETED_IDS_SEPARATOR)?.filter(String::isNotBlank)?.toMutableSet()) ?: defaultOwnedStarterIds(prefs[SelectedStarterIdKey] ?: StarterPlants.options.first().id).toMutableSet()); owned += starterId; prefs[OwnedStarterIdsKey] = owned.joinToString(COMPLETED_IDS_SEPARATOR) } }
    suspend fun saveLessonAndMissionProgress(starterId: String, lessonProgress: LessonProgress, missionProgress: DailyMissionProgress) { dataStore.edit { writeLessonProgress(it, starterId, lessonProgress); writeDailyMissionProgress(it, starterId, missionProgress) } }
    suspend fun saveCareStateAndMissionProgress(starterId: String, careState: PlantCareState, missionProgress: DailyMissionProgress) { dataStore.edit { writePlantCareState(it, starterId, careState); writeDailyMissionProgress(it, starterId, missionProgress) } }
    suspend fun saveDailyMissionProgress(starterId: String, progress: DailyMissionProgress) { dataStore.edit { writeDailyMissionProgress(it, starterId, progress) } }
    suspend fun saveSeenGrowthStageRank(starterId: String, seenGrowthStageRank: Int) { dataStore.edit { it[seenGrowthStageRankKey(starterId)] = seenGrowthStageRank } }
    suspend fun saveRewardState(rewardState: RewardState) { dataStore.edit { it[globalLeafTokensKey] = rewardState.leafTokens; it[unlockedCosmeticIdsKey] = rewardState.unlockedCosmeticIds.joinToString(COMPLETED_IDS_SEPARATOR); rewardState.equippedCosmeticId?.let { id -> it[equippedCosmeticIdKey] = id } ?: it.remove(equippedCosmeticIdKey) } }
    suspend fun recordAppOpen(atMillis: Long) { dataStore.edit { it[lastAppOpenAtKey] = atMillis } }
    suspend fun recordLessonCompleted(atMillis: Long) { dataStore.edit { it[lastLessonCompletedAtKey] = atMillis } }
    suspend fun recordCareAction(atMillis: Long) { dataStore.edit { it[lastCareActionAtKey] = atMillis } }
    suspend fun recordNotificationSent(atMillis: Long) { dataStore.edit { it[lastNotificationSentAtKey] = atMillis } }
    suspend fun saveRealPlantModeState(starterId: String, realPlantModeState: RealPlantModeState) { dataStore.edit { it[realPlantModeEnabledKey(starterId)] = realPlantModeState.enabled; it[realPlantLogKey(starterId)] = encodeRealPlantEntries(realPlantModeState.entries) } }
    suspend fun saveRealPlantModeAndPlantCareState(starterId: String, realPlantModeState: RealPlantModeState, careState: PlantCareState) { dataStore.edit { prefs -> prefs[realPlantModeEnabledKey(starterId)] = realPlantModeState.enabled; prefs[realPlantLogKey(starterId)] = encodeRealPlantEntries(realPlantModeState.entries); writePlantCareState(prefs, starterId, careState) } }
    suspend fun saveAppLanguage(appLanguage: AppLanguage) { dataStore.edit { it[appLanguageKey] = appLanguage.storageValue } }

    companion object {
        private const val DATASTORE_NAME = "greenbuddy_preferences"

        @Volatile
        private var sharedDataStore: DataStore<Preferences>? = null

        private fun getDataStore(context: Context): DataStore<Preferences> =
            sharedDataStore ?: synchronized(this) {
                sharedDataStore ?: PreferenceDataStoreFactory.create(
                    produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) },
                ).also { sharedDataStore = it }
            }
        private const val COMPLETED_IDS_SEPARATOR = ","
        private const val LOG_ENTRY_SEPARATOR = ";"
        private const val LOG_FIELD_SEPARATOR = "|"
        private val OnboardingCompleteKey = booleanPreferencesKey("onboarding_complete")
        private val SelectedStarterIdKey = stringPreferencesKey("selected_starter_id")
        private val OwnedStarterIdsKey = stringPreferencesKey("owned_starter_ids")
        private val globalLeafTokensKey = intPreferencesKey("leaf_tokens")
        private val unlockedCosmeticIdsKey = stringPreferencesKey("unlocked_cosmetic_ids")
        private val equippedCosmeticIdKey = stringPreferencesKey("equipped_cosmetic_id")
        private val lastAppOpenAtKey = longPreferencesKey("last_app_open_at")
        private val lastLessonCompletedAtKey = longPreferencesKey("last_lesson_completed_at")
        private val lastCareActionAtKey = longPreferencesKey("last_care_action_at")
        private val lastNotificationSentAtKey = longPreferencesKey("last_notification_sent_at")
        private val appLanguageKey = stringPreferencesKey("app_language")
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
        private fun legacyLeafTokensKey(starterId: String) = intPreferencesKey("${starterId}_leaf_tokens")
        private fun streakRewardClaimedForStreakKey(starterId: String) = intPreferencesKey("${starterId}_streak_reward_claimed_for_streak")
        private fun seenGrowthStageRankKey(starterId: String) = intPreferencesKey("${starterId}_seen_growth_stage_rank")
        private fun realPlantModeEnabledKey(starterId: String) = booleanPreferencesKey("${starterId}_real_plant_mode_enabled")
        private fun realPlantLogKey(starterId: String) = stringPreferencesKey("${starterId}_real_plant_log")
    }

    private fun encodeRealPlantEntries(entries: List<RealPlantLogEntry>) = entries.joinToString(LOG_ENTRY_SEPARATOR) { "${it.loggedAtEpochMillis}${LOG_FIELD_SEPARATOR}${it.action.name}" }
    private fun readLessonProgress(prefs: Preferences, starterId: String) = LessonProgress(prefs[currentLessonIndexKey(starterId)] ?: 0, prefs[completedLessonIdsKey(starterId)]?.split(COMPLETED_IDS_SEPARATOR)?.filter(String::isNotBlank)?.toSet() ?: emptySet(), prefs[totalXpKey(starterId)] ?: 0)
    private fun readPlantCareState(prefs: Preferences, starterId: String): PlantCareState { val starter = StarterPlants.options.firstOrNull { it.id == starterId } ?: StarterPlants.options.first(); val d = PlantCareState.from(starter.companion); return PlantCareState(prefs[hydrationKey(starterId)] ?: d.hydration, prefs[sunlightKey(starterId)] ?: d.sunlight, prefs[nutritionKey(starterId)] ?: d.nutrition) }
    private fun readDailyMissionProgress(prefs: Preferences, starterId: String) = DailyMissionProgress(prefs[missionDateKey(starterId)] ?: "", prefs[completedCareActionsTodayKey(starterId)] ?: 0, prefs[completedLessonsTodayKey(starterId)] ?: 0, prefs[claimedDailyRewardDateKey(starterId)], prefs[currentStreakKey(starterId)] ?: 0, prefs[longestStreakKey(starterId)] ?: 0, prefs[lastCompletedDateKey(starterId)], prefs[streakRewardClaimedForStreakKey(starterId)])
    private fun readRewardState(prefs: Preferences): RewardState { val migrated = StarterPlants.options.sumOf { prefs[legacyLeafTokensKey(it.id)] ?: 0 }; return RewardState((prefs[globalLeafTokensKey] ?: 0) + migrated, prefs[unlockedCosmeticIdsKey]?.split(COMPLETED_IDS_SEPARATOR)?.filter(String::isNotBlank)?.toSet() ?: emptySet(), prefs[equippedCosmeticIdKey]) }
    private fun readReminderState(prefs: Preferences) = ReminderState(prefs[lastAppOpenAtKey], prefs[lastLessonCompletedAtKey], prefs[lastCareActionAtKey], prefs[lastNotificationSentAtKey])
    private fun readRealPlantModeState(prefs: Preferences, starterId: String) = RealPlantModeState(prefs[realPlantModeEnabledKey(starterId)] ?: false, prefs[realPlantLogKey(starterId)]?.split(LOG_ENTRY_SEPARATOR)?.mapNotNull { val parts = it.split(LOG_FIELD_SEPARATOR); val ts = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null; val action = parts.getOrNull(1)?.let { n -> RealPlantCareAction.entries.firstOrNull { a -> a.name == n } } ?: return@mapNotNull null; RealPlantLogEntry(action, ts) }?.sortedByDescending { it.loggedAtEpochMillis }?.take(RealPlantModeState.MAX_LOG_ENTRIES) ?: emptyList())
    private fun writeLessonProgress(prefs: MutablePreferences, starterId: String, progress: LessonProgress) { prefs[currentLessonIndexKey(starterId)] = progress.currentLessonIndex; prefs[completedLessonIdsKey(starterId)] = progress.completedLessonIds.joinToString(COMPLETED_IDS_SEPARATOR); prefs[totalXpKey(starterId)] = progress.totalXp }
    private fun writePlantCareState(prefs: MutablePreferences, starterId: String, careState: PlantCareState) { prefs[hydrationKey(starterId)] = careState.hydration; prefs[sunlightKey(starterId)] = careState.sunlight; prefs[nutritionKey(starterId)] = careState.nutrition }
    private fun writeDailyMissionProgress(prefs: MutablePreferences, starterId: String, progress: DailyMissionProgress) { prefs[missionDateKey(starterId)] = progress.missionDate; prefs[completedCareActionsTodayKey(starterId)] = progress.completedCareActionsToday; prefs[completedLessonsTodayKey(starterId)] = progress.completedLessonsToday; progress.claimedDailyRewardDate?.let { prefs[claimedDailyRewardDateKey(starterId)] = it } ?: prefs.remove(claimedDailyRewardDateKey(starterId)); prefs[currentStreakKey(starterId)] = progress.currentStreak; prefs[longestStreakKey(starterId)] = progress.longestStreak; progress.lastCompletedDate?.let { prefs[lastCompletedDateKey(starterId)] = it } ?: prefs.remove(lastCompletedDateKey(starterId)); progress.streakRewardClaimedForStreak?.let { prefs[streakRewardClaimedForStreakKey(starterId)] = it } ?: prefs.remove(streakRewardClaimedForStreakKey(starterId)) }
}
