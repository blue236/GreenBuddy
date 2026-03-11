package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardStateTest {
    private val firstItem = RewardCatalog.cosmetics.first()

    @Test
    fun purchase_unlocksAndAutoEquipsFirstCosmetic() {
        val updated = RewardState(leafTokens = 50).purchase(firstItem)

        assertEquals(30, updated.leafTokens)
        assertTrue(firstItem.id in updated.unlockedCosmeticIds)
        assertEquals(firstItem.id, updated.equippedCosmeticId)
    }

    @Test
    fun purchase_ignoresUnaffordableItems() {
        val updated = RewardState(leafTokens = firstItem.cost - 1).purchase(firstItem)

        assertEquals(firstItem.cost - 1, updated.leafTokens)
        assertTrue(updated.unlockedCosmeticIds.isEmpty())
        assertNull(updated.equippedCosmeticId)
    }

    @Test
    fun equip_onlyWorksForUnlockedItems() {
        val lockedEquip = RewardState().equip(firstItem.id)
        val unlockedEquip = RewardState(unlockedCosmeticIds = setOf(firstItem.id)).equip(firstItem.id)

        assertNull(lockedEquip.equippedCosmeticId)
        assertEquals(firstItem.id, unlockedEquip.equippedCosmeticId)
    }

    @Test
    fun rewardForCareAction_onlyAddsTokensWhenHelpful() {
        val starting = RewardState(leafTokens = 10)

        val rewarded = starting.rewardForCareAction(isHelpful = true)
        val unchanged = starting.rewardForCareAction(isHelpful = false)

        assertEquals(13, rewarded.leafTokens)
        assertEquals(10, unchanged.leafTokens)
    }

    @Test
    fun careActionAtCap_isNotMeaningfullyImproved() {
        val before = PlantCareState(hydration = 100, sunlight = 100, nutrition = 100)
        val after = before.apply(CareAction.WATER)

        assertFalse(after.isMeaningfullyImprovedFrom(before))
    }

    @Test
    fun careActionThatDoesNotNetHelp_isNotMeaningfullyImproved() {
        val before = PlantCareState(hydration = 100, sunlight = 20, nutrition = 60)
        val after = before.apply(CareAction.WATER)

        assertFalse(after.isMeaningfullyImprovedFrom(before))
    }

    @Test
    fun careActionThatRaisesOverallCare_isMeaningfullyImproved() {
        val before = PlantCareState(hydration = 40, sunlight = 70, nutrition = 60)
        val after = before.apply(CareAction.WATER)

        assertTrue(after.isMeaningfullyImprovedFrom(before))
    }
}
