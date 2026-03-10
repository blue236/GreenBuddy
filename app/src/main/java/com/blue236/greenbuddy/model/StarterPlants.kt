package com.blue236.greenbuddy.model

object StarterPlants {
    val options = listOf(
        StarterPlantOption(
            id = "monstera",
            title = "Monstera",
            subtitle = "Easygoing indoor starter",
            previewEmoji = "🌿",
            companion = PlantCompanion(
                name = "Leafling",
                species = "Monstera",
                stage = "Sprout",
                hydration = 72,
                sunlight = 81,
                nutrition = 46,
                mood = "Curious",
                greeting = "Thanks for helping me learn about sunlight today!",
                careTip = "Rotate weekly for even leaf growth.",
                emoji = "🌿",
            ),
        ),
        StarterPlantOption(
            id = "basil",
            title = "Basil",
            subtitle = "Fast-growing kitchen buddy",
            previewEmoji = "🌱",
            companion = PlantCompanion(
                name = "Pesto",
                species = "Basil",
                stage = "Seedling",
                hydration = 65,
                sunlight = 88,
                nutrition = 58,
                mood = "Energetic",
                greeting = "A sunny windowsill and I’m ready to thrive.",
                careTip = "Pinch top leaves often to keep me bushy.",
                emoji = "🌱",
            ),
        ),
        StarterPlantOption(
            id = "tomato",
            title = "Tomato",
            subtitle = "Rewarding fruiting challenge",
            previewEmoji = "🍅",
            companion = PlantCompanion(
                name = "Sunny",
                species = "Tomato",
                stage = "Starter",
                hydration = 78,
                sunlight = 92,
                nutrition = 61,
                mood = "Ambitious",
                greeting = "Give me support early and I’ll return the favor later.",
                careTip = "More sun means stronger stems and sweeter fruit.",
                emoji = "🍅",
            ),
        ),
    )
}
