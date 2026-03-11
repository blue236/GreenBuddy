package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonCatalogTest {
    @Test
    fun starterTracks_areExpandedAndDistinct() {
        val species = listOf("Monstera", "Basil", "Tomato")

        species.forEach { name ->
            val lessons = LessonCatalog.forSpecies(name)
            assertEquals("Expected 4 lessons for $name", 4, lessons.size)
            assertEquals(lessons.size, lessons.map { it.id }.distinct().size)
            assertTrue(lessons.all { it.summary.isNotBlank() })
            assertTrue(lessons.all { it.rewardLabel.isNotBlank() })
        }
    }

    @Test
    fun starterTracks_coverAllQuizTypes() {
        val lessons = listOf("Monstera", "Basil", "Tomato")
            .flatMap { LessonCatalog.forSpecies(it) }

        val quizTypes = lessons.map { it.quiz.type }.toSet()
        assertEquals(setOf(QuizType.MULTIPLE_CHOICE, QuizType.TRUE_FALSE, QuizType.SCENARIO_CHOICE), quizTypes)
    }
}
