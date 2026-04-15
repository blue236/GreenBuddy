package com.blue236.greenbuddy.ui.state

import com.blue236.greenbuddy.domain.CompanionCoordinator
import com.blue236.greenbuddy.domain.GrowthEngine
import com.blue236.greenbuddy.domain.MissionEngine
import com.blue236.greenbuddy.model.AppPreferences
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.SeasonalWeatherProvider
import com.blue236.greenbuddy.model.Tab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UiStateAssemblerTest {
    private val assembler = UiStateAssembler(
        missionEngine = MissionEngine(),
        growthEngine = GrowthEngine(),
        companionCoordinator = CompanionCoordinator(),
        lessonsForStarter = { starterId, localeTag ->
            val species = when (starterId) {
                "monstera" -> "Monstera"
                "basil" -> "Basil"
                else -> "Tomato"
            }
            LessonCatalog.forSpecies(species, localeTag)
        },
        weatherProvider = SeasonalWeatherProvider,
    )

    @Test
    fun assemble_normalizesSelectedLessonProgressAgainstAvailableLessons() {
        val preferences = AppPreferences(
            selectedStarterId = "monstera",
            lessonProgressByStarterId = mapOf(
                "monstera" to LessonProgress(
                    currentLessonIndex = 99,
                    completedLessonIds = setOf("monstera_light"),
                    totalXp = 20,
                ),
            ),
        )

        val state = assembler.assemble(
            preferences = preferences,
            selectedTab = Tab.HOME,
            rewardFeedback = "Nice",
            feedbackEvent = null,
            localeTag = "en",
            today = LocalDate.of(2026, 4, 15),
        )

        assertEquals(4, state.lessons.size)
        assertEquals(1, state.lessonProgress.completedCount)
        assertEquals(state.lessons.lastIndex, state.lessonProgress.currentLessonIndex)
        assertEquals("Nice", state.rewardFeedback)
    }

    @Test
    fun assemble_buildsWeatherAndCompanionStateForSelectedStarter() {
        val preferences = AppPreferences(
            selectedStarterId = "basil",
            ownedStarterIds = setOf("monstera", "basil"),
        )

        val state = assembler.assemble(
            preferences = preferences,
            selectedTab = Tab.HOME,
            rewardFeedback = null,
            feedbackEvent = null,
            localeTag = "en",
            today = LocalDate.of(2026, 4, 15),
        )

        assertEquals("basil", state.selectedStarter.id)
        assertEquals("berlin", state.weatherSnapshot.city.id)
        assertTrue(state.weatherAdvice.summary.isNotBlank())
        assertTrue(state.companionHomeCheckIn.bubble.isNotBlank())
    }
}
