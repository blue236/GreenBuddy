package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.RealPlantModeState
import java.time.Instant
import java.time.ZoneId

data class RealPlantLogResult(
    val accepted: Boolean,
    val realPlantModeState: RealPlantModeState,
    val plantCareState: PlantCareState,
)

class RealPlantCoordinator {
    fun logAction(
        action: RealPlantCareAction,
        currentRealPlantModeState: RealPlantModeState,
        currentPlantCareState: PlantCareState,
        nowMillis: Long,
        zoneId: ZoneId,
    ): RealPlantLogResult {
        val date = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        if (!currentRealPlantModeState.canLogActionOn(action, date, zoneId)) {
            return RealPlantLogResult(false, currentRealPlantModeState, currentPlantCareState)
        }
        return RealPlantLogResult(
            accepted = true,
            realPlantModeState = currentRealPlantModeState.logAction(action, nowMillis, zoneId),
            plantCareState = currentPlantCareState.apply(action.linkedCareAction),
        )
    }
}
