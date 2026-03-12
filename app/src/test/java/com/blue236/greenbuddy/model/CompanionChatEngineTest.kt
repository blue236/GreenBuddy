package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CompanionChatEngineTest {
    private val starter = StarterPlants.options.first { it.id == "basil" }
    private val careState = PlantCareState(hydration = 30, sunlight = 84, nutrition = 55)
    private val growthState = resolveGrowthStageState(
        starterId = starter.id,
        progress = LessonProgress(totalXp = 18),
        careState = careState,
    )
    private val missionSet = DailyMissionProgress(completedCareActionsToday = 1)
        .normalizedFor(LocalDate.now())
        .resolveForToday(LocalDate.now(), LessonProgress(), careState)
    private val weatherSnapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 1, 10))
    private val weatherAdvice = WeatherAdviceGenerator.adviceFor(starter, weatherSnapshot)

    @Test
    fun detectIntent_mapsKnownCategories() {
        assertEquals(CompanionChatIntent.STATUS_CHECK, CompanionChatEngine.detectIntent("How are you feeling?"))
        assertEquals(CompanionChatIntent.CARE_ADVICE, CompanionChatEngine.detectIntent("Do you need water?"))
        assertEquals(CompanionChatIntent.MISSION_HELP, CompanionChatEngine.detectIntent("What mission should I do today?"))
        assertEquals(CompanionChatIntent.GROWTH_QUESTION, CompanionChatEngine.detectIntent("How do you grow to the next stage?"))
        assertEquals(CompanionChatIntent.WEATHER_QUESTION, CompanionChatEngine.detectIntent("How is the weather for you?"))
        assertEquals(CompanionChatIntent.CASUAL_CHAT, CompanionChatEngine.detectIntent("You seem fun today"))
    }

    @Test
    fun createSnapshot_capturesCurrentState() {
        val realPlantMode = RealPlantModeState(enabled = true).logAction(
            action = RealPlantCareAction.WATERED,
            loggedAtEpochMillis = System.currentTimeMillis(),
            zoneId = ZoneId.systemDefault(),
        )

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = realPlantMode,
        )

        assertEquals("Basil", snapshot.starter.companion.species)
        assertEquals("Thirsty", snapshot.mood)
        assertEquals("Stable", snapshot.health)
        assertTrue(snapshot.realPlantSummary.contains("Real-plant mode is on"))
    }

    @Test
    fun replyTo_usesStateAwareCareAdvice() {
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(enabled = false),
        )

        val reply = CompanionChatEngine.replyTo("Should I water you?", snapshot)

        assertEquals(CompanionChatIntent.CARE_ADVICE, reply.intent)
        assertTrue(reply.reply.contains("hydration is my lowest stat at 30"))
        assertTrue(reply.reply.contains(weatherAdvice.starterAdvice))
    }

    @Test
    fun replyTo_reportsGrowthProgress() {
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        val reply = CompanionChatEngine.replyTo("How are you growing?", snapshot)

        assertEquals(CompanionChatIntent.GROWTH_QUESTION, reply.intent)
        assertTrue(reply.reply.contains(growthState.currentStage.title))
        assertTrue(reply.reply.contains("% of the way there"))
    }
}
