package com.blue236.greenbuddy.model

object LessonCatalog {
    fun forSpecies(species: String): List<Lesson> = when (species) {
        "Monstera" -> listOf(
            Lesson(
                id = "monstera_light",
                title = "Indirect light basics",
                summary = "Help your Monstera find bright light without harsh afternoon sun.",
                concept = "Monsteras prefer bright indirect light rather than strong direct afternoon sun.",
                quizPrompt = "What light setup is best for a Monstera starter?",
                quizOptions = listOf("Harsh balcony sun all day", "Bright indirect light", "Very low light"),
                correctAnswerIndex = 1,
                rewardXp = 20,
            ),
            Lesson(
                id = "monstera_watering",
                title = "Water when the top dries",
                summary = "Keep roots happy by avoiding constantly soggy soil.",
                concept = "Let the top layer of soil dry a bit before watering again to reduce overwatering risk.",
                quizPrompt = "When should you water a Monstera again?",
                quizOptions = listOf("When the top soil starts drying", "Every few hours", "Only once a month"),
                correctAnswerIndex = 0,
                rewardXp = 25,
            ),
        )
        "Basil" -> listOf(
            Lesson(
                id = "basil_sun",
                title = "Sun + water balance",
                summary = "Basil grows fast with steady moisture and lots of light.",
                concept = "Basil grows best with steady moisture and plenty of sun, especially near a bright window.",
                quizPrompt = "What does basil want most in its starter setup?",
                quizOptions = listOf("Sun + regular pinching", "Total shade", "Water once a month"),
                correctAnswerIndex = 0,
                rewardXp = 20,
            ),
            Lesson(
                id = "basil_harvest",
                title = "Pinch to branch out",
                summary = "Frequent pinching helps basil stay bushy instead of leggy.",
                concept = "Pinching top growth encourages side shoots and gives you a healthier, fuller herb plant.",
                quizPrompt = "Why pinch basil tops?",
                quizOptions = listOf("To stop all growth", "To encourage bushier growth", "To dry the soil faster"),
                correctAnswerIndex = 1,
                rewardXp = 25,
            ),
        )
        else -> listOf(
            Lesson(
                id = "tomato_support",
                title = "Supporting fruiting plants",
                summary = "Tomatoes need strong light and early support for healthy growth.",
                concept = "Tomatoes need lots of direct sun and consistent feeding to support flowers and fruit.",
                quizPrompt = "What should you prioritize for a tomato starter?",
                quizOptions = listOf("Deep shade", "Support + full sun", "No watering"),
                correctAnswerIndex = 1,
                rewardXp = 20,
            ),
            Lesson(
                id = "tomato_feeding",
                title = "Feed for flowers and fruit",
                summary = "As tomatoes mature, balanced nutrition helps them hold blooms and fruit.",
                concept = "A fruiting plant burns more energy, so steady feeding helps sustain flowers, fruit set, and stronger stems.",
                quizPrompt = "Why feed a growing tomato consistently?",
                quizOptions = listOf("To support flowers and fruit", "To replace sunlight", "To avoid all watering"),
                correctAnswerIndex = 0,
                rewardXp = 25,
            ),
        )
    }
}
