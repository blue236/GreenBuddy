package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RealPlantModeStateTest {
    private val zoneId: ZoneId = ZoneId.of("Europe/Berlin")

    @Test
    fun logAction_keepsNewestEntriesFirstAndCapsHistory() {
        var state = RealPlantModeState(enabled = true)

        repeat(RealPlantModeState.MAX_LOG_ENTRIES + 2) { index ->
            state = state.logAction(
                action = RealPlantCareAction.WATERED,
                loggedAtEpochMillis = LocalDate.of(2026, 3, 1)
                    .plusDays(index.toLong())
                    .atTime(12, 0)
                    .atZone(zoneId)
                    .toInstant()
                    .toEpochMilli(),
                zoneId = zoneId,
            )
        }

        assertEquals(RealPlantModeState.MAX_LOG_ENTRIES, state.entries.size)
        assertEquals(
            LocalDate.of(2026, 3, 1)
                .plusDays((RealPlantModeState.MAX_LOG_ENTRIES + 1).toLong())
                .atTime(12, 0)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli(),
            state.entries.first().loggedAtEpochMillis,
        )
        assertEquals(
            LocalDate.of(2026, 3, 1)
                .plusDays(2)
                .atTime(12, 0)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli(),
            state.entries.last().loggedAtEpochMillis,
        )
    }

    @Test
    fun logAction_ignoresRepeatOfSameActionOnSameDay() {
        val today = LocalDate.of(2026, 3, 11)
        val morning = today.atTime(9, 0).atZone(zoneId).toInstant().toEpochMilli()
        val evening = today.atTime(21, 0).atZone(zoneId).toInstant().toEpochMilli()

        val state = RealPlantModeState(enabled = true)
            .logAction(RealPlantCareAction.WATERED, morning, zoneId)
            .logAction(RealPlantCareAction.WATERED, evening, zoneId)

        assertEquals(1, state.entries.size)
        assertEquals(morning, state.entries.single().loggedAtEpochMillis)
    }

    @Test
    fun completedActionsOn_countsUniqueChecklistItemsPerDay() {
        val today = LocalDate.of(2026, 3, 11)
        val noon = today.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        val evening = today.atTime(18, 30).atZone(zoneId).toInstant().toEpochMilli()
        val yesterday = today.minusDays(1).atTime(17, 0).atZone(zoneId).toInstant().toEpochMilli()

        val state = RealPlantModeState(enabled = true)
            .logAction(RealPlantCareAction.WATERED, noon, zoneId)
            .logAction(RealPlantCareAction.WATERED, evening, zoneId)
            .logAction(RealPlantCareAction.CHECKED_LIGHT, noon, zoneId)
            .logAction(RealPlantCareAction.FERTILIZED, yesterday, zoneId)

        val completed = state.completedActionsOn(today, zoneId)

        assertEquals(2, completed.size)
        assertTrue(RealPlantCareAction.WATERED in completed)
        assertTrue(RealPlantCareAction.CHECKED_LIGHT in completed)
    }
}
