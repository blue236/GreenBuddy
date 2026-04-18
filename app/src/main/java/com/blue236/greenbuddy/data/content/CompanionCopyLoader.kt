package com.blue236.greenbuddy.data.content

import android.content.Context
import android.util.Log
import com.blue236.greenbuddy.model.CompanionChatIntent
import org.json.JSONObject

data class CompanionCopySet(
    val baseSuggestionChips: List<String> = emptyList(),
    val defaultPrompts: Map<CompanionChatIntent, String> = emptyMap(),
    val intentSuggestionChips: Map<CompanionChatIntent, List<String>> = emptyMap(),
    val proactiveBubbles: Map<String, String> = emptyMap(),
)

class CompanionCopyLoader(
    private val context: Context,
) {
    private val cache = mutableMapOf<String, CompanionCopySet>()

    fun copyFor(languageTag: String): CompanionCopySet {
        val normalized = languageTag.lowercase().substringBefore('-').ifBlank { "en" }
        cache[normalized]?.let { return it }

        val loaded = runCatching { loadFromAsset("content/companion/companion-copy-$normalized.json") }
            .onFailure { error ->
                Log.w(TAG, "Failed to load companion copy for lang=$normalized", error)
            }
            .getOrNull()
            ?: if (normalized != "en") {
                runCatching { loadFromAsset("content/companion/companion-copy-en.json") }
                    .onFailure { error ->
                        Log.w(TAG, "Failed to load fallback companion copy for lang=$normalized", error)
                    }
                    .getOrNull()
            } else {
                null
            }
            ?: CompanionCopySet()

        cache[normalized] = loaded
        return loaded
    }

    private fun loadFromAsset(assetPath: String): CompanionCopySet {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val defaultPrompts = root.getJSONObject("default_prompts").toStringMap()
            .mapKeys { CompanionChatIntent.valueOf(it.key) }
        val intentSuggestionChips = root.getJSONObject("intent_suggestion_chips").toListMap()
            .mapKeys { CompanionChatIntent.valueOf(it.key) }
        val baseSuggestionChips = root.getJSONArray("base_suggestion_chips").toStringList()
        val proactiveBubbles = root.optJSONObject("proactive_bubbles")?.toStringMap().orEmpty()
        return CompanionCopySet(
            baseSuggestionChips = baseSuggestionChips,
            defaultPrompts = defaultPrompts,
            intentSuggestionChips = intentSuggestionChips,
            proactiveBubbles = proactiveBubbles,
        )
    }

    private fun JSONObject.toStringMap(): Map<String, String> = keys().asSequence().associateWith { key -> getString(key) }

    private fun JSONObject.toListMap(): Map<String, List<String>> = keys().asSequence().associateWith { key ->
        getJSONArray(key).toStringList()
    }

    private fun org.json.JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) add(getString(index))
    }

    private companion object {
        const val TAG = "CompanionCopyLoader"
    }
}
