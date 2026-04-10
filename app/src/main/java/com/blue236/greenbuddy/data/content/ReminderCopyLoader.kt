package com.blue236.greenbuddy.data.content

import android.content.Context
import android.util.Log
import com.blue236.greenbuddy.model.ReminderType
import org.json.JSONObject

data class ReminderCopy(
    val title: String,
    val message: String? = null,
    val messageWithLesson: String? = null,
    val messageWithoutLesson: String? = null,
)

class ReminderCopyLoader(
    private val context: Context,
) {
    private val cache = mutableMapOf<String, Map<ReminderType, ReminderCopy>>()

    fun copyFor(languageTag: String): Map<ReminderType, ReminderCopy> {
        val normalized = languageTag.lowercase().substringBefore('-').ifBlank { "en" }
        cache[normalized]?.let { return it }

        val loaded = runCatching { loadFromAsset("content/reminders/reminders-$normalized.json") }
            .onFailure { error ->
                Log.w(TAG, "Failed to load reminder copy for lang=$normalized", error)
            }
            .getOrNull()
            ?: if (normalized != "en") {
                runCatching { loadFromAsset("content/reminders/reminders-en.json") }
                    .onFailure { error ->
                        Log.w(TAG, "Failed to load fallback reminder copy for lang=$normalized", error)
                    }
                    .getOrNull()
            } else {
                null
            }
            ?: emptyMap()

        cache[normalized] = loaded
        return loaded
    }

    private fun loadFromAsset(assetPath: String): Map<ReminderType, ReminderCopy> {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        return buildMap {
            put(ReminderType.STREAK_WARNING, root.getJSONObject("streak_warning").toReminderCopy())
            put(ReminderType.CARE, root.getJSONObject("care").toReminderCopy())
            put(ReminderType.LESSON_READY, root.getJSONObject("lesson_ready").toReminderCopy())
        }
    }

    private fun JSONObject.toReminderCopy(): ReminderCopy = ReminderCopy(
        title = getString("title"),
        message = optString("message").takeIf { it.isNotBlank() },
        messageWithLesson = optString("message_with_lesson").takeIf { it.isNotBlank() },
        messageWithoutLesson = optString("message_without_lesson").takeIf { it.isNotBlank() },
    )

    private companion object {
        const val TAG = "ReminderCopyLoader"
    }
}
