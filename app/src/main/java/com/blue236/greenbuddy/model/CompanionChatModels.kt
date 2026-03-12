package com.blue236.greenbuddy.model

import java.time.LocalDate
import java.time.ZoneId

enum class CompanionChatIntent {
    STATUS_CHECK,
    CARE_ADVICE,
    MISSION_HELP,
    GROWTH_QUESTION,
    WEATHER_QUESTION,
    CASUAL_CHAT,
}

data class CompanionStateSnapshot(
    val starter: StarterPlantOption,
    val personality: CompanionPersonality,
    val mood: String,
    val health: String,
    val careState: PlantCareState,
    val growthStageState: GrowthStageState,
    val dailyMissionSet: DailyMissionSet?,
    val weatherSnapshot: WeatherSnapshot,
    val weatherAdvice: WeatherAdvice,
    val realPlantModeState: RealPlantModeState,
) {
    val realPlantSummary: String = companionRealPlantSummary(realPlantModeState)
}

data class CompanionChatReply(
    val intent: CompanionChatIntent,
    val userMessage: String,
    val reply: String,
    val suggestionChips: List<String>,
)

private fun companionRealPlantSummary(state: RealPlantModeState): String {
    if (!state.enabled) return "Real-plant mode is off right now."
    val completedToday = state.completedActionsOn(LocalDate.now(ZoneId.systemDefault()), ZoneId.systemDefault())
    return if (completedToday.isEmpty()) {
        "Real-plant mode is on, but you haven’t logged a real care action yet today."
    } else {
        "Real-plant mode is on, and you’ve already mirrored ${completedToday.size} real-world care action${if (completedToday.size == 1) "" else "s"} today."
    }
}

object CompanionChatEngine {
    fun createSnapshot(
        starter: StarterPlantOption,
        careState: PlantCareState,
        growthStageState: GrowthStageState,
        dailyMissionSet: DailyMissionSet?,
        weatherSnapshot: WeatherSnapshot,
        weatherAdvice: WeatherAdvice,
        realPlantModeState: RealPlantModeState,
        languageTag: String = "en",
    ): CompanionStateSnapshot = CompanionStateSnapshot(
        starter = starter,
        personality = CompanionPersonalitySystem.personalityFor(starter.companion.species, languageTag),
        mood = careState.mood,
        health = careState.health,
        careState = careState,
        growthStageState = growthStageState,
        dailyMissionSet = dailyMissionSet,
        weatherSnapshot = weatherSnapshot,
        weatherAdvice = weatherAdvice,
        realPlantModeState = realPlantModeState,
    )

    fun replyTo(message: String, snapshot: CompanionStateSnapshot, languageTag: String = "en"): CompanionChatReply {
        val intent = detectIntent(message)
        val normalizedMessage = message.trim().ifBlank { defaultPromptFor(intent, normalizedLanguageTag(languageTag)) }
        return CompanionChatReply(
            intent = intent,
            userMessage = normalizedMessage,
            reply = renderReply(intent, snapshot),
            suggestionChips = suggestionChipsFor(intent, normalizedLanguageTag(languageTag)),
        )
    }

    fun detectIntent(message: String): CompanionChatIntent {
        val normalized = message.lowercase().trim()
        return when {
            normalized.isBlank() -> CompanionChatIntent.STATUS_CHECK
            normalized.containsAny("weather", "season", "winter", "summer", "spring", "autumn", "fall", "climate") -> CompanionChatIntent.WEATHER_QUESTION
            normalized.containsAny("growth", "grow", "growing", "stage", "evolution", "next leaf", "fruit", "flower") -> CompanionChatIntent.GROWTH_QUESTION
            normalized.containsAny("mission", "task", "daily", "streak", "goal") -> CompanionChatIntent.MISSION_HELP
            normalized.containsAny("care", "water", "sun", "sunlight", "fertiliz", "feed", "help you") -> CompanionChatIntent.CARE_ADVICE
            normalized.containsAny("status", "how are you", "okay", "doing", "feel") -> CompanionChatIntent.STATUS_CHECK
            else -> CompanionChatIntent.CASUAL_CHAT
        }
    }

