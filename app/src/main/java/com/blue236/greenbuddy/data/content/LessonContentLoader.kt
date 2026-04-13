package com.blue236.greenbuddy.data.content

import android.content.Context
import android.util.Log
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.LessonQuiz
import com.blue236.greenbuddy.model.QuizType
import org.json.JSONArray
import org.json.JSONObject

class LessonContentLoader(
    private val context: Context,
) {
    private val cache = mutableMapOf<String, List<Lesson>>()

    fun lessonsFor(species: String, languageTag: String): List<Lesson> {
        val normalized = languageTag.lowercase().substringBefore('-').ifBlank { "en" }
        val cacheKey = "$species::$normalized"
        cache[cacheKey]?.let { return it }

        val assetCandidates = listOf(
            "content/lessons-$normalized.json",
            "content/lessons-en.json",
        )
        for (assetPath in assetCandidates) {
            val loaded = runCatching { loadFromAsset(assetPath, species) }
                .onFailure { error ->
                    Log.w(TAG, "Failed to load lessons from $assetPath for species=$species lang=$normalized", error)
                }
                .getOrNull()
            if (!loaded.isNullOrEmpty()) {
                cache[cacheKey] = loaded
                return loaded
            }
        }
        return LessonCatalog.forSpecies(species, languageTag).also { cache[cacheKey] = it }
    }

    private fun loadFromAsset(assetPath: String, species: String): List<Lesson> {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val lessonsArray = root.optJSONArray(species) ?: return emptyList()
        return lessonsArray.toLessons()
    }

    private fun JSONArray.toLessons(): List<Lesson> = buildList {
        for (index in 0 until length()) {
            val item = getJSONObject(index)
            add(
                Lesson(
                    id = item.getString("id"),
                    title = item.getString("title"),
                    summary = item.getString("summary"),
                    concept = item.getString("concept"),
                    keyTakeaway = item.getString("keyTakeaway"),
                    quiz = item.getJSONObject("quiz").toLessonQuiz(),
                    rewardXp = item.getInt("rewardXp"),
                    rewardLabel = item.getString("rewardLabel"),
                ),
            )
        }
    }

    private fun JSONObject.toLessonQuiz(): LessonQuiz = LessonQuiz(
        type = QuizType.valueOf(getString("type")),
        prompt = getString("prompt"),
        options = getJSONArray("options").toStringList(),
        correctAnswerIndex = getInt("correctAnswerIndex"),
    )

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) add(getString(index))
    }

    private companion object {
        const val TAG = "LessonContentLoader"
    }
}
