package com.blue236.greenbuddy.model

data class DailyPlant(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val emoji: String,
    val sunlight: String,
    val watering: String,
    val nativeRegion: String,
    val funFact: String,
)
