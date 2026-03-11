package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LocalizationTest {
    @Test
    fun languageNormalization_supportsGermanAndKorean() {
        assertEquals("de", normalizedLanguageTag("de-DE"))
        assertEquals("ko", normalizedLanguageTag("ko-KR"))
        assertEquals("en", normalizedLanguageTag("en-US"))
    }

    @Test
    fun lessonCatalog_returnsLocalizedContent() {
        val english = LessonCatalog.forSpecies("Basil", "en").first().title
        val german = LessonCatalog.forSpecies("Basil", "de").first().title
        val korean = LessonCatalog.forSpecies("Basil", "ko").first().title
        assertNotEquals(english, german)
        assertNotEquals(english, korean)
    }

    @Test
    fun appLanguage_storageRoundTripAndEffectiveTagWork() {
        assertEquals(AppLanguage.GERMAN, AppLanguage.fromStorageValue("de"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStorageValue(null))
        assertEquals("ko", AppLanguage.SYSTEM.effectiveLanguageTag("ko-KR"))
        assertEquals("en", AppLanguage.ENGLISH.effectiveLanguageTag("de-DE"))
    }
}
