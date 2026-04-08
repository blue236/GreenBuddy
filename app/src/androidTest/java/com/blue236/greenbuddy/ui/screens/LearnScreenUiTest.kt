package com.blue236.greenbuddy.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.ui.theme.GreenBuddyTheme
import org.junit.Rule
import org.junit.Test

class LearnScreenUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun learnScreen_showsProgressHeroAndAnswerTiles() {
        val starter = StarterPlants.options.first()
        val lessons = LessonCatalog.forSpecies(starter.companion.species)
        val progress = LessonProgress(totalXp = 12)
        val careState = PlantCareState(hydration = 72, sunlight = 68, nutrition = 54)
        val lesson = progress.currentLessonOrNull(lessons)!!

        composeTestRule.setContent {
            GreenBuddyTheme {
                LearnScreen(
                    starter = starter,
                    lessons = lessons,
                    progress = progress,
                    careState = careState,
                    onSubmitAnswer = { false },
                )
            }
        }

        val activity = composeTestRule.activity
        composeTestRule.onNodeWithText(activity.getString(R.string.learn_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.completed_count, progress.completedCount, lessons.size)).assertIsDisplayed()
        composeTestRule.onNodeWithText(lesson.title).assertIsDisplayed()
        composeTestRule.onNodeWithText(lesson.quiz.prompt).assertIsDisplayed()
        composeTestRule.onNodeWithText(lesson.quiz.options.first()).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.check_answer)).assertIsDisplayed()
    }
}
