package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantCareStateTest {
    @Test
    fun apply_waterBoostsHydrationAndCapsAtHundred() {
        val updated = PlantCareState(hydration = 92, sunlight = 60, nutrition = 55)
            .apply(CareAction.WATER)

        assertEquals(100, updated.hydration)
        assertEquals(57, updated.sunlight)
        assertEquals(55, updated.nutrition)
    }

    @Test
    fun derivedMoodAndHealthReflectWeakestCareNeed() {
        val thirsty = PlantCareState(hydration = 24, sunlight = 84, nutrition = 77)

        assertEquals("Thirsty", thirsty.mood)
        assertTrue(thirsty.health == "Stable" || thirsty.health == "Needs attention")
    }
}
