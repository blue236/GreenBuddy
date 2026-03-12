package com.blue236.greenbuddy.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherAdviceGeneratorTest {
    @Test
    fun berlinWinterResolvesToColdDimSnapshot() {
        val snapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 1, 15))

        assertEquals(WeatherSeason.WINTER, snapshot.season)
        assertEquals(WeatherCondition.COLD_DIM, snapshot.condition)
    }

    @Test
    fun basilAdviceCallsOutSunnyWindowInColdDimConditions() {
        val basil = StarterPlants.options.first { it.id == "basil" }
        val snapshot = WeatherSnapshot(
            city = WeatherCatalog.cityById("berlin"),
            season = WeatherSeason.WINTER,
            condition = WeatherCondition.COLD_DIM,
        )

        val advice = WeatherAdviceGenerator.adviceFor(basil, snapshot)

        assertTrue(advice.starterAdvice.contains("sunniest window", ignoreCase = true))
        assertTrue(advice.reminderHint.contains("winter light-check", ignoreCase = true))
    }

    @Test
    fun tomatoAdviceHighlightsSupportDuringWarmSunnyStretch() {
        val tomato = StarterPlants.options.first { it.id == "tomato" }
        val snapshot = WeatherSnapshot(
            city = WeatherCatalog.cityById("barcelona"),
            season = WeatherSeason.SUMMER,
            condition = WeatherCondition.HOT_DRY,
        )

        val advice = WeatherAdviceGenerator.adviceFor(tomato, snapshot)

        assertTrue(advice.starterAdvice.contains("support", ignoreCase = true))
        assertTrue(advice.starterAdvice.contains("watering", ignoreCase = true))
    }

    @Test
    fun tomatoAdviceCallsOutAirflowInHumidSummerConditions() {
        val tomato = StarterPlants.options.first { it.id == "tomato" }
        val snapshot = WeatherSnapshot(
            city = WeatherCatalog.cityById("seoul"),
            season = WeatherSeason.SUMMER,
            condition = WeatherCondition.MILD_HUMID,
        )

        val advice = WeatherAdviceGenerator.adviceFor(tomato, snapshot)

        assertTrue(advice.starterAdvice.contains("airflow", ignoreCase = true))
        assertTrue(advice.starterAdvice.contains("disease pressure", ignoreCase = true))
    }

    @Test
    fun summaryAvoidsOverpromisingRealTimeWeather() {
        val tomato = StarterPlants.options.first { it.id == "tomato" }
        val snapshot = WeatherSnapshot(
            city = WeatherCatalog.cityById("berlin"),
            season = WeatherSeason.SUMMER,
            condition = WeatherCondition.WARM_SUNNY,
        )

        val advice = WeatherAdviceGenerator.adviceFor(tomato, snapshot)

        assertTrue(advice.summary.contains("seasonally", ignoreCase = true))
        assertFalse(advice.summary.contains("right now", ignoreCase = true))
    }
}
