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
                loggedAtEpochMillis = (index + 1).toLong(),
            )
        }

        assertEquals(RealPlantModeState.MAX_LOG_ENTRIES, state.entries.size)
        assertEquals((RealPlantModeState.MAX_LOG_ENTRIES + 2).toLong(), state.entries.first().loggedAtEpochMillis)
        assertEquals(3L, state.entries.last().loggedAtEpochMillis)
    }

    @Test
    fun completedActionsOn_countsUniqueChecklistItemsPerDay() {
        val today = LocalDate.of(2026, 3, 11)
        val noon = today.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        val evening = today.atTime(18, 30).atZone(zoneId).toInstant().toEpochMilli()
        val yesterday = today.minusDays(1).atTime(17, 0).atZone(zoneId).toInstant().toEpochMilli()

        val state = RealPlantModeState(enabled = true)
            .logAction(RealPlantCareAction.WATERED, noon)
            .logAction(RealPlantCareAction.WATERED, evening)
            .logAction(RealPlantCareAction.CHECKED_LIGHT, noon)
            .logAction(RealPlantCareAction.FERTILIZED, yesterday)

        val completed = state.completedActionsOn(today, zoneId)

        assertEquals(2, completed.size)
        assertTrue(RealPlantCareAction.WATERED in completed)
        assertTrue(RealPlantCareAction.CHECKED_LIGHT in completed)
    }
}
