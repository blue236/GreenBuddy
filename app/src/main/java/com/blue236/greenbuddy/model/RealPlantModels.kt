package com.blue236.greenbuddy.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class RealPlantCareAction(
    val label: String,
    val description: String,
    val linkedCareAction: CareAction,
) {
    WATERED("I watered my real plant", "Log a real watering and give your buddy a hydration bump.", CareAction.WATER),
    CHECKED_LIGHT("I checked its light", "Confirm you moved or checked placement for better light.", CareAction.MOVE_TO_SUNLIGHT),
    FERTILIZED("I fed my real plant", "Log a feeding day and mirror it in your buddy's nutrition.", CareAction.FERTILIZE),
}

data class RealPlantLogEntry(
    val action: RealPlantCareAction,
    val loggedAtEpochMillis: Long,
) {
    fun loggedDate(zoneId: ZoneId): LocalDate = Instant.ofEpochMilli(loggedAtEpochMillis)
        .atZone(zoneId)
        .toLocalDate()
}

data class RealPlantModeState(
    val enabled: Boolean = false,
    val entries: List<RealPlantLogEntry> = emptyList(),
) {
    fun canLogActionOn(
        action: RealPlantCareAction,
        date: LocalDate,
        zoneId: ZoneId,
    ): Boolean = entries.none { entry ->
        entry.action == action && entry.loggedDate(zoneId) == date
    }

    fun logAction(
        action: RealPlantCareAction,
        loggedAtEpochMillis: Long,
        zoneId: ZoneId,
        maxEntries: Int = MAX_LOG_ENTRIES,
    ): RealPlantModeState {
        val loggedDate = Instant.ofEpochMilli(loggedAtEpochMillis).atZone(zoneId).toLocalDate()
        if (!canLogActionOn(action, loggedDate, zoneId)) return this

        return copy(
            entries = (listOf(RealPlantLogEntry(action, loggedAtEpochMillis)) + entries)
                .sortedByDescending { it.loggedAtEpochMillis }
                .take(maxEntries),
        )
    }

    fun completedActionsOn(date: LocalDate, zoneId: ZoneId): Set<RealPlantCareAction> = entries
        .filter { it.loggedDate(zoneId) == date }
        .mapTo(linkedSetOf()) { it.action }

    companion object {
        const val MAX_LOG_ENTRIES: Int = 12
    }
}
