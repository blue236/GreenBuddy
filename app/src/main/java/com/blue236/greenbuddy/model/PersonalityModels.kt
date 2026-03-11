package com.blue236.greenbuddy.model

enum class CompanionMoodContext {
    NEEDY,
    GROWING,
    READY_TO_LEARN,
    PROUD,
}

data class CompanionPersonality(
    val archetype: String,
    val tone: String,
    val homeHeadline: String,
    val profileLabel: String,
)

data class CompanionDialogue(
    val headline: String,
    val line: String,
    val careGuidance: String,
    val lessonNudge: String,
)

object CompanionPersonalitySystem {
    fun personalityFor(species: String, languageTag: String = "en"): CompanionPersonality {
        val lang = normalizedLanguageTag(languageTag)
        return when (species) {
            "Monstera" -> when (lang) {
                "de" -> CompanionPersonality("Ruhiger Entdecker", "Warm, aufmerksam und sanft neugierig.", "Regenwald-Check-in", "Gelassener Guide")
                "ko" -> CompanionPersonality("차분한 탐험가", "따뜻하고 세심하며 조용히 호기심이 많아요.", "열대우림 체크인", "느긋한 가이드")
                else -> CompanionPersonality("Calm explorer", "Warm, observant, and gently curious.", "Rainforest check-in", "Easygoing guide")
            }
            "Basil" -> when (lang) {
                "de" -> CompanionPersonality("Flotter Küchen-Sprinter", "Hell, gesprächig und immer bereit für den nächsten kleinen Erfolg.", "Frischer Küchen-Pep-Talk", "Sonniger Motivator")
                "ko" -> CompanionPersonality("경쾌한 주방 스프린터", "밝고 말이 많고 작은 성공에도 바로 신나요.", "상쾌한 주방 응원", "햇살 같은 응원가")
                else -> CompanionPersonality("Peppy kitchen sprinter", "Bright, chatty, and always ready for the next tiny win.", "Fresh counter pep talk", "Sunny motivator")
            }
            "Tomato" -> when (lang) {
                "de" -> CompanionPersonality("Zielstrebiger Gartenkapitän", "Mutig, ermutigend und beim Fortschritt leicht dramatisch.", "Gewächshaus-Briefing", "Ehrgeiziger Herausforderer")
                "ko" -> CompanionPersonality("주도적인 가든 캡틴", "대담하고 격려를 잘하며 성장에는 살짝 드라마틱해요.", "온실 브리핑", "야심찬 도전자")
                else -> CompanionPersonality("Driven garden captain", "Bold, encouraging, and a little dramatic about progress.", "Greenhouse briefing", "Ambitious challenger")
            }
            else -> when (lang) {
                "de" -> CompanionPersonality("Sanfter Sprössling", "Freundlich und unterstützend.", "Täglicher Check-in", "Pflanzenfreund")
                "ko" -> CompanionPersonality("다정한 새싹", "친절하고 든든하게 응원해 줘요.", "오늘의 체크인", "식물 친구")
                else -> CompanionPersonality("Gentle sprout", "Friendly and supportive.", "Daily check-in", "Plant pal")
            }
        }
    }

