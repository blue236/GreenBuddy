package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.AppLanguage
import com.blue236.greenbuddy.model.CompanionConversationMemory
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RealPlantModeState
import com.blue236.greenbuddy.model.RewardState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiscActionCoordinatorTest {
    private val coordinator = MiscActionCoordinator(FakeRewardEngine())

    @Test
    fun companionMessage_buildsPersistenceAndAnalyticsPayload() {
        val memory = CompanionConversationMemory()

        val outcome = coordinator.companionMessage("monstera", memory)

        assertEquals("companion_message_sent", outcome.analyticsEvent.name)
        assertEquals("monstera", outcome.starterId)
        assertEquals(memory, outcome.updatedMemory)
    }

    @Test
    fun realPlantModeToggle_updatesEnabledFlag() {
        val outcome = coordinator.realPlantModeToggle("monstera", RealPlantModeState(enabled = false), true)

        assertEquals("real_plant_mode_toggled", outcome.analyticsEvent.name)
        assertTrue(outcome.updatedState.enabled)
    }

    @Test
    fun cosmeticPurchase_buildsRewardFeedbackAndAnalytics() {
        val item = CosmeticItem("pot", "Pot", "🪴", 20, "desc")
        val rewardState = RewardState(leafTokens = 10)

        val outcome = coordinator.cosmeticPurchase(item, "en", rewardState)

        assertEquals("cosmetic_purchased", outcome.analyticsEvent.name)
        assertEquals("feedback:pot:en", outcome.rewardFeedback)
        assertEquals(rewardState, outcome.updatedRewardState)
    }

    @Test
    fun cosmeticEquip_hasNoAnalyticsWhenStateDidNotChange() {
        val outcome = coordinator.cosmeticEquip(changed = false, updatedRewardState = RewardState())

        assertNull(outcome.analyticsEvent)
    }

    @Test
    fun appLanguage_buildsLanguageAnalytics() {
        val outcome = coordinator.appLanguage(AppLanguage.KOREAN)

        assertEquals("language_selected", outcome.analyticsEvent.name)
        assertEquals("KOREAN", outcome.analyticsEvent.params["language"])
    }

    private class FakeRewardEngine : RewardEngineContract {
        override fun lessonFeedback(rewardXp: Int, missionOutcome: MissionRewardOutcome): String = "unused"
        override fun careFeedback(action: com.blue236.greenbuddy.model.CareAction, languageTag: String, wasHelpful: Boolean, missionOutcome: MissionRewardOutcome): String = "unused"
        override fun greenhouseUnlockFeedback(baseFeedback: String, unlockedStarter: com.blue236.greenbuddy.model.StarterPlantOption?, languageTag: String): String = baseFeedback
        override fun cosmeticFeedback(item: CosmeticItem, languageTag: String): String = "feedback:${item.id}:$languageTag"
    }
}
