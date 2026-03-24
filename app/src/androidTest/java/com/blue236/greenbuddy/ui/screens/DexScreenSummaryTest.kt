package com.blue236.greenbuddy.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.buildInventoryEntries
import com.blue236.greenbuddy.ui.theme.GreenBuddyTheme
import org.junit.Rule
import org.junit.Test

class DexScreenSummaryTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun greenhouseSummaryCard_showsActiveNextUnlockAndMomentum() {
        val entries = buildInventoryEntries(
            ownedStarterIds = setOf("monstera"),
            selectedStarterId = "monstera",
            lessonProgressByStarterId = mapOf("monstera" to LessonProgress(totalXp = 12)),
            careStateByStarterId = mapOf("monstera" to PlantCareState(hydration = 70, sunlight = 76, nutrition = 72)),
        )

        composeTestRule.setContent {
            GreenBuddyTheme {
                DexScreen(entries = entries, ownedStarterIds = setOf("monstera"), onSelectStarter = {})
            }
        }

        val activity = composeTestRule.activity
        val activeStarter = StarterPlants.options.first { it.id == "monstera" }
        val nextUnlock = StarterPlants.options.first { it.id == "basil" }

        composeTestRule.onNodeWithText(activity.getString(R.string.greenhouse_summary_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.greenhouse_summary_momentum_value, 1, 3)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.greenhouse_summary_next_unlock_value, nextUnlock.previewEmoji, nextUnlock.title)).assertIsDisplayed()
        composeTestRule.onNodeWithText("${activeStarter.previewEmoji} ${activeStarter.title}", substring = true).assertIsDisplayed()
    }

    @Test
    fun greenhouseCards_showActiveOwnedAndLockedLabels() {
        val entries = buildInventoryEntries(
            ownedStarterIds = setOf("monstera", "basil"),
            selectedStarterId = "monstera",
            lessonProgressByStarterId = mapOf(
                "monstera" to LessonProgress(totalXp = 12),
                "basil" to LessonProgress(totalXp = 4),
            ),
            careStateByStarterId = mapOf(
                "monstera" to PlantCareState(hydration = 70, sunlight = 76, nutrition = 72),
                "basil" to PlantCareState(hydration = 66, sunlight = 61, nutrition = 68),
            ),
        )

        composeTestRule.setContent {
            GreenBuddyTheme {
                DexScreen(entries = entries, ownedStarterIds = setOf("monstera", "basil"), onSelectStarter = {})
            }
        }

        val activity = composeTestRule.activity
        composeTestRule.onAllNodesWithText(activity.getString(R.string.active)).assertCountEquals(1)
        composeTestRule.onAllNodesWithText(activity.getString(R.string.switch_action)).assertCountEquals(1)
        composeTestRule.onAllNodesWithText(activity.getString(R.string.locked)).assertCountEquals(1)
    }
}