    fun dialogueFor(starter: StarterPlantOption, careState: PlantCareState, progress: LessonProgress, lessons: List<Lesson>, languageTag: String = "en"): CompanionDialogue {
        val lang = normalizedLanguageTag(languageTag)
        val personality = personalityFor(starter.companion.species, lang)
        val mood = moodContextFor(careState, progress, lessons)
        val allLessonsComplete = progress.isComplete(lessons)
        val currentLesson = progress.currentLessonOrNull(lessons)

        val line = when (lang) {
            "de" -> when (starter.id) {
                "monstera" -> when (mood) {
                    CompanionMoodContext.NEEDY -> "Ich hänge etwas durch. Ein gleichmäßigerer Rhythmus würde mir helfen."
                    CompanionMoodContext.GROWING -> "Gute Balance. Ich glaube, ein neues Blatt ist schon in Planung."
                    CompanionMoodContext.READY_TO_LEARN -> "Eine kurze Lektion würde unseren kleinen Dschungel gut voranbringen."
                    CompanionMoodContext.PROUD -> "Wir haben zusammen einen ruhigen Rhythmus aufgebaut."
                }
                "basil" -> when (mood) {
                    CompanionMoodContext.NEEDY -> "Kleine Küchen-Notlage: Ich könnte gerade einen schnellen Boost gebrauchen."
                    CompanionMoodContext.GROWING -> "Ja! Genau das brauche ich. Ich bin fit und bereit für mehr."
                    CompanionMoodContext.READY_TO_LEARN -> "Gib mir die nächste Lektion — ich bin in Wachstumslaune."
                    CompanionMoodContext.PROUD -> "Schau uns an. Diese Fensterbank läuft richtig gut."
                }
                else -> when (mood) {
                    CompanionMoodContext.NEEDY -> "Ich habe große Pläne, brauche aber erst wieder etwas Unterstützung."
                    CompanionMoodContext.GROWING -> "Starker Stand, gutes Licht, Vorwärtsdrang. So wächst man richtig."
                    CompanionMoodContext.READY_TO_LEARN -> "Nimm die nächste Lektion mit mir ernst. Gute Ernten starten mit guten Gewohnheiten."
                    CompanionMoodContext.PROUD -> "Das ist starke Arbeit. Wir wachsen jetzt mit Absicht."
                }
            }
            "ko" -> when (starter.id) {
                "monstera" -> when (mood) {
                    CompanionMoodContext.NEEDY -> "조금 축 처졌어요. 더 꾸준한 루틴이 있으면 다시 힘을 낼 수 있어요."
                    CompanionMoodContext.GROWING -> "균형이 좋아요. 새잎이 벌써 준비되는 느낌이에요."
                    CompanionMoodContext.READY_TO_LEARN -> "짧은 레슨 하나면 우리 작은 정글이 더 앞으로 나아갈 수 있어요."
                    CompanionMoodContext.PROUD -> "우리 둘만의 차분한 리듬이 생겼어요."
                }
                "basil" -> when (mood) {
                    CompanionMoodContext.NEEDY -> "작은 주방 비상상황이에요. 지금은 빠른 부스트가 필요해요."
                    CompanionMoodContext.GROWING -> "좋아요! 바로 이런 느낌이에요. 더 할 준비 됐어요."
                    CompanionMoodContext.READY_TO_LEARN -> "다음 레슨 주세요 — 지금은 쑥쑥 배우고 자랄 기분이에요."
                    CompanionMoodContext.PROUD -> "우리 정말 잘하고 있어요. 이 창가가 성공 루틴이 됐네요."
                }
                else -> when (mood) {
                    CompanionMoodContext.NEEDY -> "크게 자랄 계획은 많지만, 지금은 조금 더 받쳐 줄 도움이 필요해요."
                    CompanionMoodContext.GROWING -> "기반도 좋고 빛도 좋고 흐름도 좋아요. 이렇게 자라는 거예요."
                    CompanionMoodContext.READY_TO_LEARN -> "다음 레슨도 진지하게 가 봐요. 좋은 수확은 좋은 습관에서 시작돼요."
                    CompanionMoodContext.PROUD -> "아주 훌륭해요. 이제 우리는 목적 있게 자라고 있어요."
                }
            }
            else -> when (starter.companion.species) {
                "Monstera" -> when (mood) {
                    CompanionMoodContext.NEEDY -> "I’m drooping a little. A steadier routine would help me open up again."
                    CompanionMoodContext.GROWING -> "Nice balance. I can almost feel a new leaf planning itself."
                    CompanionMoodContext.READY_TO_LEARN -> "A quick lesson now would keep our little jungle moving forward."
                    CompanionMoodContext.PROUD -> "We’ve built a calm rhythm together. I look genuinely lush."
                }
                "Basil" -> when (mood) {
                    CompanionMoodContext.NEEDY -> "Okay, tiny kitchen emergency: I could really use a quick boost right now."
                    CompanionMoodContext.GROWING -> "Yes! That’s the good stuff. I’m perky and ready for more."
                    CompanionMoodContext.READY_TO_LEARN -> "Hit me with the next lesson — I’m in a fast-growing mood."
                    CompanionMoodContext.PROUD -> "Look at us go. We’ve turned this counter into a success story."
                }
                else -> when (mood) {
                    CompanionMoodContext.NEEDY -> "I’ve got big plans, but I need support before I can push higher."
                    CompanionMoodContext.GROWING -> "Strong footing, solid light, forward momentum. That’s how winners grow."
                    CompanionMoodContext.READY_TO_LEARN -> "Let’s take the next lesson seriously. Great harvests start with sharp habits."
                    CompanionMoodContext.PROUD -> "This is excellent work. We’re growing with purpose now."
                }
            }
        }

        val careGuidance = when (lang) {
            "de" -> when (careState.lowestNeed) {
                CareAction.WATER -> "Etwas Wasser würde jetzt spürbar helfen."
                CareAction.MOVE_TO_SUNLIGHT -> "Etwas mehr Licht würde sofort guttun."
                CareAction.FERTILIZE -> "Ein kleiner Nährstoffschub würde mein Wachstum stützen."
            }
            "ko" -> when (careState.lowestNeed) {
                CareAction.WATER -> "지금은 물 한 번 주면 큰 도움이 될 거예요."
                CareAction.MOVE_TO_SUNLIGHT -> "빛을 조금 더 받으면 바로 좋아질 거예요."
                CareAction.FERTILIZE -> "영양을 조금 보충해 주면 성장에 도움이 돼요."
            }
            else -> when (starter.companion.species) {
                "Monstera" -> when (careState.lowestNeed) {
                    CareAction.WATER -> "A deep drink would help me settle back into that rainforest groove."
                    CareAction.MOVE_TO_SUNLIGHT -> "A touch more light and I’ll angle those leaves with confidence."
                    CareAction.FERTILIZE -> "A little feed now would give my next growth spurt something to work with."
                }
                "Basil" -> when (careState.lowestNeed) {
                    CareAction.WATER -> "Quick sip, please — I bounce back fast when hydration is on point."
                    CareAction.MOVE_TO_SUNLIGHT -> "More sun, more flavor, more energy. Easy win."
                    CareAction.FERTILIZE -> "A nutrient top-up would keep me lush instead of leggy."
                }
                else -> when (careState.lowestNeed) {
                    CareAction.WATER -> "Keep the water coming and I’ll stay strong under pressure."
                    CareAction.MOVE_TO_SUNLIGHT -> "I need stronger light if we’re aiming for the good stuff later."
                    CareAction.FERTILIZE -> "Fuel me properly and I’ll turn effort into serious growth."
                }
            }
        }

        val lessonNudge = if (allLessonsComplete) {
            when (lang) {
                "de" -> "Starter-Lektionen abgeschlossen. Du kannst kurz durchschnaufen oder eine neue Pflanze wählen."
                "ko" -> "스타터 레슨을 모두 끝냈어요. 잠시 쉬거나 새로운 식물을 골라 볼 수 있어요."
                else -> "Starter track complete."
            }
        } else {
            when (lang) {
                "de" -> currentLesson?.title?.let { "Nächster Schritt: $it." } ?: "Deine nächste Lektion ist bereit."
                "ko" -> currentLesson?.title?.let { "다음 단계: $it" } ?: "다음 레슨이 준비됐어요."
                else -> currentLesson?.title?.let { "Next lesson: $it." } ?: "Your next lesson is ready."
            }
        }

        return CompanionDialogue(personality.homeHeadline, line, careGuidance, lessonNudge)
    }

