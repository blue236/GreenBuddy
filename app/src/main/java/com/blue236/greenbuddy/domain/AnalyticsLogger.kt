package com.blue236.greenbuddy.domain

import android.util.Log

interface AnalyticsLogger {
    fun log(event: AnalyticsEvent)
}

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap(),
)

class AndroidAnalyticsLogger(
    private val tag: String = "GreenBuddyAnalytics",
) : AnalyticsLogger {
    override fun log(event: AnalyticsEvent) {
        val payload = if (event.params.isEmpty()) {
            event.name
        } else {
            buildString {
                append(event.name)
                append(" | ")
                append(event.params.entries.joinToString { "${it.key}=${it.value}" })
            }
        }
        Log.d(tag, payload)
    }
}
