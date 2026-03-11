package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonProgressionTest {
    private val lessons = LessonCatalog.forSpecies("Monstera")

    @Test
    fun advanceWith_movesToNextLessonUntilTrackCompletes() {
        val firstStep = LessonProgress().advanceWith(
            completedLessonId = lessons.first().id,
            rewardXp = lessons.first().rewardXp,
            totalLessons = lessons.size,
        )

        assertEquals(1, firstStep.currentLessonIndex)
        assertEquals(setOf(lessons.first().id), firstStep.completedLessonIds)
        assertEquals(lessons.first().rewardXp, firstStep.totalXp)
        assertFalse(firstStep.isComplete(lessons))

        val completedTrack = lessons.drop(1).fold(firstStep) { progress, lesson ->
            progress.advanceWith(
                completedLessonId = lesson.id,
                rewardXp = lesson.rewardXp,
                totalLessons = lessons.size,
            )
        }

        assertEquals(lessons.size, completedTrack.currentLessonIndex)
        assertTrue(completedTrack.isComplete(lessons))
        assertNull(completedTrack.currentLessonOrNull(lessons))
        assertEquals(lessons.sumOf { it.rewardXp }, completedTrack.totalXp)
    }

    @Test
    fun advanceWith_doesNotDoubleCountCompletedLesson() {
        val progressed = LessonProgress().advanceWith(
            completedLessonId = lessons.first().id,
            rewardXp = lessons.first().rewardXp,
            totalLessons = lessons.size,
        )

        val duplicate = progressed.advanceWith(
            completedLessonId = lessons.first().id,
            rewardXp = lessons.first().rewardXp,
            totalLessons = lessons.size,
        )

        assertEquals(progressed, duplicate)
    }

    @Test
    fun normalizedFor_clampsCompletedTrackIndexToTerminalState() {
        val normalized = LessonProgress(currentLessonIndex = 99, completedLessonIds = lessons.map { it.id }.toSet())
            .normalizedFor(lessons)

        assertEquals(lessons.size, normalized.currentLessonIndex)
        assertTrue(normalized.isComplete(lessons))
    }
}
