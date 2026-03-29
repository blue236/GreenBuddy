package com.blue236.greenbuddy.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CompanionChatEngine
import com.blue236.greenbuddy.model.DailyMission
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.DailyMissionType
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.WeatherAdviceGenerator
import com.blue236.greenbuddy.model.WeatherCatalog
import com.blue236.greenbuddy.model.WeatherSeason
import com.blue236.greenbuddy.model.WeatherSnapshot
import com.blue236.greenbuddy.model.resolveGrowthStageState
import com.blue236.greenbuddy.ui.theme.GreenBuddyTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class HomeScreenMissionCardTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dailyMissionCard_showsChecklistRowsAndMissionCopy() {
        val starter = StarterPlants.options.first()
        val lessons = LessonCatalog.forSpecies(starter.companion.species)
        val progress = LessonProgress()
        val careState = PlantCareState(hydration = 82, sunlight = 68, nutrition = 54)
        val growthStageState = resolveGrowthStageState(starter.id, progress, careState)
        val weatherSnapshot = WeatherSnapshot(
            city = WeatherCatalog.cityOptions.first(),
            season = WeatherSeason.SPRING,
            condition = WeatherCatalog.cityOptions.first().climateBySeason.getValue(WeatherSeason.SPRING),
        )
        val weatherAdvice = WeatherAdviceGenerator.adviceFor(starter, weatherSnapshot)
        val missionSet = DailyMissionSet(
            date = LocalDate.of(2026, 3, 21),
            missions = listOf(
                DailyMission(
                    id = "lesson",
                    type = DailyMissionType.COMPLETE_LESSON,
                    title = "ignored title",
                    description = "ignored description",
                    isCompleted = true,
                ),
                DailyMission(
                    id = "care",
                    type = DailyMissionType.PERFORM_CARE_ACTION,
                    title = "ignored title",
                    description = "ignored description",
                    isCompleted = false,
                ),
                DailyMission(
                    id = "stat",
                    type = DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD,
                    title = "ignored title",
                    description = "ignored description",
                    isCompleted = false,
                    statType = com.blue236.greenbuddy.model.CareStatType.SUNLIGHT,
                    threshold = 70,
                ),
            ),
            currentStreak = 2,
            longestStreak = 4,
            allCompletedToday = false,
            dailyRewardClaimed = false,
            streakRewardClaimedForStreak = null,
        )
        val companionSnapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthStageState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )
        val companionCheckIn = CompanionChatEngine.proactiveCheckIn(companionSnapshot)

        composeTestRule.setContent {
            GreenBuddyTheme {
                HomeScreen(
                    starter = starter,
                    lessons = lessons,
                    progress = progress,
                    careState = careState,
                    dailyMissionSet = missionSet,
                    growthStageState = growthStageState,
                    greenhouseCount = 1,
                    rewardState = RewardState(),
                    rewardFeedback = null,
                    realPlantModeState = RealPlantModeState(),
                    weatherSnapshot = weatherSnapshot,
                    weatherAdvice = weatherAdvice,
                    companionStateSnapshot = companionSnapshot,
                    companionHomeCheckIn = companionCheckIn,
                    onPerformCareAction = {},
                    onSubmitCompanionChatMessage = {},
                    onAcknowledgeGrowthStage = {},
                    onSetRealPlantModeEnabled = {},
                    onLogRealPlantCare = {},
                )
            }
        }

        val activity = composeTestRule.activity
        composeTestRule.onNodeWithText(activity.getString(R.string.daily_missions)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.daily_mission_progress_arc_general, 1, 3)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.completed_of_total, 1, 3)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.daily_mission_streak_chip, 2)).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(activity.getString(R.string.daily_mission_reward_chip, 5)).assertCountEquals(4)

        composeTestRule.onNodeWithText(activity.getString(R.string.mission_complete_lesson_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.mission_complete_lesson_description)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.mission_care_action_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.mission_care_action_description)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.mission_keep_stat_title, "Sunlight", 70)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.mission_keep_stat_description, "Sunlight", 70)).assertIsDisplayed()

        composeTestRule.onNodeWithText(activity.getString(R.string.daily_mission_reward_summary_incomplete, 15)).assertIsDisplayed()
        composeTestRule.onAllNodesWithText("✓").assertCountEquals(1)
    }

    @Test
    fun rewardOverviewCard_showsNextCosmeticGoalAndRewardFeedback() {
        val starter = StarterPlants.options.first()
        val lessons = LessonCatalog.forSpecies(starter.companion.species)
        val progress = LessonProgress()
        val careState = PlantCareState(hydration = 82, sunlight = 68, nutrition = 54)
        val growthStageState = resolveGrowthStageState(starter.id, progress, careState)
        val weatherSnapshot = WeatherSnapshot(
            city = WeatherCatalog.cityOptions.first(),
            season = WeatherSeason.SPRING,
            condition = WeatherCatalog.cityOptions.first().climateBySeason.getValue(WeatherSeason.SPRING),
        )
        val weatherAdvice = WeatherAdviceGenerator.adviceFor(starter, weatherSnapshot)
        val companionSnapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthStageState,
            dailyMissionSet = null,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )
        val companionCheckIn = CompanionChatEngine.proactiveCheckIn(companionSnapshot)
        val rewardState = RewardState(leafTokens = 5)

        composeTestRule.setContent {
            GreenBuddyTheme {
                HomeScreen(
                    starter = starter,
                    lessons = lessons,
                    progress = progress,
                    careState = careState,
                    dailyMissionSet = null,
                    growthStageState = growthStageState,
                    greenhouseCount = 1,
                    rewardState = rewardState,
                    rewardFeedback = "Lesson complete · +10 leaf tokens",
                    realPlantModeState = RealPlantModeState(),
                    weatherSnapshot = weatherSnapshot,
                    weatherAdvice = weatherAdvice,
                    companionStateSnapshot = companionSnapshot,
                    companionHomeCheckIn = companionCheckIn,
                    onPerformCareAction = {},
                    onSubmitCompanionChatMessage = {},
                    onAcknowledgeGrowthStage = {},
                    onSetRealPlantModeEnabled = {},
                    onLogRealPlantCare = {},
                )
            }
        }

        val activity = composeTestRule.activity
        composeTestRule.onNodeWithText(activity.getString(R.string.reward_pulse)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.wallet_value, 5)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.reward_next_unlock_chip, "🪴", "Classic Clay Pot")).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.reward_tokens_needed_chip, 15)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Lesson complete · +10 leaf tokens").assertIsDisplayed()
    }

    @Test
    fun growthUnlockCard_showsUnlockedStageCtaOnHome() {
        val starter = StarterPlants.options.first { it.id == "basil" }
        val lessons = LessonCatalog.forSpecies(starter.companion.species)
        val progress = LessonProgress(totalXp = 20)
        val careState = PlantCareState(hydration = 58, sunlight = 58, nutrition = 58)
        val growthStageState = resolveGrowthStageState(starter.id, progress, careState, seenStageRank = 0)
        val weatherSnapshot = WeatherSnapshot(
            city = WeatherCatalog.cityOptions.first(),
            season = WeatherSeason.SPRING,
            condition = WeatherCatalog.cityOptions.first().climateBySeason.getValue(WeatherSeason.SPRING),
        )
        val weatherAdvice = WeatherAdviceGenerator.adviceFor(starter, weatherSnapshot)
        val companionSnapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthStageState,
            dailyMissionSet = null,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )
        val companionCheckIn = CompanionChatEngine.proactiveCheckIn(companionSnapshot)

        composeTestRule.setContent {
            GreenBuddyTheme {
                HomeScreen(
                    starter = starter,
                    lessons = lessons,
                    progress = progress,
                    careState = careState,
                    dailyMissionSet = null,
                    growthStageState = growthStageState,
                    greenhouseCount = 1,
                    rewardState = RewardState(),
                    rewardFeedback = null,
                    realPlantModeState = RealPlantModeState(),
                    weatherSnapshot = weatherSnapshot,
                    weatherAdvice = weatherAdvice,
                    companionStateSnapshot = companionSnapshot,
                    companionHomeCheckIn = companionCheckIn,
                    onPerformCareAction = {},
                    onSubmitCompanionChatMessage = {},
                    onAcknowledgeGrowthStage = {},
                    onSetRealPlantModeEnabled = {},
                    onLogRealPlantCare = {},
                )
            }
        }

        val activity = composeTestRule.activity
        composeTestRule.onNodeWithText(activity.getString(R.string.new_evolution_unlocked, growthStageState.currentStage.title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.nice)).assertIsDisplayed()
    }
}