    private fun renderReply(intent: CompanionChatIntent, snapshot: CompanionStateSnapshot): String {
        val species = snapshot.starter.companion.species
        val name = snapshot.starter.companion.name
        val missionSummary = snapshot.dailyMissionSet?.let { missions ->
            val remaining = missions.totalCount - missions.completedCount
            if (remaining == 0) "You already cleared today’s missions." else "You’ve got $remaining mission${if (remaining == 1) "" else "s"} left today."
        } ?: "Today’s missions are still loading in my little leaf brain."
        val realPlantSummary = snapshot.realPlantSummary
        return when (intent) {
            CompanionChatIntent.STATUS_CHECK -> when (species) {
                "Monstera" -> "$name report: I’m feeling ${snapshot.mood.lowercase()} and ${snapshot.health.lowercase()}. My lowest need is ${careLabel(snapshot.careState.lowestNeed)}. I’m at the ${snapshot.growthStageState.currentStage.title} stage, and $missionSummary $realPlantSummary"
                "Basil" -> "$name check-in! I’m ${snapshot.mood.lowercase()} but overall ${snapshot.health.lowercase()}. Best quick win: ${careLabel(snapshot.careState.lowestNeed)}. I’m currently ${snapshot.growthStageState.currentStage.title.lowercase()}, and $missionSummary $realPlantSummary"
                else -> "$name briefing: mood ${snapshot.mood.lowercase()}, health ${snapshot.health.lowercase()}. Priority action is ${careLabel(snapshot.careState.lowestNeed)}. Growth stage: ${snapshot.growthStageState.currentStage.title}. $missionSummary $realPlantSummary"
            }
            CompanionChatIntent.CARE_ADVICE -> {
                val careTip = when (snapshot.careState.lowestNeed) {
                    CareAction.WATER -> "A watering action would help most right now because hydration is my lowest stat at ${snapshot.careState.hydration}."
                    CareAction.MOVE_TO_SUNLIGHT -> "More light is the best move right now because sunlight is lagging at ${snapshot.careState.sunlight}."
                    CareAction.FERTILIZE -> "A nutrient boost would help most right now because nutrition is sitting at ${snapshot.careState.nutrition}."
                }
                "$careTip ${snapshot.weatherAdvice.starterAdvice}"
            }
            CompanionChatIntent.MISSION_HELP -> {
                val missions = snapshot.dailyMissionSet?.missions.orEmpty()
                if (missions.isEmpty()) {
                    "I can’t see today’s mission card yet, but a lesson plus one care action is usually a smart start."
                } else {
                    val nextMission = missions.firstOrNull { !it.isCompleted } ?: missions.last()
                    "Best mission move: ${nextMission.title}. ${nextMission.description} Current streak: ${snapshot.dailyMissionSet?.currentStreak ?: 0}."
                }
            }
            CompanionChatIntent.GROWTH_QUESTION -> {
                val growth = snapshot.growthStageState
                growth.nextStage?.let {
                    "I’m in my ${growth.currentStage.title} stage. Next up is ${it.title}, and I’m ${growth.readinessPercent}% of the way there. ${growth.unlockHint}"
                } ?: "I’ve already reached my final growth stage, so now the goal is staying steady and healthy."
            }
            CompanionChatIntent.WEATHER_QUESTION -> {
                "In ${snapshot.weatherSnapshot.city.defaultName}, it’s ${snapshot.weatherSnapshot.season.name.lowercase()} for my setup with ${snapshot.weatherSnapshot.condition.name.lowercase().replace('_', ' ')} conditions. ${snapshot.weatherAdvice.summary} ${snapshot.weatherAdvice.starterAdvice}"
            }
            CompanionChatIntent.CASUAL_CHAT -> when (species) {
                "Monstera" -> "I’m trying to look serene and well-adjusted, which is easier when my care stats stay balanced. Ask me about my status if you want the honest leaf report."
                "Basil" -> "I support two things: good vibes and fast progress. If you want, I can help with today’s mission or tell you what care boost would wake me up most."
                else -> "I like a little ambition in my conversations. Ask me about growth, weather, or today’s plan and I’ll keep it practical."
            }
        }
    }

    private fun suggestionChipsFor(intent: CompanionChatIntent, languageTag: String): List<String> {
        val lang = normalizedLanguageTag(languageTag)
        return when (lang) {
            "de", "ko" -> baseSuggestionChips()
            else -> when (intent) {
                CompanionChatIntent.STATUS_CHECK -> listOf("What do you need most?", "How close are you to the next stage?", "What should I do today?")
                CompanionChatIntent.CARE_ADVICE -> listOf("Should I water you?", "What about sunlight?", "How does weather affect you?")
                CompanionChatIntent.MISSION_HELP -> listOf("Which mission first?", "How is my streak?", "Will care help growth?")
                CompanionChatIntent.GROWTH_QUESTION -> listOf("What is your growth stage?", "How do you evolve?", "What’s blocking progress?")
                CompanionChatIntent.WEATHER_QUESTION -> listOf("How’s the weather for you?", "What season advice do you have?", "Should I change your light?")
                CompanionChatIntent.CASUAL_CHAT -> baseSuggestionChips()
            }
        }
    }

    private fun baseSuggestionChips(): List<String> = listOf(
        "How are you feeling?",
        "What do you need most?",
        "What should I do today?",
    )

    private fun defaultPromptFor(intent: CompanionChatIntent, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
        "de", "ko" -> "How are you feeling?"
        else -> when (intent) {
            CompanionChatIntent.STATUS_CHECK -> "How are you feeling?"
            CompanionChatIntent.CARE_ADVICE -> "What do you need most?"
            CompanionChatIntent.MISSION_HELP -> "What should I do today?"
            CompanionChatIntent.GROWTH_QUESTION -> "How are you growing?"
            CompanionChatIntent.WEATHER_QUESTION -> "How’s the weather for you?"
            CompanionChatIntent.CASUAL_CHAT -> "Want to chat?"
        }
    }

    private fun buildRealPlantSummary(state: RealPlantModeState): String {
        if (!state.enabled) return "Real-plant mode is off right now."
        val completedToday = state.completedActionsOn(LocalDate.now(ZoneId.systemDefault()), ZoneId.systemDefault())
        return if (completedToday.isEmpty()) {
            "Real-plant mode is on, but you haven’t logged a real care action yet today."
        } else {
            "Real-plant mode is on, and you’ve already mirrored ${completedToday.size} real-world care action${if (completedToday.size == 1) "" else "s"} today."
        }
    }

    private fun careLabel(action: CareAction): String = when (action) {
        CareAction.WATER -> "watering"
        CareAction.MOVE_TO_SUNLIGHT -> "more sunlight"
        CareAction.FERTILIZE -> "fertilizing"
    }

    private fun String.containsAny(vararg needles: String): Boolean = needles.any { contains(it) }
}
