package com.blue236.greenbuddy.data.content

import android.content.Context
import android.util.Log
import com.blue236.greenbuddy.model.DailyPlant
import org.json.JSONArray

class DailyPlantLoader(private val context: Context) {
    private var cache: List<DailyPlant>? = null

    fun load(): List<DailyPlant> {
        cache?.let { return it }
        val result = runCatching { loadFromAsset() }
            .onFailure { Log.w(TAG, "Failed to load daily-plants.json", it) }
            .getOrDefault(emptyList())
        cache = result
        return result
    }

    private fun loadFromAsset(): List<DailyPlant> {
        val json = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    DailyPlant(
                        id = obj.getString("id"),
                        commonName = obj.getString("commonName"),
                        scientificName = obj.getString("scientificName"),
                        family = obj.getString("family"),
                        emoji = obj.getString("emoji"),
                        sunlight = obj.getString("sunlight"),
                        watering = obj.getString("watering"),
                        nativeRegion = obj.getString("nativeRegion"),
                        funFact = obj.getString("funFact"),
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "DailyPlantLoader"
        const val ASSET_PATH = "content/daily-plants.json"
    }
}
