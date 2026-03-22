package com.blue236.greenbuddy.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.resolveGrowthStageState
import com.blue236.greenbuddy.ui.theme.GreenBuddyTheme
import org.junit.Rule
import org.junit.Test

class ProfileScreenGrowthTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun growthStatus_showsProgressAndCelebrateCtaForNewUnlock() {
        val starter = StarterPlants.options.first { it.id == "basil" }
        val progress = com.blue236.greenbuddy.model.LessonProgress(totalXp = 20)
        val careState = PlantCareState(hydration = 58, sunlight = 58, nutrition = 58)
        val growthStageState = resolveGrowthStageState(starter.id, progress, careState, seenStageRank = 0)

        composeTestRule.setContent {
            GreenBuddyTheme {
                ProfileScreen(
                    starter = starter,
                    progress = progress,
                    dailyMissionSet = null,
                    growthStageState = growthStageState,
                    ownedPlantCount = 1,
                    rewardState = RewardState(),
                    realPlantModeState = RealPlantModeState(),
                    onOpenSettings = {},
                    onAcknowledgeGrowthStage = {},
                    onPurchaseCosmetic = {},
                    onEquipCosmetic = {},
                )
            }
        }

        val activity = composeTestRule.activity
        composeTestRule.onNodeWithText(activity.getString(R.string.growth_status)).assertIsDisplayed()
        composeTestRule.onNodeWithText("${growthStageState.currentStage.emoji} ${growthStageState.currentStage.title}").assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.growth_readiness_chip, growthStageState.readinessPercent)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.growth_next_stage_chip, growthStageState.nextStage?.title ?: "")).assertIsDisplayed()
        composeTestRule.onNodeWithText(growthStageState.currentStage.unlockedMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.celebrate_growth)).assertIsDisplayed()
    }
}