    fun reminderCopy(type: ReminderType, starter: StarterPlantOption, careState: PlantCareState, lessonTitle: String?, languageTag: String = "en"): ReminderNotification {
        val lang = normalizedLanguageTag(languageTag)
        return when (type) {
            ReminderType.STREAK_WARNING -> when (lang) {
                "de" -> ReminderNotification(type, "Halte deinen GreenBuddy-Rhythmus am Leben", "${starter.companion.name} wartet schon. Schau heute kurz vorbei, damit die Routine nicht abreißt.")
                "ko" -> ReminderNotification(type, "GreenBuddy 루틴을 이어 가요", "${starter.companion.name}가 기다리고 있어요. 오늘 잠깐 들러서 흐름을 이어 주세요.")
                else -> ReminderNotification(type, "Keep your GreenBuddy rhythm alive", "${starter.companion.name} has missed you. Drop in today so the habit doesn’t go cold.")
            }
            ReminderType.CARE -> when (lang) {
                "de" -> ReminderNotification(type, "${starter.companion.name} braucht eine kleine Pflege", reminderCareMessage(starter, careState, lang))
                "ko" -> ReminderNotification(type, "${starter.companion.name}에게 돌봄이 필요해요", reminderCareMessage(starter, careState, lang))
                else -> ReminderNotification(type, "${starter.companion.name} needs a quick care check", reminderCareMessage(starter, careState, lang))
            }
            ReminderType.LESSON_READY -> when (lang) {
                "de" -> ReminderNotification(type, "Eine GreenBuddy-Lektion ist bereit", lessonTitle?.let { "'$it' ist bereit, sobald du eine Minute hast." } ?: "Deine nächste GreenBuddy-Lektion ist bereit, sobald du eine Minute hast.")
                "ko" -> ReminderNotification(type, "GreenBuddy 레슨이 준비됐어요", lessonTitle?.let { "'$it' 레슨이 준비됐어요. 잠깐 시간 날 때 시작해 보세요." } ?: "다음 GreenBuddy 레슨이 준비됐어요. 잠깐 시간 날 때 시작해 보세요.")
                else -> when (starter.companion.species) {
                    "Monstera" -> ReminderNotification(type, "A calm GreenBuddy lesson is ready", lessonTitle?.let { "'$it' is ready whenever you want a quiet plant-care minute." } ?: "Your next GreenBuddy lesson is ready whenever you want a quiet plant-care minute.")
                    "Basil" -> ReminderNotification(type, "${starter.companion.name} is ready for the next quick lesson", lessonTitle?.let { "'$it' is ready — short, useful, and perfect for keeping the pace up." } ?: "Your next GreenBuddy lesson is ready — short, useful, and easy to jump into.")
                    "Tomato" -> ReminderNotification(type, "Your next growth lesson is queued", lessonTitle?.let { "'$it' is ready. One solid check-in now keeps the whole plan moving." } ?: "Your next GreenBuddy lesson is ready. One solid check-in keeps the plan moving.")
                    else -> ReminderNotification(type, "A GreenBuddy lesson is ready", lessonTitle?.let { "Your next lesson, '$it', is waiting whenever you have a minute." } ?: "Your next GreenBuddy lesson is ready whenever you have a minute.")
                }
            }
        }
    }

