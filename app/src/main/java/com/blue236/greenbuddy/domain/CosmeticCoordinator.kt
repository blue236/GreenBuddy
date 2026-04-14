package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.RewardState

data class CosmeticPurchaseResult(
    val accepted: Boolean,
    val updatedRewardState: RewardState,
)

class CosmeticCoordinator {
    fun purchase(item: CosmeticItem, currentRewardState: RewardState): CosmeticPurchaseResult {
        if (!currentRewardState.canPurchase(item)) {
            return CosmeticPurchaseResult(
                accepted = false,
                updatedRewardState = currentRewardState,
            )
        }
        return CosmeticPurchaseResult(
            accepted = true,
            updatedRewardState = currentRewardState.purchase(item),
        )
    }

    fun equip(itemId: String, currentRewardState: RewardState): RewardState =
        currentRewardState.equip(itemId)
}
