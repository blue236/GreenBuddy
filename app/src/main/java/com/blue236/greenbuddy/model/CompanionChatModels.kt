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

enum class CompanionMessageRole {
    USER,
    COMPANION,
}

data class CompanionMessage(
    val role: CompanionMessageRole,
    val text: String,
    val intent: CompanionChatIntent? = null,
)

data class CompanionConversationMemory(
    val messages: List<CompanionMessage> = emptyList(),
) {
    fun withExchange(userMessage: String, userIntent: CompanionChatIntent, companionReply: String): CompanionConversationMemory {
        val updated = messages + listOf(
            CompanionMessage(role = CompanionMessageRole.USER, text = userMessage, intent = userIntent),
            CompanionMessage(role = CompanionMessageRole.COMPANION, text = companionReply, intent = userIntent),
        )
        return copy(messages = updated.takeLast(MAX_MESSAGES))
    }

    val lastIntent: CompanionChatIntent?
        get() = messages.lastOrNull { it.role == CompanionMessageRole.USER }?.intent

    companion object {
        const val MAX_EXCHANGES = 4
        const val MAX_MESSAGES = MAX_EXCHANGES * 2
    }
}

data class CompanionHomeCheckIn(
    val bubble: String,
    val suggestionChips: List<String>,
)

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
    val recentConversationMemory: CompanionConversationMemory = CompanionConversationMemory(),
) {
    val realPlantSummary: String? = companionRealPlantSummary(realPlantModeState)
}

data class CompanionChatReply(
    val intent: CompanionChatIntent,
    val userMessage: String,
    val reply: String,
    val suggestionChips: List<String>,
)