    private fun reminderCareMessage(starter: StarterPlantOption, careState: PlantCareState, languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
        "de" -> when (careState.lowestNeed) {
            CareAction.WATER -> "Der Wasserstand sinkt. Eine kurze Gießkontrolle würde helfen."
            CareAction.MOVE_TO_SUNLIGHT -> "Das Licht wird schwächer. Ein hellerer Platz würde guttun."
            CareAction.FERTILIZE -> "Die Nährstoffe werden knapp. Eine kleine Düngung würde helfen."
        }
        "ko" -> when (careState.lowestNeed) {
            CareAction.WATER -> "수분이 내려가고 있어요. 가볍게 물 상태를 확인해 주세요."
            CareAction.MOVE_TO_SUNLIGHT -> "빛이 부족해지고 있어요. 더 밝은 곳이 좋아요."
            CareAction.FERTILIZE -> "영양이 줄고 있어요. 가볍게 영양을 보충해 주세요."
        }
        else -> when (starter.companion.species) {
            "Monstera" -> when (careState.lowestNeed) {
                CareAction.WATER -> "Hydration is dipping. A slow, steady watering check would help a lot."
                CareAction.MOVE_TO_SUNLIGHT -> "Light is getting low. A brighter corner would perk those leaves right up."
                CareAction.FERTILIZE -> "Nutrition is thinning out. A little feed would help support fresh growth."
            }
            "Basil" -> when (careState.lowestNeed) {
                CareAction.WATER -> "I’m drying out fast. A quick watering check would get me lively again."
                CareAction.MOVE_TO_SUNLIGHT -> "I want more sun. A brighter spot would wake me right up."
                CareAction.FERTILIZE -> "Nutrients are fading. A small boost would keep me full and leafy."
            }
            else -> when (careState.lowestNeed) {
                CareAction.WATER -> "Water is running low. Keep me topped up so I can stay strong."
                CareAction.MOVE_TO_SUNLIGHT -> "Light is slipping. I need stronger sun to keep building momentum."
                CareAction.FERTILIZE -> "Nutrition is low. A proper feed would keep this grow plan on course."
            }
        }
    }

    private fun moodContextFor(careState: PlantCareState, progress: LessonProgress, lessons: List<Lesson>): CompanionMoodContext = when {
        careState.lowestStat <= 40 -> CompanionMoodContext.NEEDY
        progress.isComplete(lessons) -> CompanionMoodContext.PROUD
        careState.averageScore >= 75 -> CompanionMoodContext.GROWING
        else -> CompanionMoodContext.READY_TO_LEARN
    }
}
