package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.DailyPlant
import java.time.LocalDate

object DailyPlantEngine {
    fun plantFor(plants: List<DailyPlant>, date: LocalDate = LocalDate.now()): DailyPlant? {
        if (plants.isEmpty()) return null
        val index = (date.toEpochDay() % plants.size).toInt()
        return plants[index]
    }
}
