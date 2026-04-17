package com.blue236.greenbuddy.ui.state

import com.blue236.greenbuddy.data.content.CompanionCopySet
import com.blue236.greenbuddy.domain.CompanionCoordinator
import com.blue236.greenbuddy.domain.GrowthEngine
import com.blue236.greenbuddy.domain.MissionEngine
import com.blue236.greenbuddy.model.AppPreferences
import com.blue236.greenbuddy.model.FeedbackEvent
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.SeasonalWeatherProvider
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.Tab
import com.blue236.greenbuddy.model.WeatherAdviceGenerator
import com.blue236.greenbuddy.model.normalizedFor
import com.blue236.greenbuddy.model.WeatherProvider
import java.time.LocalDate

class UiStateAssembler(
    private val missionEngine: MissionEngine,
    private val growthEngine: GrowthEngine,
    private val companionCoordinator: CompanionCoordinator,
    private val lessonsForStarter: (starterId: String, localeTag: String) -> List<Lesson>,
    private val weatherProvider: WeatherProvider = SeasonalWeatherProvider,
) {
    fun assemble(
        preferences: AppPreferences,
        selectedTab: Tab,
        rewardFeedback: String?,
        feedbackEvent: FeedbackEvent?,
        localeTag: String,
        companionCopy: CompanionCopySet = CompanionCopySet(),
        today: LocalDate = LocalDate.now(),
    ): GreenBuddyUiState {
        val lessonsByStarterId = StarterPlants.options.associate { starter ->
            starter.id to lessonsForStarter(starter.id, localeTag)
        }
        val normalizedLessonProgressByStarterId = preferences.lessonProgressByStarterId.mapValues { (starterId, progress) ->
            progress.normalizedFor(lessonsByStarterId[starterId].orEmpty())
        }
        val selectedLessons = lessonsByStarterId[preferences.selectedStarter.id].orEmpty()
        val selectedLessonProgress = normalizedLessonProgressByStarterId[preferences.selectedStarter.id] ?: LessonProgress()
        val selectedCareState = preferences.plantCareState
        val normalizedDailyMissionProgress = preferences.dailyMissionProgress.normalizedFor(today)
        val todayMissions = missionEngine.resolveToday(
            normalizedDailyMissionProgress,
            selectedLessonProgress,
            selectedCareState,
            today,
        )
        val growthStageState = growthEngine.resolve(
            preferences.selectedStarter.id,
            selectedLessonProgress,
            selectedCareState,
            preferences.seenGrowthStageRank,
        )
        val weatherSnapshot = weatherProvider.snapshotFor(preferences.selectedWeatherCityId, today)
        val weatherAdvice = WeatherAdviceGenerator.adviceFor(preferences.selectedStarter, weatherSnapshot, localeTag)
        val companionSnapshot = companionCoordinator.snapshot(
            starter = preferences.selectedStarter,
            careState = selectedCareState,
            growthStageState = growthStageState,
            dailyMissionSet = todayMissions,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = preferences.realPlantModeState,
            recentConversationMemory = preferences.companionConversationMemory,
            languageTag = localeTag,
        )
        return GreenBuddyUiState(
            selectedTab = selectedTab,
            selectedStarterId = preferences.selectedStarter.id,
            ownedStarterIds = preferences.ownedStarterIds,
            onboardingComplete = preferences.onboardingComplete,
            lessons = selectedLessons,
            lessonProgressByStarterId = normalizedLessonProgressByStarterId,
            plantCareStateByStarterId = preferences.plantCareStateByStarterId,
            dailyMissionProgress = normalizedDailyMissionProgress,
            dailyMissionSet = todayMissions,
            growthStageState = growthStageState,
            rewardState = preferences.rewardState,
            rewardFeedback = rewardFeedback,
            feedbackEvent = feedbackEvent,
            realPlantModeState = preferences.realPlantModeState,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            companionStateSnapshot = companionSnapshot,
            companionHomeCheckIn = companionCoordinator.homeCheckIn(companionSnapshot, localeTag, companionCopy),
            appLanguage = preferences.appLanguage,
        )
    }
}
