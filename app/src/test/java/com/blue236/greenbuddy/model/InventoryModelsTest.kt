package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryModelsTest {
    @Test
    fun selectedStarterFallsBackToOwnedPlantWhenRequestedPlantIsLocked() {
        val state = GreenBuddyUiState(
            selectedStarterId = "tomato",
            ownedStarterIds = setOf("monstera"),
        )

        assertEquals("monstera", state.selectedStarter.id)
    }

    @Test
    fun inventoryEntriesExposePerPlantProgressAndCare() {
        val entries = buildInventoryEntries(
            ownedStarterIds = setOf("monstera", "basil"),
            selectedStarterId = "basil",
            lessonProgressByStarterId = mapOf("basil" to LessonProgress(totalXp = 45, completedLessonIds = setOf("basil_sun"))),
            careStateByStarterId = mapOf("basil" to PlantCareState(hydration = 80, sunlight = 90, nutrition = 70)),
        )

        val basilEntry = entries.first { it.option.id == "basil" }
        val tomatoEntry = entries.first { it.option.id == "tomato" }

        assertTrue(basilEntry.isOwned)
        assertTrue(basilEntry.isActive)
        assertEquals(45, basilEntry.progress.totalXp)
        assertEquals(80, basilEntry.careState.hydration)
        assertFalse(tomatoEntry.isOwned)
    }

    @Test
    fun nextUnlockableStarterIdReturnsFirstLockedPlant() {
        assertEquals("basil", nextUnlockableStarterId(setOf("monstera")))
        assertEquals("tomato", nextUnlockableStarterId(setOf("monstera", "basil")))
        assertEquals(null, nextUnlockableStarterId(StarterPlants.options.map { it.id }.toSet()))
    }
}
