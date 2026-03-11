package com.blue236.greenbuddy.model

private const val DEFAULT_LESSON_TOKEN_REWARD = 12
private const val DEFAULT_CARE_TOKEN_REWARD = 3

data class CosmeticItem(
    val id: String,
    val name: String,
    val emoji: String,
    val cost: Int,
    val description: String,
)

object RewardCatalog {
    val cosmetics: List<CosmeticItem> = listOf(
        CosmeticItem(
            id = "classic_pot",
            name = "Classic Clay Pot",
            emoji = "🪴",
            cost = 20,
            description = "A warm terracotta pot for a cozy windowsill vibe.",
        ),
        CosmeticItem(
            id = "sunny_ribbon",
            name = "Sunny Ribbon",
            emoji = "🎀",
            cost = 30,
            description = "A cheerful ribbon that makes your buddy look extra cared for.",
        ),
        CosmeticItem(
            id = "golden_glow",
            name = "Golden Glow",
            emoji = "✨",
            cost = 45,
            description = "A sparkly flourish for companions on a hot streak.",
        ),
    )

    fun cosmeticById(id: String?): CosmeticItem? = cosmetics.firstOrNull { it.id == id }
}

data class RewardState(
    val leafTokens: Int = 0,
    val unlockedCosmeticIds: Set<String> = emptySet(),
    val equippedCosmeticId: String? = null,
) {
    val equippedCosmetic: CosmeticItem?
        get() = RewardCatalog.cosmeticById(equippedCosmeticId)

    fun rewardForLesson(rewardXp: Int): RewardState = copy(
        leafTokens = leafTokens + lessonTokenReward(rewardXp),
    )

    fun rewardForCareAction(): RewardState = copy(
        leafTokens = leafTokens + DEFAULT_CARE_TOKEN_REWARD,
    )

    fun canPurchase(item: CosmeticItem): Boolean = item.id !in unlockedCosmeticIds && leafTokens >= item.cost

    fun purchase(item: CosmeticItem): RewardState {
        if (!canPurchase(item)) return this
        return copy(
            leafTokens = leafTokens - item.cost,
            unlockedCosmeticIds = unlockedCosmeticIds + item.id,
            equippedCosmeticId = equippedCosmeticId ?: item.id,
        )
    }

    fun equip(itemId: String): RewardState {
        if (itemId !in unlockedCosmeticIds) return this
        return copy(equippedCosmeticId = itemId)
    }

    companion object {
        fun lessonTokenReward(rewardXp: Int): Int = (rewardXp / 2).coerceAtLeast(DEFAULT_LESSON_TOKEN_REWARD)
        fun careTokenReward(): Int = DEFAULT_CARE_TOKEN_REWARD
    }
}
