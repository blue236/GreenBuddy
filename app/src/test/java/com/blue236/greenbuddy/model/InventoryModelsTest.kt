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
            lessonProgressByStarterId = mapOf(
                "monstera" to LessonProgress(totalXp = 20, completedLessonIds = setOf("monstera_light")),
                "basil" to LessonProgress(totalXp = 45, completedLessonIds = setOf("basil_sun")),
            ),
            careStateByStarterId = mapOf(
                "monstera" to PlantCareState(hydration = 61, sunlight = 74, nutrition = 58),
                "basil" to PlantCareState(hydration = 80, sunlight = 90, nutrition = 70),
            ),
        )

        val monsteraEntry = entries.first { it.option.id == "monstera" }
        val basilEntry = entries.first { it.option.id == "basil" }
        val tomatoEntry = entries.first { it.option.id == "tomato" }

        assertTrue(monsteraEntry.isOwned)
        assertFalse(monsteraEntry.isActive)
        assertEquals(20, monsteraEntry.progress.totalXp)
        assertEquals(61, monsteraEntry.careState.hydration)

        assertTrue(basilEntry.isOwned)
        assertTrue(basilEntry.isActive)
        assertEquals(45, basilEntry.progress.totalXp)
        assertEquals(80, basilEntry.careState.hydration)

        assertFalse(tomatoEntry.isOwned)
    }

    @Test
    fun inventoryEntriesUseResolvedOwnedFallbackForActiveState() {
        val state = GreenBuddyUiState(
            selectedStarterId = "tomato",
            ownedStarterIds = setOf("monstera", "basil"),
        )

        val activeEntries = state.inventoryEntries.filter { it.isActive }

        assertEquals(1, activeEntries.size)
        assertEquals("monstera", activeEntries.single().option.id)
        assertFalse(state.inventoryEntries.first { it.option.id == "tomato" }.isActive)
    }

    @Test
    fun nextUnlockableStarterIdReturnsFirstLockedPlant() {
        assertEquals("basil", nextUnlockableStarterId(setOf("monstera")))
        assertEquals("tomato", nextUnlockableStarterId(setOf("monstera", "basil")))
        assertEquals(null, nextUnlockableStarterId(StarterPlants.options.map { it.id }.toSet()))
    }

    @Test
    fun activeInventoryEntryFallsBackToFirstOwnedPlantWhenNoEntryIsMarkedActive() {
        val entries = buildInventoryEntries(
            ownedStarterIds = setOf("monstera", "basil"),
            selectedStarterId = "tomato",
            lessonProgressByStarterId = emptyMap(),
            careStateByStarterId = emptyMap(),
        ).map { entry ->
            if (entry.option.id == "monstera") entry.copy(isActive = false) else entry
        }

        assertEquals("monstera", activeInventoryEntry(entries)?.option?.id)
    }

    @Test
    fun unlockRequirementReflectsAutomaticUnlockFlowWithoutPlantCountAssumptions() {
        assertEquals(
            "Automatically unlocks when you complete any current plant track.",
            unlockRequirementFor(
                option = StarterPlants.options.first { it.id == "basil" },
                ownedStarterIds = setOf("monstera"),
                languageTag = "en",
            ),
        )
        assertEquals(
            "Unlock earlier greenhouse companions first.",
            unlockRequirementFor(
                option = StarterPlants.options.first { it.id == "tomato" },
                ownedStarterIds = setOf("monstera"),
                languageTag = "en",
            ),
        )
    }

    @Test
    fun unlockRequirementSupportsLocalizedCopy() {
        assertEquals(
            "현재 식물 트랙 하나를 완료하면 자동으로 잠금 해제돼요.",
            unlockRequirementFor(
                option = StarterPlants.options.first { it.id == "basil" },
                ownedStarterIds = setOf("monstera"),
                languageTag = "ko",
            ),
        )
        assertEquals(
            "Schalte zuerst frühere Gewächshaus-Begleiter frei.",
            unlockRequirementFor(
                option = StarterPlants.options.first { it.id == "tomato" },
                ownedStarterIds = setOf("monstera"),
                languageTag = "de",
            ),
        )
    }
}
