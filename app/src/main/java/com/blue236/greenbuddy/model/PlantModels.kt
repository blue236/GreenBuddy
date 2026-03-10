package com.blue236.greenbuddy.model

data class PlantCompanion(
    val name: String,
    val species: String,
    val stage: String,
    val hydration: Int,
    val sunlight: Int,
    val nutrition: Int,
    val mood: String,
    val greeting: String,
    val careTip: String,
    val emoji: String,
)

data class StarterPlantOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val previewEmoji: String,
    val companion: PlantCompanion,
)