private fun companionRealPlantSummary(state: RealPlantModeState): String? {
    if (!state.enabled) return null
    val completedToday = state.completedActionsOn(LocalDate.now(ZoneId.systemDefault()), ZoneId.systemDefault())
    return if (completedToday.isEmpty()) {
        "Real-plant mode is on if you want to mirror today’s real care too."
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
        recentConversationMemory: CompanionConversationMemory = CompanionConversationMemory(),
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
        recentConversationMemory = recentConversationMemory,
    )

    fun replyTo(
        message: String,
        snapshot: CompanionStateSnapshot,
        languageTag: String = "en",
    ): CompanionChatReply {
        val lang = normalizedLanguageTag(languageTag)
        val intent = detectIntent(message, snapshot.recentConversationMemory)
        val normalizedMessage = message.trim().ifBlank { defaultPromptFor(intent, lang) }
        return CompanionChatReply(
            intent = intent,
            userMessage = normalizedMessage,
            reply = renderReply(intent, normalizedMessage, snapshot, lang),
            suggestionChips = suggestionChipsFor(snapshot, intent, lang),
        )
    }

    fun proactiveCheckIn(snapshot: CompanionStateSnapshot, languageTag: String = "en"): CompanionHomeCheckIn {
        val lang = normalizedLanguageTag(languageTag)
        val bubble = renderProactiveBubble(snapshot, lang)
        val primaryIntent = when {
            snapshot.careState.lowestStat <= 40 -> CompanionChatIntent.CARE_ADVICE
            snapshot.dailyMissionSet?.allCompletedToday == false -> CompanionChatIntent.MISSION_HELP
            snapshot.growthStageState.nextStage != null && snapshot.growthStageState.readinessPercent >= 70 -> CompanionChatIntent.GROWTH_QUESTION
            snapshot.weatherSnapshot.condition == WeatherCondition.COLD_DIM -> CompanionChatIntent.WEATHER_QUESTION
            else -> CompanionChatIntent.STATUS_CHECK
        }
        return CompanionHomeCheckIn(
            bubble = bubble,
            suggestionChips = suggestionChipsFor(snapshot, primaryIntent, lang),
        )
    }

    fun updatedMemoryFor(reply: CompanionChatReply, snapshot: CompanionStateSnapshot): CompanionConversationMemory =
        snapshot.recentConversationMemory.withExchange(reply.userMessage, reply.intent, reply.reply)

    fun detectIntent(message: String, memory: CompanionConversationMemory = CompanionConversationMemory()): CompanionChatIntent {
        val normalized = normalizeForIntent(message)
        return when {
            normalized.isBlank() -> memory.lastIntent ?: CompanionChatIntent.STATUS_CHECK
            normalized.containsAny(
                "weather", "season", "winter", "summer", "spring", "autumn", "fall", "climate",
                "wetter", "jahreszeit", "fruhling", "fruehling", "sommer", "herbst", "winter", "klima",
                "날씨", "계절", "봄", "여름", "가을", "겨울", "기후"
            ) -> CompanionChatIntent.WEATHER_QUESTION
            normalized.containsAny(
                "growth", "grow", "growing", "stage", "evolution", "next leaf", "fruit", "flower",
                "wachstum", "wachsen", "phase", "stufe", "entwicklung", "blute", "bluete", "frucht",
                "성장", "자라", "단계", "진화", "꽃", "열매", "다음 단계"
            ) -> CompanionChatIntent.GROWTH_QUESTION
            normalized.containsAny(
                "mission", "missions", "task", "daily", "streak", "goal", "today plan",
                "missionen", "aufgabe", "aufgaben", "taglich", "taeglich", "serie", "ziel", "heute",
                "미션", "과제", "할 일", "오늘", "계획", "연속"
            ) -> CompanionChatIntent.MISSION_HELP
            normalized.containsAny(
                "care", "water", "sun", "sunlight", "fertiliz", "feed", "help you", "need most", "thirst",
                "pflege", "giessen", "gießen", "wasser", "sonne", "licht", "dungen", "düngen", "hilfe", "durstig",
                "돌봄", "물", "햇빛", "빛", "영양", "비료", "도와", "뭐가 필요", "목말라"
            ) -> CompanionChatIntent.CARE_ADVICE
            normalized.containsAny(
                "status", "how are you", "okay", "doing", "feel", "mood", "health",
                "wie geht", "status", "okay", "wie fuhlst", "wie fuehlst", "geht es dir", "laune", "gesund",
                "어때", "어때요", "기분", "상태", "괜찮", "어떻게 지내", "건강"
            ) -> CompanionChatIntent.STATUS_CHECK
            normalized.wordCount() <= 3 && normalized.containsAny("and", "also", "what about", "then", "too", "또", "그리고", "und", "auch") -> memory.lastIntent ?: CompanionChatIntent.CASUAL_CHAT
            normalized.wordCount() <= 3 && memory.lastIntent != null -> memory.lastIntent!!
            else -> CompanionChatIntent.CASUAL_CHAT
        }
    }

    private fun renderReply(intent: CompanionChatIntent, userMessage: String, snapshot: CompanionStateSnapshot, languageTag: String): String {
        val species = snapshot.starter.companion.species
        val name = snapshot.starter.companion.name
        val mood = snapshot.careState.localizedMood(languageTag).lowercase()
        val health = snapshot.careState.localizedHealth(languageTag).lowercase()
        val stage = snapshot.growthStageState.currentStage.localizedGrowthTitle(languageTag)
        val missionSummary = missionSummary(snapshot.dailyMissionSet, languageTag)
        val realPlantSummary = localizedRealPlantSummary(snapshot.realPlantModeState, languageTag)
        val continuity = continuityLead(snapshot.recentConversationMemory, intent, languageTag)
        return when (intent) {
            CompanionChatIntent.STATUS_CHECK -> when (normalizedLanguageTag(languageTag)) {
                "de" -> when (species) {
                    "Monstera" -> joinSentences(
                        continuity,
                        "$name meldet sich: Ich fühle mich $mood und insgesamt $health.",
                        "Mein dringendstes Bedürfnis ist ${careLabel(snapshot.careState.lowestNeed, languageTag)}.",
                        "Ich bin gerade in der Phase $stage.",
                        missionSummary,
                        realPlantSummary,
                    )
                    "Basil" -> joinSentences(
                        continuity,
                        "$name checkt ein! Ich bin $mood und insgesamt $health.",
                        "Der beste schnelle Schritt ist ${careLabel(snapshot.careState.lowestNeed, languageTag)}.",
                        "Aktuell bin ich in der Phase $stage.",
                        missionSummary,
                        realPlantSummary,
                    )
                    else -> joinSentences(
                        continuity,
                        "$name berichtet: Stimmung $mood, Gesundheit $health.",
                        "Priorität hat ${careLabel(snapshot.careState.lowestNeed, languageTag)}.",
                        "Wachstumsphase: $stage.",
                        missionSummary,
                        realPlantSummary,
                    )
                }
                "ko" -> when (species) {
                    "Monstera" -> joinSentences(
                        continuity,
                        "$name 보고할게요. 지금 기분은 $mood, 전체 상태는 ${health} 쪽이에요.",
                        "가장 먼저 챙기면 좋은 건 ${careLabel(snapshot.careState.lowestNeed, languageTag)}예요.",
                        "현재 단계는 ${stage}예요.",
                        missionSummary,
                        realPlantSummary,
                    )
                    "Basil" -> joinSentences(
                        continuity,
                        "$name 체크인이에요! 지금 저는 $mood 느낌이고 전체적으로는 ${health} 상태예요.",
                        "가장 빠르게 도움 되는 건 ${careLabel(snapshot.careState.lowestNeed, languageTag)}예요.",
                        "지금 단계는 ${stage}예요.",
                        missionSummary,
                        realPlantSummary,
                    )
                    else -> joinSentences(
                        continuity,
                        "$name 브리핑이에요. 기분은 $mood, 건강 상태는 ${health}예요.",
                        "우선 액션은 ${careLabel(snapshot.careState.lowestNeed, languageTag)}예요.",
                        "성장 단계는 ${stage}예요.",
                        missionSummary,
                        realPlantSummary,
                    )
                }
                else -> when (species) {
                    "Monstera" -> joinSentences(
                        continuity,
                        "$name report: I’m feeling $mood and $health.",
                        "My lowest need is ${careLabel(snapshot.careState.lowestNeed, languageTag)}.",
                        "I’m at the $stage stage.",
                        missionSummary,
                        realPlantSummary,
                    )
                    "Basil" -> joinSentences(
                        continuity,
                        "$name check-in! I’m $mood but overall $health.",
                        "Best quick win: ${careLabel(snapshot.careState.lowestNeed, languageTag)}.",
                        "I’m currently ${stage.lowercase()}.",
                        missionSummary,
                        realPlantSummary,
                    )
                    else -> joinSentences(
                        continuity,
                        "$name briefing: mood $mood, health $health.",
                        "Priority action is ${careLabel(snapshot.careState.lowestNeed, languageTag)}.",
                        "Growth stage: $stage.",
                        missionSummary,
                        realPlantSummary,
                    )
                }
            }
            CompanionChatIntent.CARE_ADVICE -> {
                val careTip = when (snapshot.careState.lowestNeed) {
                    CareAction.WATER -> when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Gießen würde mir gerade am meisten helfen, weil meine Hydration mit ${snapshot.careState.hydration} am niedrigsten ist."
                        "ko" -> "지금은 수분 수치가 ${snapshot.careState.hydration}로 가장 낮아서 물 주기가 가장 큰 도움이 돼요."
                        else -> "A watering action would help most right now because hydration is my lowest stat at ${snapshot.careState.hydration}."
                    }
                    CareAction.MOVE_TO_SUNLIGHT -> when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Mehr Licht wäre jetzt der beste Schritt, weil mein Sonnenwert mit ${snapshot.careState.sunlight} hinterherhinkt."
                        "ko" -> "지금은 햇빛 수치가 ${snapshot.careState.sunlight}로 뒤처져 있어서 빛을 더 받는 게 가장 좋아요."
                        else -> "More light is the best move right now because sunlight is lagging at ${snapshot.careState.sunlight}."
                    }
                    CareAction.FERTILIZE -> when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Ein Nährstoff-Boost würde mir jetzt am meisten helfen, weil mein Nährwert bei ${snapshot.careState.nutrition} liegt."
                        "ko" -> "지금은 영양 수치가 ${snapshot.careState.nutrition}라서 영양을 주는 게 가장 도움이 돼요."
                        else -> "A nutrient boost would help most right now because nutrition is sitting at ${snapshot.careState.nutrition}."
                    }
                }
                joinSentences(continuity, careTip, snapshot.weatherAdvice.starterAdvice)
            }
            CompanionChatIntent.MISSION_HELP -> {
                val missions = snapshot.dailyMissionSet?.missions.orEmpty()
                val missionReply = if (missions.isEmpty()) {
                    when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Ich sehe die heutige Missionskarte noch nicht, aber eine Lektion plus eine Pflegeaktion ist meistens ein guter Start."
                        "ko" -> "아직 오늘 미션 카드는 보이지 않지만, 보통은 레슨 하나와 돌봄 액션 하나로 시작하면 좋아요."
                        else -> "I can’t see today’s mission card yet, but a lesson plus one care action is usually a smart start."
                    }
                } else {
                    val nextMission = missions.firstOrNull { !it.isCompleted } ?: missions.last()
                    when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Bester Missionszug: ${nextMission.title}. ${nextMission.description} Aktuelle Serie: ${snapshot.dailyMissionSet?.currentStreak ?: 0}."
                        "ko" -> "지금 가장 좋은 미션은 ${nextMission.title}예요. ${nextMission.description} 현재 연속 기록은 ${snapshot.dailyMissionSet?.currentStreak ?: 0}이에요."
                        else -> "Best mission move: ${nextMission.title}. ${nextMission.description} Current streak: ${snapshot.dailyMissionSet?.currentStreak ?: 0}."
                    }
                }
                joinSentences(continuity, missionReply)
            }
            CompanionChatIntent.GROWTH_QUESTION -> {
                val growth = snapshot.growthStageState
                val growthReply = growth.nextStage?.let {
                    when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Ich bin gerade in der Phase ${growth.currentStage.localizedGrowthTitle(languageTag)}. Als Nächstes kommt ${it.localizedGrowthTitle(languageTag)}, und ich bin schon ${growth.readinessPercent}% auf dem Weg dorthin. ${growth.localizedUnlockHint(languageTag)}"
                        "ko" -> "저는 지금 ${growth.currentStage.localizedGrowthTitle(languageTag)} 단계예요. 다음은 ${it.localizedGrowthTitle(languageTag)} 단계이고, 거기까지 ${growth.readinessPercent}% 왔어요. ${growth.localizedUnlockHint(languageTag)}"
                        else -> "I’m in my ${growth.currentStage.title} stage. Next up is ${it.title}, and I’m ${growth.readinessPercent}% of the way there. ${growth.unlockHint}"
                    }
                } ?: when (normalizedLanguageTag(languageTag)) {
                    "de" -> "Ich habe meine letzte Wachstumsstufe schon erreicht. Jetzt geht es darum, stabil und gesund zu bleiben."
                    "ko" -> "저는 이미 마지막 성장 단계에 도달했어요. 이제는 안정적으로 건강을 유지하는 게 목표예요."
                    else -> "I’ve already reached my final growth stage, so now the goal is staying steady and healthy."
                }
                joinSentences(continuity, growthReply)
            }
            CompanionChatIntent.WEATHER_QUESTION -> joinSentences(
                continuity,
                when (normalizedLanguageTag(languageTag)) {
                    "de" -> "In ${snapshot.weatherSnapshot.city.localizedName(languageTag)} ist gerade ${localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag)} mit eher ${localizedWeatherConditionLabel(snapshot.weatherSnapshot.condition, languageTag)} Bedingungen. ${snapshot.weatherAdvice.summary} ${snapshot.weatherAdvice.starterAdvice}"
                    "ko" -> "${snapshot.weatherSnapshot.city.localizedName(languageTag)}은 지금 ${localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag)}이고, 전반적으로 ${localizedWeatherConditionLabel(snapshot.weatherSnapshot.condition, languageTag)} 환경이에요. ${snapshot.weatherAdvice.summary} ${snapshot.weatherAdvice.starterAdvice}"
                    else -> "In ${snapshot.weatherSnapshot.city.defaultName}, it’s ${snapshot.weatherSnapshot.season.name.lowercase()} for my setup with ${snapshot.weatherSnapshot.condition.name.lowercase().replace('_', ' ')} conditions. ${snapshot.weatherAdvice.summary} ${snapshot.weatherAdvice.starterAdvice}"
                }
            )
            CompanionChatIntent.CASUAL_CHAT -> joinSentences(
                continuity,
                when (normalizedLanguageTag(languageTag)) {
                    "de" -> when (species) {
                        "Monstera" -> "Ich wirke gern gelassen und ausgeglichen — das klappt am besten, wenn meine Pflegewerte im Gleichgewicht bleiben. Frag mich nach meinem Status, wenn du den ehrlichen Blattbericht willst."
                        "Basil" -> if (userMessage.contains("thanks", ignoreCase = true) || userMessage.contains("danke", ignoreCase = true) || userMessage.contains("고마", ignoreCase = true)) "Sehr gern. Ich nehme Lob am liebsten in Form von Wasser, Licht oder sauberer Missionsarbeit." else "Ich stehe für gute Vibes und schnelles Vorankommen. Wenn du willst, helfe ich dir bei der heutigen Mission oder sage dir, welcher Pflegeschub mir am meisten bringen würde."
                        else -> "Ich mag Gespräche mit ein bisschen Ehrgeiz. Frag mich nach Wachstum, Wetter oder dem heutigen Plan, dann bleibe ich praktisch."
                    }
                    "ko" -> when (species) {
                        "Monstera" -> "저는 차분하고 균형 잡혀 보이는 걸 좋아해요. 그러려면 돌봄 수치가 고르게 유지되는 게 가장 중요해요. 솔직한 잎사귀 리포트가 궁금하면 상태를 물어봐 주세요."
                        "Basil" -> "저는 좋은 분위기와 빠른 성장 둘 다 좋아해요. 원하면 오늘 미션을 같이 보거나, 어떤 돌봄이 가장 효과적인지 바로 알려 드릴게요."
                        else -> "저는 대화에 약간의 목표 의식이 있는 걸 좋아해요. 성장, 날씨, 오늘 계획을 물어보면 실용적으로 답할게요."
                    }
                    else -> when (species) {
                        "Monstera" -> "I’m trying to look serene and well-adjusted, which is easier when my care stats stay balanced. Ask me about my status if you want the honest leaf report."
                        "Basil" -> if (userMessage.contains("thanks", ignoreCase = true)) "Any time. I accept gratitude in the form of water, sun, or a nicely maintained streak." else "I support two things: good vibes and fast progress. If you want, I can help with today’s mission or tell you what care boost would wake me up most."
                        else -> "I like a little ambition in my conversations. Ask me about growth, weather, or today’s plan and I’ll keep it practical."
                    }
                }
            )
        }
    }

    private fun renderProactiveBubble(snapshot: CompanionStateSnapshot, languageTag: String): String {
        val lang = normalizedLanguageTag(languageTag)
        val starterName = snapshot.starter.companion.name
        val nextMission = snapshot.dailyMissionSet?.missions?.firstOrNull { !it.isCompleted }
        return when {
            snapshot.careState.lowestStat <= 40 -> when (lang) {
                "de" -> "$starterName stupst dich an: Wenn du gerade nur eine Sache machst, dann bitte ${careLabel(snapshot.careState.lowestNeed, languageTag)}. Das würde mich am schnellsten beruhigen."
                "ko" -> "${starterName}가 먼저 말 걸어요. 지금 한 가지만 한다면 ${careLabel(snapshot.careState.lowestNeed, languageTag)}부터 해 주세요. 그게 저를 가장 빨리 회복시켜요."
                else -> "$starterName is nudging you: if you only do one thing right now, make it ${careLabel(snapshot.careState.lowestNeed, languageTag)}. That would steady me fastest."
            }
            snapshot.dailyMissionSet?.allCompletedToday == false && nextMission != null -> when (lang) {
                "de" -> "$starterName hat einen planfreudigen Moment: ${nextMission.title} wäre heute mein favorite next move."
                "ko" -> "${starterName}가 오늘의 다음 수를 제안해요. 지금은 ${nextMission.title}부터 가면 흐름이 좋아요."
                else -> "$starterName has a strong opinion: ${nextMission.title} feels like the best next move today."
            }
            snapshot.growthStageState.nextStage != null && snapshot.growthStageState.readinessPercent >= 70 -> when (lang) {
                "de" -> "$starterName fühlt sich nach Fortschritt an — ich bin schon ${snapshot.growthStageState.readinessPercent}% auf dem Weg zur nächsten Wachstumsstufe."
                "ko" -> "${starterName}가 성장 기분을 내고 있어요. 다음 단계까지 이미 ${snapshot.growthStageState.readinessPercent}% 왔어요."
                else -> "$starterName is feeling ambitious — I’m already ${snapshot.growthStageState.readinessPercent}% of the way to my next growth stage."
            }
            snapshot.weatherSnapshot.condition == WeatherCondition.COLD_DIM -> when (lang) {
                "de" -> "$starterName schaut zum Fenster: Heute wirkt das Licht etwas knapp. Behalte meine Energie im Blick."
                "ko" -> "${starterName}가 창가를 살펴봐요. 오늘은 빛이 조금 부족해 보여서 제 에너지를 챙겨 주세요."
                else -> "$starterName is glancing at the window: the light feels a bit thin today, so keep an eye on my energy."
            }
            else -> when (lang) {
                "de" -> "$starterName meldet sich freiwillig: Ich fühle mich ${snapshot.careState.localizedMood(languageTag).lowercase()} und bin bereit für einen kleinen Check-in."
                "ko" -> "${starterName}가 먼저 인사해요. 지금 저는 ${snapshot.careState.localizedMood(languageTag).lowercase()} 느낌이라 가볍게 체크인하기 좋아요."
                else -> "$starterName is checking in on purpose: I’m feeling ${snapshot.careState.localizedMood(languageTag).lowercase()} and ready for a small moment together."
            }
        }
    }

    private fun suggestionChipsFor(snapshot: CompanionStateSnapshot, intent: CompanionChatIntent, languageTag: String): List<String> {
        val dynamic = mutableListOf<String>()
        val lang = normalizedLanguageTag(languageTag)
        when (lang) {
            "de" -> {
                if (snapshot.careState.lowestStat <= 55) dynamic += when (snapshot.careState.lowestNeed) {
                    CareAction.WATER -> "Soll ich dich gießen?"
                    CareAction.MOVE_TO_SUNLIGHT -> "Brauchst du mehr Licht?"
                    CareAction.FERTILIZE -> "Fehlt dir Nährstoff-Schub?"
                }
                snapshot.dailyMissionSet?.missions?.firstOrNull { !it.isCompleted }?.let { dynamic += "Welche Mission zuerst?" }
                if (snapshot.growthStageState.nextStage != null) dynamic += "Wie nah bist du an der nächsten Stufe?"
                dynamic += when (intent) {
                    CompanionChatIntent.STATUS_CHECK -> listOf("Wie geht es dir?", "Was soll ich heute tun?")
                    CompanionChatIntent.CARE_ADVICE -> listOf("Hilft dir das Wetter gerade?", "Wie geht es dir jetzt?")
                    CompanionChatIntent.MISSION_HELP -> listOf("Wie läuft meine Serie?", "Hilft Pflege beim Wachstum?")
                    CompanionChatIntent.GROWTH_QUESTION -> listOf("Was bremst deinen Fortschritt?", "Welche Pflege hilft dir am meisten?")
                    CompanionChatIntent.WEATHER_QUESTION -> listOf("Soll ich dein Licht anpassen?", "Was brauchst du heute am meisten?")
                    CompanionChatIntent.CASUAL_CHAT -> listOf("Wie geht es dir?", "Was brauchst du am meisten?")
                }
            }
            "ko" -> {
                if (snapshot.careState.lowestStat <= 55) dynamic += when (snapshot.careState.lowestNeed) {
                    CareAction.WATER -> "물 줘야 해?"
                    CareAction.MOVE_TO_SUNLIGHT -> "햇빛이 더 필요해?"
                    CareAction.FERTILIZE -> "영양이 더 필요해?"
                }
                snapshot.dailyMissionSet?.missions?.firstOrNull { !it.isCompleted }?.let { dynamic += "어떤 미션부터 할까?" }
                if (snapshot.growthStageState.nextStage != null) dynamic += "다음 단계까지 얼마나 남았어?"
                dynamic += when (intent) {
                    CompanionChatIntent.STATUS_CHECK -> listOf("지금 기분이 어때?", "오늘은 뭘 하면 돼?")
                    CompanionChatIntent.CARE_ADVICE -> listOf("날씨가 영향 있어?", "지금 상태 다시 알려 줘")
                    CompanionChatIntent.MISSION_HELP -> listOf("연속 기록은 어때?", "돌봄이 성장에 도움 돼?")
                    CompanionChatIntent.GROWTH_QUESTION -> listOf("뭐가 성장을 막고 있어?", "가장 도움 되는 돌봄은 뭐야?")
                    CompanionChatIntent.WEATHER_QUESTION -> listOf("빛을 바꿔야 할까?", "오늘 가장 필요한 게 뭐야?")
                    CompanionChatIntent.CASUAL_CHAT -> listOf("지금 기분이 어때?", "가장 필요한 게 뭐야?")
                }
            }
            else -> {
                if (snapshot.careState.lowestStat <= 55) dynamic += when (snapshot.careState.lowestNeed) {
                    CareAction.WATER -> "Should I water you?"
                    CareAction.MOVE_TO_SUNLIGHT -> "Do you need more light?"
                    CareAction.FERTILIZE -> "Need a nutrient boost?"
                }
                snapshot.dailyMissionSet?.missions?.firstOrNull { !it.isCompleted }?.let { dynamic += "Which mission first?" }
                if (snapshot.growthStageState.nextStage != null) dynamic += "How close are you to the next stage?"
                dynamic += when (intent) {
                    CompanionChatIntent.STATUS_CHECK -> listOf("How are you feeling?", "What should I do today?")
                    CompanionChatIntent.CARE_ADVICE -> listOf("How does weather affect you?", "How are you feeling now?")
                    CompanionChatIntent.MISSION_HELP -> listOf("How is my streak?", "Will care help growth?")
                    CompanionChatIntent.GROWTH_QUESTION -> listOf("What’s blocking progress?", "What care helps most?")
                    CompanionChatIntent.WEATHER_QUESTION -> listOf("Should I change your light?", "What do you need most today?")
                    CompanionChatIntent.CASUAL_CHAT -> listOf("How are you feeling?", "What do you need most?")
                }
            }
        }
        return dynamic.distinct().take(4)
    }

    fun baseSuggestionChips(languageTag: String = "en"): List<String> = when (normalizedLanguageTag(languageTag)) {
        "de" -> listOf("Wie geht es dir?", "Was brauchst du am meisten?", "Was soll ich heute tun?")
        "ko" -> listOf("지금 기분이 어때?", "가장 필요한 게 뭐야?", "오늘은 뭘 하면 돼?")
        else -> listOf("How are you feeling?", "What do you need most?", "What should I do today?")
    }

    fun defaultPromptFor(intent: CompanionChatIntent, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
        "de" -> when (intent) {
            CompanionChatIntent.STATUS_CHECK -> "Wie geht es dir?"
            CompanionChatIntent.CARE_ADVICE -> "Was brauchst du am meisten?"
            CompanionChatIntent.MISSION_HELP -> "Was soll ich heute tun?"
            CompanionChatIntent.GROWTH_QUESTION -> "Wie wächst du gerade?"
            CompanionChatIntent.WEATHER_QUESTION -> "Wie ist das Wetter für dich?"
            CompanionChatIntent.CASUAL_CHAT -> "Wollen wir kurz reden?"
        }
        "ko" -> when (intent) {
            CompanionChatIntent.STATUS_CHECK -> "지금 기분이 어때?"
            CompanionChatIntent.CARE_ADVICE -> "가장 필요한 게 뭐야?"
            CompanionChatIntent.MISSION_HELP -> "오늘은 뭘 하면 돼?"
            CompanionChatIntent.GROWTH_QUESTION -> "지금 어떻게 자라고 있어?"
            CompanionChatIntent.WEATHER_QUESTION -> "지금 날씨가 너한테 어때?"
            CompanionChatIntent.CASUAL_CHAT -> "잠깐 얘기해 볼래?"
        }
        else -> when (intent) {
            CompanionChatIntent.STATUS_CHECK -> "How are you feeling?"
            CompanionChatIntent.CARE_ADVICE -> "What do you need most?"
            CompanionChatIntent.MISSION_HELP -> "What should I do today?"
            CompanionChatIntent.GROWTH_QUESTION -> "How are you growing?"
            CompanionChatIntent.WEATHER_QUESTION -> "How’s the weather for you?"
            CompanionChatIntent.CASUAL_CHAT -> "Want to chat?"
        }
    }

    private fun continuityLead(memory: CompanionConversationMemory, intent: CompanionChatIntent, languageTag: String): String? {
        if (memory.messages.isEmpty()) return null
        if (memory.lastIntent != intent) return null
        return when (normalizedLanguageTag(languageTag)) {
            "de" -> "Wir bleiben beim Thema."
            "ko" -> "좋아요, 이 흐름으로 이어서 갈게요."
            else -> "Let’s keep building on that."
        }
    }

    private fun missionSummary(dailyMissionSet: DailyMissionSet?, languageTag: String): String = dailyMissionSet?.let { missions ->
        val remaining = missions.totalCount - missions.completedCount
        when (normalizedLanguageTag(languageTag)) {
            "de" -> if (remaining == 0) "Du hast die heutigen Missionen schon geschafft." else "Du hast heute noch $remaining Mission${if (remaining == 1) "" else "en"} offen."
            "ko" -> if (remaining == 0) "오늘 미션은 이미 다 끝냈어요." else "오늘 남은 미션이 ${remaining}개 있어요."
            else -> if (remaining == 0) "You already cleared today’s missions." else "You’ve got $remaining mission${if (remaining == 1) "" else "s"} left today."
        }
    } ?: when (normalizedLanguageTag(languageTag)) {
        "de" -> "Die heutigen Missionen laden noch in meinem kleinen Blattgehirn."
        "ko" -> "제 작은 잎사귀 두뇌에 오늘 미션이 아직 다 들어오지 않았어요."
        else -> "Today’s missions are still loading in my little leaf brain."
    }

    private fun localizedRealPlantSummary(state: RealPlantModeState, languageTag: String): String? {
        if (!state.enabled) return null
        val completedToday = state.completedActionsOn(LocalDate.now(ZoneId.systemDefault()), ZoneId.systemDefault())
        return if (completedToday.isEmpty()) {
            when (normalizedLanguageTag(languageTag)) {
                "de" -> "Der Echte-Pflanze-Modus ist an, falls du heute noch echte Pflege spiegeln willst."
                "ko" -> "오늘 실제 돌봄도 함께 반영하고 싶다면 실제 식물 모드를 사용할 수 있어요."
                else -> companionRealPlantSummary(state)
            }
        } else {
            when (normalizedLanguageTag(languageTag)) {
                "de" -> "Der Echte-Pflanze-Modus ist an, und du hast heute schon ${completedToday.size} echte Pflegeaktion${if (completedToday.size == 1) "" else "en"} gespiegelt."
                "ko" -> "실제 식물 모드가 켜져 있고, 오늘은 실제 돌봄 ${completedToday.size}개를 이미 반영했어요."
                else -> companionRealPlantSummary(state)
            }
        }
    }

    private fun careLabel(action: CareAction, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
        "de" -> when (action) {
            CareAction.WATER -> "Gießen"
            CareAction.MOVE_TO_SUNLIGHT -> "mehr Licht"
            CareAction.FERTILIZE -> "Düngen"
        }
        "ko" -> when (action) {
            CareAction.WATER -> "물 주기"
            CareAction.MOVE_TO_SUNLIGHT -> "햇빛 더 받기"
            CareAction.FERTILIZE -> "영양 주기"
        }
        else -> when (action) {
            CareAction.WATER -> "watering"
            CareAction.MOVE_TO_SUNLIGHT -> "more sunlight"
            CareAction.FERTILIZE -> "fertilizing"
        }
    }

    private fun normalizeForIntent(message: String): String = message
        .lowercase()
        .trim()
        .replace("ä", "a")
        .replace("ö", "o")
        .replace("ü", "u")
        .replace("ß", "ss")

    private fun String.containsAny(vararg needles: String): Boolean = needles.any { contains(it) }
    private fun String.wordCount(): Int = split(Regex("\\s+")).count { it.isNotBlank() }

    private fun joinSentences(vararg segments: String?): String = segments
        .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
        .joinToString(" ")
}

