package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.CompanionChatEngine
import com.blue236.greenbuddy.model.CompanionConversationMemory
import com.blue236.greenbuddy.model.CompanionHomeCheckIn
import com.blue236.greenbuddy.model.CompanionStateSnapshot
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.GrowthStageState
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.ReminderNotification
import com.blue236.greenbuddy.model.ReminderType
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.WeatherAdvice
import com.blue236.greenbuddy.model.WeatherSnapshot

data class CompanionMessageResult(
    val reply: com.blue236.greenbuddy.model.CompanionChatReply,
    val updatedMemory: CompanionConversationMemory,
)

class CompanionCoordinator {
    fun snapshot(
        starter: StarterPlantOption,
        careState: PlantCareState,
        growthStageState: GrowthStageState,
        dailyMissionSet: DailyMissionSet?,
        weatherSnapshot: WeatherSnapshot,
        weatherAdvice: WeatherAdvice,
        realPlantModeState: RealPlantModeState,
        recentConversationMemory: CompanionConversationMemory,
        languageTag: String,
    ): CompanionStateSnapshot = CompanionChatEngine.createSnapshot(
        starter = starter,
        careState = careState,
        growthStageState = growthStageState,
        dailyMissionSet = dailyMissionSet,
        weatherSnapshot = weatherSnapshot,
        weatherAdvice = weatherAdvice,
        realPlantModeState = realPlantModeState,
        recentConversationMemory = recentConversationMemory,
        languageTag = languageTag,
    )

    fun homeCheckIn(snapshot: CompanionStateSnapshot, languageTag: String): CompanionHomeCheckIn =
        CompanionChatEngine.proactiveCheckIn(snapshot, languageTag)

    fun handleMessage(message: String, snapshot: CompanionStateSnapshot, languageTag: String): CompanionMessageResult {
        val reply = CompanionChatEngine.replyTo(message = message, snapshot = snapshot, languageTag = languageTag)
        return CompanionMessageResult(
            reply = reply,
            updatedMemory = CompanionChatEngine.updatedMemoryFor(reply, snapshot),
        )
    }

    fun reminderCopy(
        type: ReminderType,
        starter: StarterPlantOption,
        careState: PlantCareState,
        lessonTitle: String?,
        languageTag: String,
    ): ReminderNotification = com.blue236.greenbuddy.model.CompanionPersonalitySystem.reminderCopy(type, starter, careState, lessonTitle, languageTag)
}
