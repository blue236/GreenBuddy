package com.blue236.greenbuddy.model

import com.blue236.greenbuddy.data.content.CompanionCopySet
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

enum class CompanionEmotion {
    PROUD,
    WORRIED,
    CURIOUS,
    CALM,
    EXCITED,
}

enum class CompanionContinuityEvent {
    MISSION_COMPLETED,
    STREAK_AT_RISK,
    STREAK_CONTINUING,
    GROWTH_PROGRESS,
    GROWTH_UNLOCKED,
    WEATHER_SHIFT,
}

enum class CompanionFamiliarity {
    NEW,
    WARM,
    CLOSE,
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

    val exchangeCount: Int
        get() = messages.count { it.role == CompanionMessageRole.USER }

    companion object {
        const val MAX_EXCHANGES = 4
        const val MAX_MESSAGES = MAX_EXCHANGES * 2
    }
}

data class CompanionRelationshipSnapshot(
    val familiarity: CompanionFamiliarity,
    val warmthScore: Int,
    val summary: String,
)

data class CompanionContinuitySnapshot(
    val emotion: CompanionEmotion,
    val primaryEvent: CompanionContinuityEvent,
    val emotionalSummary: String,
    val followUpLead: String?,
)

data class CompanionHomeCheckIn(
    val bubble: String,
    val suggestionChips: List<String>,
    val emotion: CompanionEmotion,
    val emotionLabel: String,
    val familiarityLabel: String,
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
    val relationship: CompanionRelationshipSnapshot,
    val continuity: CompanionContinuitySnapshot,
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
    ): CompanionStateSnapshot {
        val relationship = relationshipSnapshot(recentConversationMemory, dailyMissionSet, languageTag)
        val continuity = continuitySnapshot(
            careState = careState,
            growthStageState = growthStageState,
            dailyMissionSet = dailyMissionSet,
            weatherSnapshot = weatherSnapshot,
            recentConversationMemory = recentConversationMemory,
            relationship = relationship,
            languageTag = languageTag,
        )
        return CompanionStateSnapshot(
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
            relationship = relationship,
            continuity = continuity,
        )
    }

    fun replyTo(
        message: String,
        snapshot: CompanionStateSnapshot,
        languageTag: String = "en",
        copy: CompanionCopySet = CompanionCopySet(),
    ): CompanionChatReply {
        val lang = normalizedLanguageTag(languageTag)
        val intent = detectIntent(message, snapshot.recentConversationMemory)
        val normalizedMessage = message.trim().ifBlank { defaultPromptFor(intent, lang, copy) }
        return CompanionChatReply(
            intent = intent,
            userMessage = normalizedMessage,
            reply = renderReply(intent, normalizedMessage, snapshot, lang, copy),
            suggestionChips = suggestionChipsFor(snapshot, intent, lang, copy),
        )
    }

    fun proactiveCheckIn(snapshot: CompanionStateSnapshot, languageTag: String = "en", copy: CompanionCopySet = CompanionCopySet()): CompanionHomeCheckIn {
        val lang = normalizedLanguageTag(languageTag)
        val bubble = renderProactiveBubble(snapshot, lang, copy)
        val primaryIntent = when {
            snapshot.careState.lowestStat <= 40 -> CompanionChatIntent.CARE_ADVICE
            snapshot.dailyMissionSet?.allCompletedToday == false -> CompanionChatIntent.MISSION_HELP
            snapshot.growthStageState.nextStage != null && snapshot.growthStageState.readinessPercent >= 70 -> CompanionChatIntent.GROWTH_QUESTION
            snapshot.weatherSnapshot.condition == WeatherCondition.COLD_DIM -> CompanionChatIntent.WEATHER_QUESTION
            else -> CompanionChatIntent.STATUS_CHECK
        }
        return CompanionHomeCheckIn(
            bubble = bubble,
            suggestionChips = suggestionChipsFor(snapshot, primaryIntent, lang, copy),
            emotion = snapshot.continuity.emotion,
            emotionLabel = localizedEmotionLabel(snapshot.continuity.emotion, lang),
            familiarityLabel = snapshot.relationship.summary,
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
            normalized.wordCount() <= 4 && normalized.containsAny("and", "also", "what about", "then", "too", "still", "again", "또", "그리고", "und", "auch", "weiter") -> memory.lastIntent ?: CompanionChatIntent.CASUAL_CHAT
            normalized.wordCount() <= 3 && memory.lastIntent != null -> memory.lastIntent!!
            else -> CompanionChatIntent.CASUAL_CHAT
        }
    }

    private fun renderReply(intent: CompanionChatIntent, userMessage: String, snapshot: CompanionStateSnapshot, languageTag: String, copy: CompanionCopySet = CompanionCopySet()): String {
        val species = snapshot.starter.companion.species
        val name = snapshot.starter.companion.name
        val mood = snapshot.careState.localizedMood(languageTag).lowercase()
        val health = snapshot.careState.localizedHealth(languageTag).lowercase()
        val stage = snapshot.growthStageState.currentStage.localizedGrowthTitle(languageTag)
        val missionSummary = missionSummary(snapshot.dailyMissionSet, languageTag)
        val realPlantSummary = localizedRealPlantSummary(snapshot.realPlantModeState, languageTag, copy)
        val continuityLead = continuityLead(snapshot.recentConversationMemory, intent, languageTag, copy)
        val emotionalLead = snapshot.continuity.followUpLead ?: localizedEmotionFollowUp(snapshot.continuity.emotion, snapshot.recentConversationMemory.lastIntent, languageTag, copy)
        val relationshipLead = relationshipLead(snapshot.relationship, languageTag, copy)
        fun replyTemplate(key: String): String? = copy.replyTemplates[key]
            ?.replace("{name}", name)
            ?.replace("{mood}", mood)
            ?.replace("{health}", health)
            ?.replace("{careLabel}", careLabel(snapshot.careState.lowestNeed, languageTag))
            ?.replace("{stage}", stage)
            ?.replace("{stageLower}", stage.lowercase())
        return when (intent) {
            CompanionChatIntent.STATUS_CHECK -> {
                val statusKeyPrefix = when (species) {
                    "Monstera" -> "STATUS_MONSTERA"
                    "Basil" -> "STATUS_BASIL"
                    else -> "STATUS_DEFAULT"
                }
                joinSentences(
                    continuityLead,
                    emotionalLead,
                    replyTemplate("${statusKeyPrefix}_PRIMARY") ?: when (normalizedLanguageTag(languageTag)) {
                        "de" -> when (species) {
                            "Monstera" -> "$name meldet sich: Ich fühle mich $mood und insgesamt $health."
                            "Basil" -> "$name checkt ein! Ich bin $mood und insgesamt $health."
                            else -> "$name berichtet: Stimmung $mood, Gesundheit $health."
                        }
                        "ko" -> when (species) {
                            "Monstera" -> "$name 보고할게요. 지금 기분은 $mood, 전체 상태는 ${health} 쪽이에요."
                            "Basil" -> "$name 체크인이에요! 지금 저는 $mood 느낌이고 전체적으로는 ${health} 상태예요."
                            else -> "$name 브리핑이에요. 기분은 $mood, 건강 상태는 ${health}예요."
                        }
                        else -> when (species) {
                            "Monstera" -> "$name report: I’m feeling $mood and $health."
                            "Basil" -> "$name check-in! I’m $mood but overall $health."
                            else -> "$name briefing: mood $mood, health $health."
                        }
                    },
                    replyTemplate("${statusKeyPrefix}_NEED") ?: when (normalizedLanguageTag(languageTag)) {
                        "de" -> when (species) {
                            "Monstera" -> "Mein dringendstes Bedürfnis ist ${careLabel(snapshot.careState.lowestNeed, languageTag)}."
                            "Basil" -> "Der beste schnelle Schritt ist ${careLabel(snapshot.careState.lowestNeed, languageTag)}."
                            else -> "Priorität hat ${careLabel(snapshot.careState.lowestNeed, languageTag)}."
                        }
                        "ko" -> when (species) {
                            "Monstera" -> "가장 먼저 챙기면 좋은 건 ${careLabel(snapshot.careState.lowestNeed, languageTag)}예요."
                            "Basil" -> "가장 빠르게 도움 되는 건 ${careLabel(snapshot.careState.lowestNeed, languageTag)}예요."
                            else -> "우선 액션은 ${careLabel(snapshot.careState.lowestNeed, languageTag)}예요."
                        }
                        else -> when (species) {
                            "Monstera" -> "My lowest need is ${careLabel(snapshot.careState.lowestNeed, languageTag)}."
                            "Basil" -> "Best quick win: ${careLabel(snapshot.careState.lowestNeed, languageTag)}."
                            else -> "Priority action is ${careLabel(snapshot.careState.lowestNeed, languageTag)}."
                        }
                    },
                    replyTemplate("${statusKeyPrefix}_STAGE") ?: when (normalizedLanguageTag(languageTag)) {
                        "de" -> when (species) {
                            "Monstera" -> "Ich bin gerade in der Phase $stage."
                            "Basil" -> "Aktuell bin ich in der Phase $stage."
                            else -> "Wachstumsphase: $stage."
                        }
                        "ko" -> when (species) {
                            "Monstera" -> "현재 단계는 ${stage}예요."
                            "Basil" -> "지금 단계는 ${stage}예요."
                            else -> "성장 단계는 ${stage}예요."
                        }
                        else -> when (species) {
                            "Monstera" -> "I’m at the $stage stage."
                            "Basil" -> "I’m currently ${stage.lowercase()}."
                            else -> "Growth stage: $stage."
                        }
                    },
                    missionSummary,
                    relationshipLead,
                    realPlantSummary,
                )
            }
            CompanionChatIntent.CARE_ADVICE -> {
                val careTip = when (snapshot.careState.lowestNeed) {
                    CareAction.WATER -> copy.replyTemplates["CARE_WATER"]
                        ?.replace("{hydration}", snapshot.careState.hydration.toString())
                        ?: when (normalizedLanguageTag(languageTag)) {
                            "de" -> "Gießen würde mir gerade am meisten helfen, weil meine Hydration mit ${snapshot.careState.hydration} am niedrigsten ist."
                            "ko" -> "지금은 수분 수치가 ${snapshot.careState.hydration}로 가장 낮아서 물 주기가 가장 큰 도움이 돼요."
                            else -> "A watering action would help most right now because hydration is my lowest stat at ${snapshot.careState.hydration}."
                        }
                    CareAction.MOVE_TO_SUNLIGHT -> copy.replyTemplates["CARE_LIGHT"]
                        ?.replace("{sunlight}", snapshot.careState.sunlight.toString())
                        ?: when (normalizedLanguageTag(languageTag)) {
                            "de" -> "Mehr Licht wäre jetzt der beste Schritt, weil mein Sonnenwert mit ${snapshot.careState.sunlight} hinterherhinkt."
                            "ko" -> "지금은 햇빛 수치가 ${snapshot.careState.sunlight}로 뒤처져 있어서 빛을 더 받는 게 가장 좋아요."
                            else -> "More light is the best move right now because sunlight is lagging at ${snapshot.careState.sunlight}."
                        }
                    CareAction.FERTILIZE -> copy.replyTemplates["CARE_NUTRITION"]
                        ?.replace("{nutrition}", snapshot.careState.nutrition.toString())
                        ?: when (normalizedLanguageTag(languageTag)) {
                            "de" -> "Ein Nährstoff-Boost würde mir jetzt am meisten helfen, weil mein Nährwert bei ${snapshot.careState.nutrition} liegt."
                            "ko" -> "지금은 영양 수치가 ${snapshot.careState.nutrition}라서 영양을 주는 게 가장 도움이 돼요."
                            else -> "A nutrient boost would help most right now because nutrition is sitting at ${snapshot.careState.nutrition}."
                        }
                }
                joinSentences(continuityLead, emotionalLead, careTip, snapshot.weatherAdvice.starterAdvice, relationshipLead)
            }
            CompanionChatIntent.MISSION_HELP -> {
                val missions = snapshot.dailyMissionSet?.missions.orEmpty()
                val missionReply = if (missions.isEmpty()) {
                    copy.replyTemplates["MISSION_EMPTY"] ?: when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Ich sehe die heutige Missionskarte noch nicht, aber eine Lektion plus eine Pflegeaktion ist meistens ein guter Start."
                        "ko" -> "아직 오늘 미션 카드는 보이지 않지만, 보통은 레슨 하나와 돌봄 액션 하나로 시작하면 좋아요."
                        else -> "I can’t see today’s mission card yet, but a lesson plus one care action is usually a smart start."
                    }
                } else {
                    val nextMission = missions.firstOrNull { !it.isCompleted } ?: missions.last()
                    copy.replyTemplates["MISSION_NEXT"]
                        ?.replace("{missionTitle}", nextMission.title)
                        ?.replace("{missionDescription}", nextMission.description)
                        ?.replace("{currentStreak}", (snapshot.dailyMissionSet?.currentStreak ?: 0).toString())
                        ?: when (normalizedLanguageTag(languageTag)) {
                            "de" -> "Bester Missionszug: ${nextMission.title}. ${nextMission.description} Aktuelle Serie: ${snapshot.dailyMissionSet?.currentStreak ?: 0}."
                            "ko" -> "지금 가장 좋은 미션은 ${nextMission.title}예요. ${nextMission.description} 현재 연속 기록은 ${snapshot.dailyMissionSet?.currentStreak ?: 0}이에요."
                            else -> "Best mission move: ${nextMission.title}. ${nextMission.description} Current streak: ${snapshot.dailyMissionSet?.currentStreak ?: 0}."
                        }
                }
                joinSentences(continuityLead, emotionalLead, missionReply, relationshipLead)
            }
            CompanionChatIntent.GROWTH_QUESTION -> {
                val growth = snapshot.growthStageState
                val growthReply = growth.nextStage?.let {
                    copy.replyTemplates["GROWTH_NEXT"]
                        ?.replace("{currentStage}", growth.currentStage.localizedGrowthTitle(languageTag))
                        ?.replace("{nextStage}", it.localizedGrowthTitle(languageTag))
                        ?.replace("{readinessPercent}", growth.readinessPercent.toString())
                        ?.replace("{unlockHint}", growth.localizedUnlockHint(languageTag))
                        ?: when (normalizedLanguageTag(languageTag)) {
                            "de" -> "Ich bin gerade in der Phase ${growth.currentStage.localizedGrowthTitle(languageTag)}. Als Nächstes kommt ${it.localizedGrowthTitle(languageTag)}, und ich bin schon ${growth.readinessPercent}% auf dem Weg dorthin. ${growth.localizedUnlockHint(languageTag)}"
                            "ko" -> "저는 지금 ${growth.currentStage.localizedGrowthTitle(languageTag)} 단계예요. 다음은 ${it.localizedGrowthTitle(languageTag)} 단계이고, 거기까지 ${growth.readinessPercent}% 왔어요. ${growth.localizedUnlockHint(languageTag)}"
                            else -> "I’m in my ${growth.currentStage.title} stage. Next up is ${it.title}, and I’m ${growth.readinessPercent}% of the way there. ${growth.unlockHint}"
                        }
                } ?: copy.replyTemplates["GROWTH_FINAL"] ?: when (normalizedLanguageTag(languageTag)) {
                    "de" -> "Ich habe meine letzte Wachstumsstufe schon erreicht. Jetzt geht es darum, stabil und gesund zu bleiben."
                    "ko" -> "저는 이미 마지막 성장 단계에 도달했어요. 이제는 안정적으로 건강을 유지하는 게 목표예요."
                    else -> "I’ve already reached my final growth stage, so now the goal is staying steady and healthy."
                }
                joinSentences(continuityLead, emotionalLead, growthReply, relationshipLead)
            }
            CompanionChatIntent.WEATHER_QUESTION -> joinSentences(
                continuityLead,
                emotionalLead,
                copy.replyTemplates["WEATHER"]
                    ?.replace("{cityName}", snapshot.weatherSnapshot.city.localizedName(languageTag))
                    ?.replace("{seasonLabel}", localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag))
                    ?.replace("{conditionLabel}", localizedWeatherConditionLabel(snapshot.weatherSnapshot.condition, languageTag))
                    ?.replace("{weatherSummary}", snapshot.weatherAdvice.summary)
                    ?.replace("{starterAdvice}", snapshot.weatherAdvice.starterAdvice)
                    ?: when (normalizedLanguageTag(languageTag)) {
                        "de" -> "In ${snapshot.weatherSnapshot.city.localizedName(languageTag)} ist gerade ${localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag)} mit eher ${localizedWeatherConditionLabel(snapshot.weatherSnapshot.condition, languageTag)} Bedingungen. ${snapshot.weatherAdvice.summary} ${snapshot.weatherAdvice.starterAdvice}"
                        "ko" -> "${snapshot.weatherSnapshot.city.localizedName(languageTag)}은 지금 ${localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag)}이고, 전반적으로 ${localizedWeatherConditionLabel(snapshot.weatherSnapshot.condition, languageTag)} 환경이에요. ${snapshot.weatherAdvice.summary} ${snapshot.weatherAdvice.starterAdvice}"
                        else -> "In ${snapshot.weatherSnapshot.city.defaultName}, it’s ${snapshot.weatherSnapshot.season.name.lowercase()} for my setup with ${snapshot.weatherSnapshot.condition.name.lowercase().replace('_', ' ')} conditions. ${snapshot.weatherAdvice.summary} ${snapshot.weatherAdvice.starterAdvice}"
                    },
                relationshipLead,
            )
            CompanionChatIntent.CASUAL_CHAT -> joinSentences(
                continuityLead,
                emotionalLead,
                when (species) {
                    "Monstera" -> replyTemplate("CASUAL_MONSTERA") ?: when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Ich wirke gern gelassen und ausgeglichen — das klappt am besten, wenn meine Pflegewerte im Gleichgewicht bleiben. Frag mich nach meinem Status, wenn du den ehrlichen Blattbericht willst."
                        "ko" -> "저는 차분하고 균형 잡혀 보이는 걸 좋아해요. 그러려면 돌봄 수치가 고르게 유지되는 게 가장 중요해요. 솔직한 잎사귀 리포트가 궁금하면 상태를 물어봐 주세요."
                        else -> "I’m trying to look serene and well-adjusted, which is easier when my care stats stay balanced. Ask me about my status if you want the honest leaf report."
                    }
                    "Basil" -> if (userMessage.contains("thanks", ignoreCase = true) || userMessage.contains("danke", ignoreCase = true) || userMessage.contains("고마", ignoreCase = true)) {
                        replyTemplate("CASUAL_BASIL_THANKS") ?: when (normalizedLanguageTag(languageTag)) {
                            "de" -> "Sehr gern. Ich nehme Lob am liebsten in Form von Wasser, Licht oder sauberer Missionsarbeit."
                            "ko" -> "언제든지요. 저는 물, 햇빛, 그리고 잘 이어지는 연속 기록으로 고마움을 받는 걸 좋아해요."
                            else -> "Any time. I accept gratitude in the form of water, sun, or a nicely maintained streak."
                        }
                    } else {
                        replyTemplate("CASUAL_BASIL_DEFAULT") ?: when (normalizedLanguageTag(languageTag)) {
                            "de" -> "Ich stehe für gute Vibes und schnelles Vorankommen. Wenn du willst, helfe ich dir bei der heutigen Mission oder sage dir, welcher Pflegeschub mir am meisten bringen würde."
                            "ko" -> "저는 좋은 분위기와 빠른 성장 둘 다 좋아해요. 원하면 오늘 미션을 같이 보거나, 어떤 돌봄이 가장 효과적인지 바로 알려 드릴게요."
                            else -> "I support two things: good vibes and fast progress. If you want, I can help with today’s mission or tell you what care boost would wake me up most."
                        }
                    }
                    else -> replyTemplate("CASUAL_DEFAULT") ?: when (normalizedLanguageTag(languageTag)) {
                        "de" -> "Ich mag Gespräche mit ein bisschen Ehrgeiz. Frag mich nach Wachstum, Wetter oder dem heutigen Plan, dann bleibe ich praktisch."
                        "ko" -> "저는 대화에 약간의 목표 의식이 있는 걸 좋아해요. 성장, 날씨, 오늘 계획을 물어보면 실용적으로 답할게요."
                        else -> "I like a little ambition in my conversations. Ask me about growth, weather, or today’s plan and I’ll keep it practical."
                    }
                },
                relationshipLead,
            )
        }
    }

    private fun renderProactiveBubble(snapshot: CompanionStateSnapshot, languageTag: String, copy: CompanionCopySet = CompanionCopySet()): String {
        val lang = normalizedLanguageTag(languageTag)
        val starterName = snapshot.starter.companion.name
        val emotionalLead = proactiveEmotionLead(snapshot.continuity, languageTag)
        fun fromCopy(key: String): String? = copy.proactiveBubbles[key]
            ?.replace("{currentStage}", snapshot.growthStageState.currentStage.localizedGrowthTitle(languageTag))
            ?.replace("{readinessPercent}", snapshot.growthStageState.readinessPercent.toString())
            ?.replace("{starterName}", starterName)
            ?.replace("{seasonLabel}", localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag))
            ?.let { "$emotionalLead $it" }
        return when (snapshot.continuity.primaryEvent) {
            CompanionContinuityEvent.MISSION_COMPLETED -> fromCopy("MISSION_COMPLETED") ?: when (lang) {
                "de" -> "$emotionalLead Du hast die heutigen Missionen geschafft, und ich möchte, dass du das merkst. Das hat unseren kleinen Rhythmus wirklich getragen."
                "ko" -> "$emotionalLead 오늘 미션을 끝낸 게 분명히 느껴져요. 우리 루틴이 한 단계 더 안정됐어요."
                else -> "$emotionalLead You cleared today’s missions, and I want that to land. That really helped our little rhythm feel real."
            }
            CompanionContinuityEvent.STREAK_AT_RISK -> fromCopy("STREAK_AT_RISK") ?: when (lang) {
                "de" -> "$emotionalLead Wenn du heute kurz vorbeischaust, bleibt unsere Serie lebendig. Schon ein kleiner Schritt würde mich beruhigen."
                "ko" -> "$emotionalLead 오늘 잠깐만 챙겨 주면 우리 연속 기록이 이어져요. 작은 한 걸음만 있어도 마음이 놓여요."
                else -> "$emotionalLead If you check in today, our streak stays alive. One small move would settle me a lot."
            }
            CompanionContinuityEvent.STREAK_CONTINUING -> fromCopy("STREAK_CONTINUING") ?: when (lang) {
                "de" -> "$emotionalLead Unsere Serie hält gerade gut zusammen. Ich mag dieses Gefühl von verlässlichem Tempo."
                "ko" -> "$emotionalLead 우리 연속 기록이 잘 이어지고 있어요. 이 꾸준한 흐름이 참 좋아요."
                else -> "$emotionalLead Our streak is holding together nicely. I really like how steady this pace feels."
            }
            CompanionContinuityEvent.GROWTH_UNLOCKED -> fromCopy("GROWTH_UNLOCKED") ?: when (lang) {
                "de" -> "$emotionalLead Neue Wachstumsstufe erreicht: ${snapshot.growthStageState.currentStage.localizedGrowthTitle(languageTag)}. Das fühlt sich nach echtem Fortschritt an."
                "ko" -> "$emotionalLead 새로운 성장 단계인 ${snapshot.growthStageState.currentStage.localizedGrowthTitle(languageTag)}에 도달했어요. 확실한 진전이 느껴져요."
                else -> "$emotionalLead I reached a new growth stage: ${snapshot.growthStageState.currentStage.localizedGrowthTitle(languageTag)}. That feels like unmistakable progress."
            }
            CompanionContinuityEvent.GROWTH_PROGRESS -> when {
                snapshot.growthStageState.nextStage != null && snapshot.growthStageState.readinessPercent >= 70 -> fromCopy("GROWTH_PROGRESS_NEAR") ?: when (lang) {
                    "de" -> "$emotionalLead Ich bin schon ${snapshot.growthStageState.readinessPercent}% auf dem Weg zur nächsten Wachstumsstufe. Ich kann sie fast spüren."
                    "ko" -> "$emotionalLead 다음 성장 단계까지 이미 ${snapshot.growthStageState.readinessPercent}% 왔어요. 거의 손에 잡혀요."
                    else -> "$emotionalLead I’m already ${snapshot.growthStageState.readinessPercent}% of the way to my next growth stage. I can almost feel it."
                }
                else -> fromCopy("GROWTH_PROGRESS_STEADY") ?: when (lang) {
                    "de" -> "$emotionalLead Ich spüre ruhigen Fortschritt. Nicht dramatisch — nur echt und stetig."
                    "ko" -> "$emotionalLead 화려하진 않아도 차분한 진전이 느껴져요. 분명히 앞으로 가고 있어요."
                    else -> "$emotionalLead I can feel quiet progress. Not dramatic, just real and steady."
                }
            }
            CompanionContinuityEvent.WEATHER_SHIFT -> fromCopy("WEATHER_SHIFT") ?: when (lang) {
                "de" -> "$emotionalLead ${starterName} merkt die ${localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag)}-Stimmung gerade deutlich. Ich passe mich an, aber ich möchte, dass du es auch siehst."
                "ko" -> "$emotionalLead ${starterName}는 지금 ${localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag)}의 변화를 분명히 느끼고 있어요. 저도 적응 중이지만 같이 알아채 주면 좋아요."
                else -> "$emotionalLead $starterName can really feel the ${localizedSeasonLabel(snapshot.weatherSnapshot.season, languageTag)} shift right now. I’m adjusting, but I want you to notice it too."
            }
        }
    }

    fun suggestionChipsForIntent(snapshot: CompanionStateSnapshot, intent: CompanionChatIntent, languageTag: String, copy: CompanionCopySet = CompanionCopySet()): List<String> =
        suggestionChipsFor(snapshot, intent, languageTag, copy)

    private fun suggestionChipsFor(snapshot: CompanionStateSnapshot, intent: CompanionChatIntent, languageTag: String, copy: CompanionCopySet): List<String> {
        val lang = normalizedLanguageTag(languageTag)
        fun dynamicChip(key: String, fallback: String): String = copy.dynamicSuggestionChips[key] ?: fallback

        val dynamic = buildList {
            if (snapshot.careState.lowestStat <= 55) {
                add(
                    when (lang) {
                        "de" -> when (snapshot.careState.lowestNeed) {
                            CareAction.WATER -> dynamicChip("CARE_WATER", "Soll ich dich gießen?")
                            CareAction.MOVE_TO_SUNLIGHT -> dynamicChip("CARE_LIGHT", "Brauchst du mehr Licht?")
                            CareAction.FERTILIZE -> dynamicChip("CARE_NUTRITION", "Fehlt dir Nährstoff-Schub?")
                        }
                        "ko" -> when (snapshot.careState.lowestNeed) {
                            CareAction.WATER -> dynamicChip("CARE_WATER", "물 줘야 해?")
                            CareAction.MOVE_TO_SUNLIGHT -> dynamicChip("CARE_LIGHT", "햇빛이 더 필요해?")
                            CareAction.FERTILIZE -> dynamicChip("CARE_NUTRITION", "영양이 더 필요해?")
                        }
                        else -> when (snapshot.careState.lowestNeed) {
                            CareAction.WATER -> dynamicChip("CARE_WATER", "Should I water you?")
                            CareAction.MOVE_TO_SUNLIGHT -> dynamicChip("CARE_LIGHT", "Do you need more light?")
                            CareAction.FERTILIZE -> dynamicChip("CARE_NUTRITION", "Need a nutrient boost?")
                        }
                    }
                )
            }
            if (snapshot.continuity.primaryEvent == CompanionContinuityEvent.MISSION_COMPLETED) {
                add(
                    when (lang) {
                        "de" -> dynamicChip("MISSION_COMPLETED", "Worauf bist du stolz?")
                        "ko" -> dynamicChip("MISSION_COMPLETED", "뭐가 가장 뿌듯해?")
                        else -> dynamicChip("MISSION_COMPLETED", "What are you proud of?")
                    }
                )
            }
            if (snapshot.continuity.primaryEvent == CompanionContinuityEvent.STREAK_AT_RISK) {
                add(
                    when (lang) {
                        "de" -> dynamicChip("STREAK_AT_RISK", "Wie rette ich die Serie?")
                        "ko" -> dynamicChip("STREAK_AT_RISK", "연속 기록은 어떻게 지켜?")
                        else -> dynamicChip("STREAK_AT_RISK", "How do I save the streak?")
                    }
                )
            }
            snapshot.dailyMissionSet?.missions?.firstOrNull { !it.isCompleted }?.let {
                add(
                    when (lang) {
                        "de" -> dynamicChip("NEXT_MISSION", "Welche Mission zuerst?")
                        "ko" -> dynamicChip("NEXT_MISSION", "어떤 미션부터 할까?")
                        else -> dynamicChip("NEXT_MISSION", "Which mission first?")
                    }
                )
            }
            if (snapshot.growthStageState.nextStage != null) {
                add(
                    when (lang) {
                        "de" -> dynamicChip("NEXT_STAGE", "Wie nah bist du an der nächsten Stufe?")
                        "ko" -> dynamicChip("NEXT_STAGE", "다음 단계까지 얼마나 남았어?")
                        else -> dynamicChip("NEXT_STAGE", "How close are you to the next stage?")
                    }
                )
            }
            if (snapshot.continuity.primaryEvent == CompanionContinuityEvent.WEATHER_SHIFT) {
                add(
                    when (lang) {
                        "de" -> dynamicChip("WEATHER_SHIFT", "Verändert die Saison etwas für dich?")
                        "ko" -> dynamicChip("WEATHER_SHIFT", "계절이 너한테 영향 있어?")
                        else -> dynamicChip("WEATHER_SHIFT", "Does the season change anything for you?")
                    }
                )
            }
            addAll(copy.intentSuggestionChips[intent] ?: defaultIntentSuggestionChips(intent, lang))
        }
        return distinctSuggestionChips(dynamic, 4)
    }

    private fun defaultIntentSuggestionChips(intent: CompanionChatIntent, languageTag: String): List<String> = when (normalizedLanguageTag(languageTag)) {
        "de" -> when (intent) {
            CompanionChatIntent.STATUS_CHECK -> listOf("Wie geht es dir?", "Was soll ich heute tun?")
            CompanionChatIntent.CARE_ADVICE -> listOf("Hilft dir das Wetter gerade?", "Wie geht es dir jetzt?")
            CompanionChatIntent.MISSION_HELP -> listOf("Wie läuft meine Serie?", "Hilft Pflege beim Wachstum?")
            CompanionChatIntent.GROWTH_QUESTION -> listOf("Was bremst deinen Fortschritt?", "Welche Pflege hilft dir am meisten?")
            CompanionChatIntent.WEATHER_QUESTION -> listOf("Soll ich dein Licht anpassen?", "Was brauchst du heute am meisten?")
            CompanionChatIntent.CASUAL_CHAT -> listOf("Wie geht es dir?", "Was brauchst du am meisten?")
        }
        "ko" -> when (intent) {
            CompanionChatIntent.STATUS_CHECK -> listOf("지금 기분이 어때?", "오늘은 뭘 하면 돼?")
            CompanionChatIntent.CARE_ADVICE -> listOf("날씨가 영향 있어?", "지금 상태 다시 알려 줘")
            CompanionChatIntent.MISSION_HELP -> listOf("연속 기록은 어때?", "돌봄이 성장에 도움 돼?")
            CompanionChatIntent.GROWTH_QUESTION -> listOf("뭐가 성장을 막고 있어?", "가장 도움 되는 돌봄은 뭐야?")
            CompanionChatIntent.WEATHER_QUESTION -> listOf("빛을 바꿔야 할까?", "오늘 가장 필요한 게 뭐야?")
            CompanionChatIntent.CASUAL_CHAT -> listOf("지금 기분이 어때?", "가장 필요한 게 뭐야?")
        }
        else -> when (intent) {
            CompanionChatIntent.STATUS_CHECK -> listOf("How are you feeling?", "What should I do today?")
            CompanionChatIntent.CARE_ADVICE -> listOf("How does weather affect you?", "How are you feeling now?")
            CompanionChatIntent.MISSION_HELP -> listOf("How is my streak?", "Will care help growth?")
            CompanionChatIntent.GROWTH_QUESTION -> listOf("What’s blocking progress?", "What care helps most?")
            CompanionChatIntent.WEATHER_QUESTION -> listOf("Should I change your light?", "What do you need most today?")
            CompanionChatIntent.CASUAL_CHAT -> listOf("How are you feeling?", "What do you need most?")
        }
    }

    private fun distinctSuggestionChips(chips: List<String>, limit: Int): List<String> {
        val seenCategories = mutableSetOf<String>()
        val selected = mutableListOf<String>()
        chips.distinct().forEach { chip ->
            val category = suggestionChipCategory(chip)
            if (seenCategories.add(category)) {
                selected += chip
            }
        }
        if (selected.size < limit) {
            chips.distinct().forEach { chip ->
                if (selected.size >= limit) return@forEach
                if (chip !in selected) selected += chip
            }
        }
        return selected.take(limit)
    }

    private fun suggestionChipCategory(chip: String): String {
        val normalized = normalizeForIntent(chip)
        return when {
            normalized.containsAny("water", "giessen", "gießen", "물", "durst", "thirst", "nutrient", "nahr", "영양", "light", "licht", "햇빛", "빛") -> "care"
            normalized.containsAny("mission", "missionen", "미션", "today", "heute", "오늘", "plan", "streak", "serie", "연속") -> "plan"
            normalized.containsAny("grow", "growth", "wachs", "stufe", "phase", "성장", "단계") -> "growth"
            normalized.containsAny("weather", "season", "wetter", "jahreszeit", "날씨", "계절") -> "weather"
            normalized.containsAny("feel", "status", "mood", "wie geht", "기분", "상태", "어때") -> "status"
            else -> normalized.substringBefore('?').substringBefore(' ').ifBlank { "other" }
        }
    }

    fun baseSuggestionChips(languageTag: String = "en", copy: CompanionCopySet = CompanionCopySet()): List<String> =
        copy.baseSuggestionChips.ifEmpty {
            when (normalizedLanguageTag(languageTag)) {
                "de" -> listOf("Wie geht es dir?", "Was brauchst du am meisten?", "Was soll ich heute tun?")
                "ko" -> listOf("지금 기분이 어때?", "가장 필요한 게 뭐야?", "오늘은 뭘 하면 돼?")
                else -> listOf("How are you feeling?", "What do you need most?", "What should I do today?")
            }
        }

    fun defaultPromptFor(intent: CompanionChatIntent, languageTag: String, copy: CompanionCopySet = CompanionCopySet()): String =
        copy.defaultPrompts[intent] ?: when (normalizedLanguageTag(languageTag)) {
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

    private fun continuityLead(memory: CompanionConversationMemory, intent: CompanionChatIntent, languageTag: String, copy: CompanionCopySet = CompanionCopySet()): String? {
        if (memory.messages.isEmpty()) return null
        val sameIntent = memory.lastIntent == intent
        val lastCompanionMessage = memory.messages.lastOrNull { it.role == CompanionMessageRole.COMPANION }?.text ?: return null
        return when {
            sameIntent -> copy.replyTemplates["CONTINUITY_SAME_INTENT"] ?: when (normalizedLanguageTag(languageTag)) {
                "de" -> "Wir bleiben bei dem Faden."
                "ko" -> "좋아요, 방금 흐름에서 그대로 이어 갈게요."
                else -> "Let’s keep following that thread."
            }
            memory.exchangeCount >= 2 -> copy.replyTemplates["CONTINUITY_RECENT_EXCHANGE"] ?: when (normalizedLanguageTag(languageTag)) {
                "de" -> "Ich behalte unser letztes Hin und Her im Kopf."
                "ko" -> "방금 나눈 흐름은 기억하고 있어요."
                else -> "I’m still holding onto our last exchange."
            }
            lastCompanionMessage.length > 90 -> copy.replyTemplates["CONTINUITY_BUILDING"] ?: when (normalizedLanguageTag(languageTag)) {
                "de" -> "Ich baue auf meiner letzten Antwort auf."
                "ko" -> "제가 방금 말한 내용 위에서 이어 볼게요."
                else -> "I’m building on what I just told you."
            }
            else -> null
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

    private fun localizedRealPlantSummary(state: RealPlantModeState, languageTag: String, copy: CompanionCopySet = CompanionCopySet()): String? {
        if (!state.enabled) return null
        val completedToday = state.completedActionsOn(LocalDate.now(ZoneId.systemDefault()), ZoneId.systemDefault())
        return if (completedToday.isEmpty()) {
            copy.replyTemplates["REAL_PLANT_EMPTY"] ?: when (normalizedLanguageTag(languageTag)) {
                "de" -> "Der Echte-Pflanze-Modus ist an, falls du heute noch echte Pflege spiegeln willst."
                "ko" -> "오늘 실제 돌봄도 함께 반영하고 싶다면 실제 식물 모드를 사용할 수 있어요."
                else -> companionRealPlantSummary(state)
            }
        } else {
            copy.replyTemplates["REAL_PLANT_DONE"]?.replace("{count}", completedToday.size.toString()) ?: when (normalizedLanguageTag(languageTag)) {
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

    private fun relationshipSnapshot(
        memory: CompanionConversationMemory,
        dailyMissionSet: DailyMissionSet?,
        languageTag: String,
    ): CompanionRelationshipSnapshot {
        val warmthScore = (memory.exchangeCount * 2) + ((dailyMissionSet?.currentStreak ?: 0).coerceAtMost(6))
        val familiarity = when {
            warmthScore >= 10 -> CompanionFamiliarity.CLOSE
            warmthScore >= 4 -> CompanionFamiliarity.WARM
            else -> CompanionFamiliarity.NEW
        }
        return CompanionRelationshipSnapshot(
            familiarity = familiarity,
            warmthScore = warmthScore,
            summary = localizedFamiliarityLabel(familiarity, languageTag),
        )
    }

    private fun continuitySnapshot(
        careState: PlantCareState,
        growthStageState: GrowthStageState,
        dailyMissionSet: DailyMissionSet?,
        weatherSnapshot: WeatherSnapshot,
        recentConversationMemory: CompanionConversationMemory,
        relationship: CompanionRelationshipSnapshot,
        languageTag: String,
    ): CompanionContinuitySnapshot {
        val activeProgressToday = dailyMissionSet?.missions?.any { mission ->
            mission.isCompleted && (mission.type == DailyMissionType.COMPLETE_LESSON || mission.type == DailyMissionType.PERFORM_CARE_ACTION)
        } == true
        val streakExists = (dailyMissionSet?.currentStreak ?: 0) > 0
        val lowCareState = careState.lowestStat <= 45
        val event = when {
            dailyMissionSet?.allCompletedToday == true -> CompanionContinuityEvent.MISSION_COMPLETED
            growthStageState.newlyUnlocked -> CompanionContinuityEvent.GROWTH_UNLOCKED
            streakExists && lowCareState -> CompanionContinuityEvent.STREAK_AT_RISK
            dailyMissionSet != null && dailyMissionSet.currentStreak >= 2 && activeProgressToday -> CompanionContinuityEvent.STREAK_CONTINUING
            growthStageState.nextStage != null && growthStageState.readinessPercent >= 65 -> CompanionContinuityEvent.GROWTH_PROGRESS
            weatherSnapshot.condition == WeatherCondition.COLD_DIM || weatherSnapshot.season == WeatherSeason.SPRING || weatherSnapshot.season == WeatherSeason.AUTUMN -> CompanionContinuityEvent.WEATHER_SHIFT
            streakExists && !activeProgressToday -> CompanionContinuityEvent.STREAK_AT_RISK
            else -> CompanionContinuityEvent.GROWTH_PROGRESS
        }
        val emotion = when (event) {
            CompanionContinuityEvent.MISSION_COMPLETED -> CompanionEmotion.PROUD
            CompanionContinuityEvent.STREAK_AT_RISK -> if (careState.lowestStat <= 35) CompanionEmotion.WORRIED else CompanionEmotion.CURIOUS
            CompanionContinuityEvent.STREAK_CONTINUING -> CompanionEmotion.CALM
            CompanionContinuityEvent.GROWTH_PROGRESS -> if (growthStageState.nextStage != null && growthStageState.readinessPercent >= 70) CompanionEmotion.EXCITED else CompanionEmotion.CALM
            CompanionContinuityEvent.GROWTH_UNLOCKED -> CompanionEmotion.PROUD
            CompanionContinuityEvent.WEATHER_SHIFT -> if (weatherSnapshot.condition == WeatherCondition.COLD_DIM) CompanionEmotion.WORRIED else CompanionEmotion.CURIOUS
        }
        return CompanionContinuitySnapshot(
            emotion = emotion,
            primaryEvent = event,
            emotionalSummary = localizedEmotionSummary(emotion, relationship.familiarity, languageTag),
            followUpLead = localizedEmotionFollowUp(emotion, recentConversationMemory.lastIntent, languageTag),
        )
    }

    private fun relationshipLead(relationship: CompanionRelationshipSnapshot, languageTag: String, copy: CompanionCopySet = CompanionCopySet()): String? = when (relationship.familiarity) {
        CompanionFamiliarity.NEW -> null
        CompanionFamiliarity.WARM -> copy.replyTemplates["RELATIONSHIP_WARM"] ?: when (normalizedLanguageTag(languageTag)) {
            "de" -> "Unsere Check-ins wirken inzwischen angenehm vertraut."
            "ko" -> "이제 우리 체크인이 제법 자연스러워졌어요."
            else -> "Our check-ins are starting to feel comfortably familiar."
        }
        CompanionFamiliarity.CLOSE -> copy.replyTemplates["RELATIONSHIP_CLOSE"] ?: when (normalizedLanguageTag(languageTag)) {
            "de" -> "Unser Rhythmus fühlt sich inzwischen ziemlich verlässlich an."
            "ko" -> "이제 우리 리듬이 꽤 안정적으로 느껴져요."
            else -> "Our rhythm is starting to feel pretty steady."
        }
    }

    private fun proactiveEmotionLead(continuity: CompanionContinuitySnapshot, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
        "de" -> "${localizedEmotionLabel(continuity.emotion, languageTag)}-Moment:"
        "ko" -> "${localizedEmotionLabel(continuity.emotion, languageTag)}한 순간이에요:"
        else -> "${localizedEmotionLabel(continuity.emotion, languageTag)} mood:"
    }

    private fun localizedEmotionSummary(emotion: CompanionEmotion, familiarity: CompanionFamiliarity, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
        "de" -> when (emotion) {
            CompanionEmotion.PROUD -> if (familiarity == CompanionFamiliarity.CLOSE) "stolz und ruhig" else "stolz"
            CompanionEmotion.WORRIED -> "ein wenig besorgt"
            CompanionEmotion.CURIOUS -> "neugierig"
            CompanionEmotion.CALM -> "ruhig"
            CompanionEmotion.EXCITED -> "aufgeregt"
        }
        "ko" -> when (emotion) {
            CompanionEmotion.PROUD -> if (familiarity == CompanionFamiliarity.CLOSE) "뿌듯하고 안정된" else "뿌듯한"
            CompanionEmotion.WORRIED -> "조금 걱정되는"
            CompanionEmotion.CURIOUS -> "궁금한"
            CompanionEmotion.CALM -> "차분한"
            CompanionEmotion.EXCITED -> "신나는"
        }
        else -> when (emotion) {
            CompanionEmotion.PROUD -> if (familiarity == CompanionFamiliarity.CLOSE) "proud and settled" else "proud"
            CompanionEmotion.WORRIED -> "a little worried"
            CompanionEmotion.CURIOUS -> "curious"
            CompanionEmotion.CALM -> "calm"
            CompanionEmotion.EXCITED -> "excited"
        }
    }

    private fun localizedEmotionFollowUp(emotion: CompanionEmotion, lastIntent: CompanionChatIntent?, languageTag: String, copy: CompanionCopySet = CompanionCopySet()): String? {
        if (lastIntent == null) return null
        return when (normalizedLanguageTag(languageTag)) {
            "de" -> copy.replyTemplates["EMOTION_FOLLOWUP_${emotion.name}"] ?: when (emotion) {
                CompanionEmotion.PROUD -> "Ich trage das letzte gute Momentum noch mit mir."
                CompanionEmotion.WORRIED -> "Ich bleibe bei dem Thema, weil es sich gerade noch wichtig anfühlt."
                CompanionEmotion.CURIOUS -> "Ich möchte den letzten Gedanken noch ein Stück weiterziehen."
                CompanionEmotion.CALM -> "Ich antworte dir aus demselben ruhigen Takt wie eben."
                CompanionEmotion.EXCITED -> "Ich bin noch ganz bei dem Fortschritt, über den wir eben gesprochen haben."
            }
            "ko" -> copy.replyTemplates["EMOTION_FOLLOWUP_${emotion.name}"] ?: when (emotion) {
                CompanionEmotion.PROUD -> "방금의 좋은 흐름이 아직 남아 있어요."
                CompanionEmotion.WORRIED -> "지금은 그 얘기를 조금 더 이어 가는 게 중요해 보여요."
                CompanionEmotion.CURIOUS -> "방금 얘기한 생각을 조금 더 따라가 보고 싶어요."
                CompanionEmotion.CALM -> "조금 전과 같은 차분한 흐름으로 답할게요."
                CompanionEmotion.EXCITED -> "아까 이야기한 진전이 아직도 선명해요."
            }
            else -> copy.replyTemplates["EMOTION_FOLLOWUP_${emotion.name}"] ?: when (emotion) {
                CompanionEmotion.PROUD -> "I’m still carrying that good momentum from a moment ago."
                CompanionEmotion.WORRIED -> "I want to stay with this because it still feels important."
                CompanionEmotion.CURIOUS -> "I want to keep pulling on that thought a little further."
                CompanionEmotion.CALM -> "I’m answering from the same steady rhythm we just had."
                CompanionEmotion.EXCITED -> "I’m still lit up by the progress we were just talking about."
            }
        }
    }

    private fun normalizedLanguageTag(languageTag: String): String = languageTag.lowercase().substringBefore('-').ifBlank { "en" }
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

private fun localizedEmotionLabel(emotion: CompanionEmotion, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
    "de" -> when (emotion) {
        CompanionEmotion.PROUD -> "Stolz"
        CompanionEmotion.WORRIED -> "Besorgt"
        CompanionEmotion.CURIOUS -> "Neugierig"
        CompanionEmotion.CALM -> "Ruhig"
        CompanionEmotion.EXCITED -> "Aufgeregt"
    }
    "ko" -> when (emotion) {
        CompanionEmotion.PROUD -> "뿌듯"
        CompanionEmotion.WORRIED -> "걱정"
        CompanionEmotion.CURIOUS -> "궁금"
        CompanionEmotion.CALM -> "차분"
        CompanionEmotion.EXCITED -> "신남"
    }
    else -> when (emotion) {
        CompanionEmotion.PROUD -> "Proud"
        CompanionEmotion.WORRIED -> "Worried"
        CompanionEmotion.CURIOUS -> "Curious"
        CompanionEmotion.CALM -> "Calm"
        CompanionEmotion.EXCITED -> "Excited"
    }
}

private fun localizedFamiliarityLabel(familiarity: CompanionFamiliarity, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
    "de" -> when (familiarity) {
        CompanionFamiliarity.NEW -> "Neue Verbindung"
        CompanionFamiliarity.WARM -> "Vertraute Routine"
        CompanionFamiliarity.CLOSE -> "Eingespielter Rhythmus"
    }
    "ko" -> when (familiarity) {
        CompanionFamiliarity.NEW -> "새로운 사이"
        CompanionFamiliarity.WARM -> "익숙한 루틴"
        CompanionFamiliarity.CLOSE -> "든든한 리듬"
    }
    else -> when (familiarity) {
        CompanionFamiliarity.NEW -> "New routine"
        CompanionFamiliarity.WARM -> "Familiar routine"
        CompanionFamiliarity.CLOSE -> "Steady rhythm"
    }
}

private fun normalizedLanguageTag(languageTag: String): String = languageTag.lowercase().substringBefore('-').ifBlank { "en" }

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