private fun localizedSeasonLabel(season: WeatherSeason, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
    "de" -> when (season) {
        WeatherSeason.SPRING -> "Frühling"
        WeatherSeason.SUMMER -> "Sommer"
        WeatherSeason.AUTUMN -> "Herbst"
        WeatherSeason.WINTER -> "Winter"
    }
    "ko" -> when (season) {
        WeatherSeason.SPRING -> "봄"
        WeatherSeason.SUMMER -> "여름"
        WeatherSeason.AUTUMN -> "가을"
        WeatherSeason.WINTER -> "겨울"
    }
    else -> season.name.lowercase()
}

private fun localizedWeatherConditionLabel(condition: WeatherCondition, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
    "de" -> when (condition) {
        WeatherCondition.COOL_BRIGHT -> "kühlen, hellen"
        WeatherCondition.WARM_SUNNY -> "warmen, sonnigen"
        WeatherCondition.HOT_DRY -> "heißen, trockenen"
        WeatherCondition.MILD_HUMID -> "milden, feuchten"
        WeatherCondition.COLD_DIM -> "kalten, eher dunklen"
    }
    "ko" -> when (condition) {
        WeatherCondition.COOL_BRIGHT -> "선선하고 밝은"
        WeatherCondition.WARM_SUNNY -> "따뜻하고 화창한"
        WeatherCondition.HOT_DRY -> "덥고 건조한"
        WeatherCondition.MILD_HUMID -> "온화하고 습한"
        WeatherCondition.COLD_DIM -> "춥고 다소 어두운"
    }
    else -> condition.name.lowercase().replace('_', ' ')
}
