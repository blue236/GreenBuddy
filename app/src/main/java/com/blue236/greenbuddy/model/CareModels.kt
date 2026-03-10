package com.blue236.greenbuddy.model

enum class CareAction(
    val label: String,
    val description: String,
) {
    WATER("Water", "Boost hydration with a good drink."),
    MOVE_TO_SUNLIGHT("Sun bath", "Shift your plant into brighter light."),
    FERTILIZE("Fertilize", "Feed nutrients for stronger growth."),
}

data class PlantCareState(
    val hydration: Int,
    val sunlight: Int,
    val nutrition: Int,
) {
    val averageScore: Int = (hydration + sunlight + nutrition) / 3

    val health: String
        get() = when {
            averageScore >= 85 -> "Thriving"
            averageScore >= 65 -> "Healthy"
            averageScore >= 45 -> "Stable"
            else -> "Needs attention"
        }

    val mood: String
        get() = when {
            hydration < 35 -> "Thirsty"
            sunlight < 35 -> "Shady"
            nutrition < 35 -> "Hungry"
            averageScore >= 85 -> "Joyful"
            averageScore >= 65 -> "Content"
            else -> "Sleepy"
        }

    fun apply(action: CareAction): PlantCareState = when (action) {
        CareAction.WATER -> copy(
            hydration = (hydration + 18).coerceAtMost(100),
            sunlight = (sunlight - 3).coerceAtLeast(0),
        )
        CareAction.MOVE_TO_SUNLIGHT -> copy(
            sunlight = (sunlight + 18).coerceAtMost(100),
            hydration = (hydration - 5).coerceAtLeast(0),
        )
        CareAction.FERTILIZE -> copy(
            nutrition = (nutrition + 16).coerceAtMost(100),
            hydration = (hydration - 4).coerceAtLeast(0),
        )
    }

    companion object {
        fun from(companion: PlantCompanion) = PlantCareState(
            hydration = companion.hydration,
            sunlight = companion.sunlight,
            nutrition = companion.nutrition,
        )
    }
}
