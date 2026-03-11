package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
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
}
