package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.AppLanguage
import com.blue236.greenbuddy.model.CompanionConversationMemory
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardState

sealed interface MiscActionOutcome {
    val analyticsEvent: AnalyticsEvent?
}

data class CompanionMessageOutcome(
    override val analyticsEvent: AnalyticsEvent,
    val starterId: String,
    val updatedMemory: CompanionConversationMemory,
) : MiscActionOutcome

data class RealPlantToggleOutcome(
    override val analyticsEvent: AnalyticsEvent,
    val starterId: String,
    val updatedState: RealPlantModeState,
) : MiscActionOutcome

data class RealPlantLogOutcome(
    override val analyticsEvent: AnalyticsEvent,
    val starterId: String,
    val realPlantModeState: RealPlantModeState,
    val plantCareState: PlantCareState,
) : MiscActionOutcome

data class CosmeticPurchaseOutcome(
    override val analyticsEvent: AnalyticsEvent,
    val rewardFeedback: String,
    val updatedRewardState: RewardState,
) : MiscActionOutcome

data class CosmeticEquipOutcome(
    override val analyticsEvent: AnalyticsEvent?,
    val updatedRewardState: RewardState,
) : MiscActionOutcome

data class GrowthAcknowledgeOutcome(
    override val analyticsEvent: AnalyticsEvent,
    val starterId: String,
    val seenGrowthStageRank: Int,
) : MiscActionOutcome

data class WeatherCityOutcome(
    override val analyticsEvent: AnalyticsEvent,
    val cityId: String,
) : MiscActionOutcome

data class AppLanguageOutcome(
    override val analyticsEvent: AnalyticsEvent,
    val appLanguage: AppLanguage,
) : MiscActionOutcome

class MiscActionCoordinator(
    private val rewardEngine: RewardEngineContract,
) {
    fun companionMessage(starterId: String, updatedMemory: CompanionConversationMemory): CompanionMessageOutcome =
        CompanionMessageOutcome(
            analyticsEvent = AnalyticsEvent("companion_message_sent", mapOf("starter_id" to starterId)),
            starterId = starterId,
            updatedMemory = updatedMemory,
        )

    fun realPlantModeToggle(starterId: String, currentState: RealPlantModeState, enabled: Boolean): RealPlantToggleOutcome =
        RealPlantToggleOutcome(
            analyticsEvent = AnalyticsEvent("real_plant_mode_toggled", mapOf("enabled" to enabled.toString())),
            starterId = starterId,
            updatedState = currentState.copy(enabled = enabled),
        )

    fun realPlantLog(starterId: String, actionName: String, realPlantModeState: RealPlantModeState, plantCareState: PlantCareState): RealPlantLogOutcome =
        RealPlantLogOutcome(
            analyticsEvent = AnalyticsEvent("real_plant_action_logged", mapOf("action" to actionName, "starter_id" to starterId)),
            starterId = starterId,
            realPlantModeState = realPlantModeState,
            plantCareState = plantCareState,
        )

    fun cosmeticPurchase(item: CosmeticItem, languageTag: String, updatedRewardState: RewardState): CosmeticPurchaseOutcome =
        CosmeticPurchaseOutcome(
            analyticsEvent = AnalyticsEvent("cosmetic_purchased", mapOf("item_id" to item.id)),
            rewardFeedback = rewardEngine.cosmeticFeedback(item, languageTag),
            updatedRewardState = updatedRewardState,
        )

    fun cosmeticEquip(changed: Boolean, updatedRewardState: RewardState): CosmeticEquipOutcome =
        CosmeticEquipOutcome(
            analyticsEvent = if (changed) AnalyticsEvent("cosmetic_equipped") else null,
            updatedRewardState = updatedRewardState,
        )

    fun growthAcknowledge(starterId: String, seenGrowthStageRank: Int): GrowthAcknowledgeOutcome =
        GrowthAcknowledgeOutcome(
            analyticsEvent = AnalyticsEvent("growth_acknowledged", mapOf("starter_id" to starterId)),
            starterId = starterId,
            seenGrowthStageRank = seenGrowthStageRank,
        )

    fun weatherCity(cityId: String): WeatherCityOutcome =
        WeatherCityOutcome(
            analyticsEvent = AnalyticsEvent("weather_city_selected", mapOf("city_id" to cityId)),
            cityId = cityId,
        )

    fun appLanguage(appLanguage: AppLanguage): AppLanguageOutcome =
        AppLanguageOutcome(
            analyticsEvent = AnalyticsEvent("language_selected", mapOf("language" to appLanguage.name)),
            appLanguage = appLanguage,
        )
}
