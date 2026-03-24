package com.blue236.greenbuddy.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CompanionChatEngine
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
import org.junit.Rule
import org.junit.Test

class HomeScreenCompanionUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun companionCards_showProactiveTitleContinuitySummaryAndOpenChatCta() {
        val starter = StarterPlants.options.first()
        val lessons = LessonCatalog.forSpecies(starter.companion.species)
        val progress = LessonProgress(totalXp = 12)
        val careState = PlantCareState(hydration = 72, sunlight = 68, nutrition = 54)
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
        composeTestRule.onNodeWithText(activity.getString(R.string.companion_proactive_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.companion_chat_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.companion_chat_open)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.companion_continuity_summary, companionCheckIn.emotionLabel, companionCheckIn.familiarityLabel)).assertIsDisplayed()
    }
}
