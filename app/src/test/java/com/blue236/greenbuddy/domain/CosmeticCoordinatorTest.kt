package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.RewardCatalog
import com.blue236.greenbuddy.model.RewardState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CosmeticCoordinatorTest {
    private val coordinator = CosmeticCoordinator()
    private val firstItem = RewardCatalog.cosmetics.first()

    @Test
    fun purchase_acceptsAffordableLockedItem() {
        val result = coordinator.purchase(firstItem, RewardState(leafTokens = 50))

        assertTrue(result.accepted)
        assertEquals(30, result.updatedRewardState.leafTokens)
        assertTrue(firstItem.id in result.updatedRewardState.unlockedCosmeticIds)
    }

    @Test
    fun purchase_rejectsUnaffordableItemWithoutChangingState() {
        val starting = RewardState(leafTokens = firstItem.cost - 1)

        val result = coordinator.purchase(firstItem, starting)

        assertFalse(result.accepted)
        assertEquals(starting, result.updatedRewardState)
    }

    @Test
    fun equip_routesThroughRewardStateRules() {
        val locked = coordinator.equip(firstItem.id, RewardState())
        val unlocked = coordinator.equip(firstItem.id, RewardState(unlockedCosmeticIds = setOf(firstItem.id)))

        assertEquals(null, locked.equippedCosmeticId)
        assertEquals(firstItem.id, unlocked.equippedCosmeticId)
    }
}
