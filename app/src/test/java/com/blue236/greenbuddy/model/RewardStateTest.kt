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
    fun purchase_doesNotChargeAgainForAlreadyUnlockedItem() {
        val starting = RewardState(
            leafTokens = 50,
            unlockedCosmeticIds = setOf(firstItem.id),
            equippedCosmeticId = firstItem.id,
        )

        val updated = starting.purchase(firstItem)

        assertEquals(starting, updated)
    }

    @Test
    fun purchase_preservesExistingEquippedCosmeticWhenBuyingAnother() {
        val secondItem = RewardCatalog.cosmetics[1]
        val starting = RewardState(
            leafTokens = 80,
            unlockedCosmeticIds = setOf(firstItem.id),
            equippedCosmeticId = firstItem.id,
        )

        val updated = starting.purchase(secondItem)

        assertEquals(80 - secondItem.cost, updated.leafTokens)
        assertEquals(setOf(firstItem.id, secondItem.id), updated.unlockedCosmeticIds)
        assertEquals(firstItem.id, updated.equippedCosmeticId)
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
    fun nextUnlockableCosmetic_picksCheapestLockedItem() {
        val state = RewardState(unlockedCosmeticIds = setOf(firstItem.id))

        assertEquals("sunny_ribbon", state.nextUnlockableCosmetic?.id)
    }

    @Test
    fun tokensNeededFor_neverGoesBelowZero() {
        val state = RewardState(leafTokens = 50)

        assertEquals(0, state.tokensNeededFor(firstItem))
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
    fun nextUnlockableCosmetic_picksLowestCostLockedItemAndTracksRemainingTokens() {
        val state = RewardState(
            leafTokens = 18,
            unlockedCosmeticIds = setOf(firstItem.id),
        )
        val nextItem = RewardCatalog.cosmetics[1]

        assertEquals(nextItem, state.nextUnlockableCosmetic)
        assertEquals(12, state.tokensNeededFor(nextItem))
    }

    @Test
    fun nextUnlockableCosmetic_isNullWhenEverythingIsUnlocked() {
        val state = RewardState(
            leafTokens = 999,
            unlockedCosmeticIds = RewardCatalog.cosmetics.map { it.id }.toSet(),
            equippedCosmeticId = RewardCatalog.cosmetics.last().id,
        )

        assertNull(state.nextUnlockableCosmetic)
    }

    @Test
    fun careActionThatRaisesOverallCare_isMeaningfullyImproved() {
        val before = PlantCareState(hydration = 40, sunlight = 70, nutrition = 60)
        val after = before.apply(CareAction.WATER)

        assertTrue(after.isMeaningfullyImprovedFrom(before))
    }
}
