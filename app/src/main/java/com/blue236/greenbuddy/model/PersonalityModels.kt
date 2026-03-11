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
    fun personalityFor(species: String): CompanionPersonality = when (species) {
        "Monstera" -> CompanionPersonality(
            archetype = "Calm explorer",
            tone = "Warm, observant, and gently curious.",
            homeHeadline = "Rainforest check-in",
            profileLabel = "Easygoing guide",
        )
        "Basil" -> CompanionPersonality(
            archetype = "Peppy kitchen sprinter",
            tone = "Bright, chatty, and always ready for the next tiny win.",
            homeHeadline = "Fresh counter pep talk",
            profileLabel = "Sunny motivator",
        )
        "Tomato" -> CompanionPersonality(
            archetype = "Driven garden captain",
            tone = "Bold, encouraging, and a little dramatic about progress.",
            homeHeadline = "Greenhouse briefing",
            profileLabel = "Ambitious challenger",
        )
        else -> CompanionPersonality(
            archetype = "Gentle sprout",
            tone = "Friendly and supportive.",
            homeHeadline = "Daily check-in",
            profileLabel = "Plant pal",
        )
    }

    fun dialogueFor(
        starter: StarterPlantOption,
        careState: PlantCareState,
        progress: LessonProgress,
        lessons: List<Lesson>,
    ): CompanionDialogue {
        val personality = personalityFor(starter.companion.species)
        val mood = moodContextFor(careState, progress, lessons)
        val allLessonsComplete = progress.isComplete(lessons)
        val currentLesson = progress.currentLessonOrNull(lessons)

        val line = when (starter.companion.species) {
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
            "Tomato" -> when (mood) {
                CompanionMoodContext.NEEDY -> "I’ve got big plans, but I need support before I can push higher."
                CompanionMoodContext.GROWING -> "Strong footing, solid light, forward momentum. That’s how winners grow."
                CompanionMoodContext.READY_TO_LEARN -> "Let’s take the next lesson seriously. Great harvests start with sharp habits."
                CompanionMoodContext.PROUD -> "This is excellent work. We’re growing with purpose now."
            }
            else -> starter.companion.greeting
        }

        val careGuidance = when (starter.companion.species) {
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
            "Tomato" -> when (careState.lowestNeed) {
                CareAction.WATER -> "Keep the water coming and I’ll stay strong under pressure."
                CareAction.MOVE_TO_SUNLIGHT -> "I need stronger light if we’re aiming for the good stuff later."
                CareAction.FERTILIZE -> "Fuel me properly and I’ll turn effort into serious growth."
            }
            else -> starter.companion.careTip
        }

        val lessonNudge = if (allLessonsComplete) {
            when (starter.companion.species) {
                "Monstera" -> "Starter lessons complete. We can coast for a bit or try a new plant path."
                "Basil" -> "Starter track cleared. Want to start another grow-fast run in PlantDex?"
                "Tomato" -> "Starter track complete. If you want a new challenge, pick another companion and we go again."
                else -> "Starter track complete."
            }
        } else {
            when (starter.companion.species) {
                "Monstera" -> currentLesson?.title?.let { "Next gentle step: $it." } ?: "A calm lesson would suit us right now."
                "Basil" -> currentLesson?.title?.let { "Next up: $it — let’s keep the pace up." } ?: "I’m ready for the next bite-sized lesson."
                "Tomato" -> currentLesson?.title?.let { "Next objective: $it." } ?: "Let’s line up the next lesson and keep building."
                else -> currentLesson?.title?.let { "Next lesson: $it." } ?: "Your next lesson is ready."
            }
        }

        return CompanionDialogue(
            headline = personality.homeHeadline,
            line = line,
            careGuidance = careGuidance,
            lessonNudge = lessonNudge,
        )
    }

    fun reminderCopy(
        type: ReminderType,
        starter: StarterPlantOption,
        careState: PlantCareState,
        lessonTitle: String?,
    ): ReminderNotification = when (type) {
        ReminderType.STREAK_WARNING -> when (starter.companion.species) {
            "Monstera" -> ReminderNotification(type, "Keep your GreenBuddy rhythm alive", "${starter.companion.name} is waiting quietly. Drop in today and keep your calm little routine growing.")
            "Basil" -> ReminderNotification(type, "${starter.companion.name} wants a quick comeback", "Pop back in today — ${starter.companion.name} thrives when you keep the momentum fresh.")
            "Tomato" -> ReminderNotification(type, "${starter.companion.name} is counting on you", "Today still counts. Check in with ${starter.companion.name} and keep this growth streak from slipping.")
            else -> ReminderNotification(type, "Keep your GreenBuddy routine alive", "${starter.companion.name} has missed you. Drop in today so the habit doesn’t go cold.")
        }
        ReminderType.CARE -> when (starter.companion.species) {
            "Monstera" -> ReminderNotification(type, "${starter.companion.name} needs a soft reset", reminderCareMessage(starter, careState))
            "Basil" -> ReminderNotification(type, "${starter.companion.name} could use a quick boost", reminderCareMessage(starter, careState))
            "Tomato" -> ReminderNotification(type, "${starter.companion.name} needs support to stay on track", reminderCareMessage(starter, careState))
            else -> ReminderNotification(type, "${starter.companion.name} needs a quick care check", reminderCareMessage(starter, careState))
        }
        ReminderType.LESSON_READY -> when (starter.companion.species) {
            "Monstera" -> ReminderNotification(type, "A calm GreenBuddy lesson is ready", lessonTitle?.let { "'$it' is ready whenever you want a quiet plant-care minute." } ?: "Your next GreenBuddy lesson is ready whenever you want a quiet plant-care minute.")
            "Basil" -> ReminderNotification(type, "${starter.companion.name} is ready for the next quick lesson", lessonTitle?.let { "'$it' is ready — short, useful, and perfect for keeping the pace up." } ?: "Your next GreenBuddy lesson is ready — short, useful, and easy to jump into.")
            "Tomato" -> ReminderNotification(type, "Your next growth lesson is queued", lessonTitle?.let { "'$it' is ready. One solid check-in now keeps the whole plan moving." } ?: "Your next GreenBuddy lesson is ready. One solid check-in keeps the plan moving.")
            else -> ReminderNotification(type, "A GreenBuddy lesson is ready", lessonTitle?.let { "Your next lesson, '$it', is waiting whenever you have a minute." } ?: "Your next GreenBuddy lesson is ready whenever you have a minute.")
        }
    }

    private fun reminderCareMessage(starter: StarterPlantOption, careState: PlantCareState): String = when (starter.companion.species) {
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
        "Tomato" -> when (careState.lowestNeed) {
            CareAction.WATER -> "Water is running low. Keep me topped up so I can stay strong."
            CareAction.MOVE_TO_SUNLIGHT -> "Light is slipping. I need stronger sun to keep building momentum."
            CareAction.FERTILIZE -> "Nutrition is low. A proper feed would keep this grow plan on course."
        }
        else -> starter.companion.careTip
    }

    private fun moodContextFor(
        careState: PlantCareState,
        progress: LessonProgress,
        lessons: List<Lesson>,
    ): CompanionMoodContext = when {
        careState.lowestStat <= 40 -> CompanionMoodContext.NEEDY
        progress.isComplete(lessons) -> CompanionMoodContext.PROUD
        careState.averageScore >= 75 -> CompanionMoodContext.GROWING
        else -> CompanionMoodContext.READY_TO_LEARN
    }
}
