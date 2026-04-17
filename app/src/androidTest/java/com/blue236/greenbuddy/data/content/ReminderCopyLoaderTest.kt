package com.blue236.greenbuddy.data.content

import androidx.test.platform.app.InstrumentationRegistry
import com.blue236.greenbuddy.model.ReminderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class ReminderCopyLoaderTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun copyFor_loadsLocalizedKoreanReminderCopy() {
        val loader = ReminderCopyLoader(context)

        val copy = loader.copyFor("ko")

        assertEquals("GreenBuddy 리듬을 이어 가요", copy[ReminderType.STREAK_WARNING]?.title)
        assertEquals("{starterName} 상태를 가볍게 확인해 주세요", copy[ReminderType.CARE]?.title)
    }

    @Test
    fun copyFor_loadsLocalizedGermanReminderCopy() {
        val loader = ReminderCopyLoader(context)

        val copy = loader.copyFor("de")

        assertEquals("Halte deinen GreenBuddy-Rhythmus lebendig", copy[ReminderType.STREAK_WARNING]?.title)
        assertEquals("Eine neue GreenBuddy-Lektion ist bereit", copy[ReminderType.LESSON_READY]?.title)
    }

    @Test
    fun copyFor_returnsCachedInstanceForRepeatedLanguageRequests() {
        val loader = ReminderCopyLoader(context)

        val first = loader.copyFor("ko")
        val second = loader.copyFor("ko-KR")
        val different = loader.copyFor("de")

        assertSame(first, second)
        assertNotSame(first, different)
    }
}
