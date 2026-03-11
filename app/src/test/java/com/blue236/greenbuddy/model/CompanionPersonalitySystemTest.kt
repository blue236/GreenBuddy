package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionPersonalitySystemTest {
    @Test
    fun personalityFor_givesDistinctArchetypesAcrossStarters() {
        val monstera = CompanionPersonalitySystem.personalityFor("Monstera")
        val basil = CompanionPersonalitySystem.personalityFor("Basil")
        val tomato = CompanionPersonalitySystem.personalityFor("Tomato")

        assertNotEquals(monstera.archetype, basil.archetype)
        assertNotEquals(basil.archetype, tomato.archetype)
        assertNotEquals(monstera.homeHeadline, tomato.homeHeadline)
    }

    @Test
    fun dialogueFor_changesToneBasedOnCareNeeds() {
        val starter = StarterPlants.options.first { it.id == "basil" }
        val lessons = LessonCatalog.forSpecies(starter.companion.species)

        val needyDialogue = CompanionPersonalitySystem.dialogueFor(
            starter = starter,
            careState = PlantCareState(hydration = 20, sunlight = 80, nutrition = 75),
            progress = LessonProgress(),
            lessons = lessons,
        )
        val growingDialogue = CompanionPersonalitySystem.dialogueFor(
            starter = starter,
            careState = PlantCareState(hydration = 82, sunlight = 88, nutrition = 80),
            progress = LessonProgress(),
            lessons = lessons,
        )

        assertTrue(needyDialogue.line.contains("quick boost", ignoreCase = true))
        assertTrue(growingDialogue.line.contains("perky", ignoreCase = true))
        assertNotEquals(needyDialogue.careGuidance, growingDialogue.careGuidance)
    }

    @Test
    fun dialogueFor_usesProudToneWhenTrackIsComplete() {
        val starter = StarterPlants.options.first { it.id == "tomato" }
        val lessons = LessonCatalog.forSpecies(starter.companion.species)
        val progress = LessonProgress(
            currentLessonIndex = lessons.lastIndex,
            completedLessonIds = lessons.map { it.id }.toSet(),
            totalXp = lessons.sumOf { it.rewardXp },
        )

        val dialogue = CompanionPersonalitySystem.dialogueFor(
            starter = starter,
            careState = PlantCareState(hydration = 82, sunlight = 90, nutrition = 84),
            progress = progress,
            lessons = lessons,
        )

        assertTrue(dialogue.line.contains("excellent work", ignoreCase = true))
        assertTrue(dialogue.lessonNudge.contains("complete", ignoreCase = true))
    }

    @Test
    fun reminderCopy_reflectsSpeciesVoice() {
        val monsteraReminder = CompanionPersonalitySystem.reminderCopy(
            type = ReminderType.LESSON_READY,
            starter = StarterPlants.options.first { it.id == "monstera" },
            careState = PlantCareState(hydration = 70, sunlight = 70, nutrition = 70),
            lessonTitle = "Indirect light basics",
        )
        val basilReminder = CompanionPersonalitySystem.reminderCopy(
            type = ReminderType.LESSON_READY,
            starter = StarterPlants.options.first { it.id == "basil" },
            careState = PlantCareState(hydration = 70, sunlight = 70, nutrition = 70),
            lessonTitle = "Sun + water balance",
        )

        assertEquals("A calm GreenBuddy lesson is ready", monsteraReminder.title)
        assertTrue(basilReminder.message.contains("keeping the pace up", ignoreCase = true))
    }
}
