package com.blue236.greenbuddy.data.content

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class LessonContentLoaderTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun lessonsFor_loadsExternalizedEnglishLessons() {
        val loader = LessonContentLoader(context)

        val lessons = loader.lessonsFor("Monstera", "en")

        assertFalse(lessons.isEmpty())
        assertEquals("monstera_light", lessons.first().id)
        assertEquals("Window light without scorch", lessons.first().title)
    }

    @Test
    fun lessonsFor_fallsBackToEnglishAssetWhenLanguageSpecificAssetIsMissing() {
        val loader = LessonContentLoader(context)

        val english = loader.lessonsFor("Basil", "en")
        val koreanFallback = loader.lessonsFor("Basil", "ko")

        assertEquals(english.map { it.id }, koreanFallback.map { it.id })
        assertEquals(english.first().title, koreanFallback.first().title)
    }

    @Test
    fun lessonsFor_returnsCachedInstanceForRepeatedRequests() {
        val loader = LessonContentLoader(context)

        val first = loader.lessonsFor("Tomato", "en")
        val second = loader.lessonsFor("Tomato", "en")
        val differentLanguageKey = loader.lessonsFor("Tomato", "de")

        assertSame(first, second)
        assertNotSame(first, differentLanguageKey)
    }
}
