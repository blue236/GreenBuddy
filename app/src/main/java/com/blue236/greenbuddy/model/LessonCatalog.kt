package com.blue236.greenbuddy.model

object LessonCatalog {
    fun forSpecies(species: String): List<Lesson> = when (species) {
        "Monstera" -> monsteraLessons
        "Basil" -> basilLessons
        else -> tomatoLessons
    }

    private val monsteraLessons = listOf(
        Lesson(
            id = "monstera_light",
            title = "Window light without scorch",
            summary = "Set up your Monstera near bright filtered light so new leaves can size up without burning.",
            concept = "Monsteras grow best with bright indirect light. Gentle morning sun can work, but harsh afternoon rays often scorch leaves.",
            keyTakeaway = "Reward: Leaf Finder badge — build a calm indoor light routine.",
            quiz = LessonQuiz(
                type = QuizType.MULTIPLE_CHOICE,
                prompt = "What light setup is best for a Monstera starter in most homes?",
                options = listOf("Bright indirect light near a window", "Harsh balcony sun all day", "A dim corner far from windows"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 20,
            rewardLabel = "Leaf Finder badge",
        ),
        Lesson(
            id = "monstera_rotation",
            title = "Rotate for balanced growth",
            summary = "A quick weekly turn keeps your Monstera from leaning hard toward one side of the room.",
            concept = "Indoor plants chase the strongest light source. Rotating the pot helps the stem and leaves develop more evenly.",
            keyTakeaway = "Reward: Balanced Canopy badge — train a fuller indoor silhouette.",
            quiz = LessonQuiz(
                type = QuizType.TRUE_FALSE,
                prompt = "True or false: Rotating a Monstera every week or two can help it grow more evenly.",
                options = listOf("True", "False"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 25,
            rewardLabel = "Balanced Canopy badge",
        ),
        Lesson(
            id = "monstera_watering",
            title = "Water after the top dries",
            summary = "Monsteras prefer a soak-then-rest rhythm instead of staying wet all week.",
            concept = "Let the top layer of soil dry a bit before watering again. Constantly soggy soil raises the risk of root stress and yellowing.",
            keyTakeaway = "Reward: Root Watch badge — protect roots from overwatering.",
            quiz = LessonQuiz(
                type = QuizType.SCENARIO_CHOICE,
                prompt = "Your Monstera soil is still damp two centimeters down, but it is watering day on your calendar. What should you do?",
                options = listOf("Wait a bit longer and check again before watering", "Water now because the schedule matters most", "Add extra fertilizer instead of checking moisture"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 25,
            rewardLabel = "Root Watch badge",
        ),
        Lesson(
            id = "monstera_support",
            title = "Give climbing leaves support",
            summary = "A stake or moss pole helps mature Monstera growth feel purposeful instead of floppy.",
            concept = "Monsteras are natural climbers. Support can encourage larger leaves and a tidier growth habit as the plant matures.",
            keyTakeaway = "Reward: Climber Coach badge — guide a stronger growth habit.",
            quiz = LessonQuiz(
                type = QuizType.MULTIPLE_CHOICE,
                prompt = "Why add support to a growing Monstera?",
                options = listOf("To encourage natural climbing growth", "To keep the soil permanently wet", "To replace the need for light"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 30,
            rewardLabel = "Climber Coach badge",
        ),
    )

    private val basilLessons = listOf(
        Lesson(
            id = "basil_sun",
            title = "Chase kitchen-window sun",
            summary = "Basil is happiest with strong daily light and steady moisture, especially on a bright sill.",
            concept = "Basil grows fast when it gets several hours of sun. Weak light quickly leads to pale, stretched stems.",
            keyTakeaway = "Reward: Sun Sipper badge — set up a productive herb corner.",
            quiz = LessonQuiz(
                type = QuizType.MULTIPLE_CHOICE,
                prompt = "What does basil want most from its starter setup?",
                options = listOf("Strong light and regular moisture", "Total shade and dry soil", "Cold drafts and infrequent watering"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 20,
            rewardLabel = "Sun Sipper badge",
        ),
        Lesson(
            id = "basil_harvest",
            title = "Pinch above leaf pairs",
            summary = "Small, frequent harvests make basil bushier and keep it from becoming a tall bare stick.",
            concept = "Pinching top growth above a leaf pair encourages side shoots. That means more leaves to harvest and a fuller plant.",
            keyTakeaway = "Reward: Bushy Builder badge — shape better kitchen harvests.",
            quiz = LessonQuiz(
                type = QuizType.TRUE_FALSE,
                prompt = "True or false: Regularly pinching basil tips can encourage bushier growth.",
                options = listOf("True", "False"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 25,
            rewardLabel = "Bushy Builder badge",
        ),
        Lesson(
            id = "basil_flower",
            title = "Catch flowers early",
            summary = "Once basil focuses on flowers, leaf quality and flavor often drop off.",
            concept = "Flowering shifts the plant toward reproduction. Removing flower buds early helps keep growth leafy for longer.",
            keyTakeaway = "Reward: Flavor Keeper badge — extend the tasty stage.",
            quiz = LessonQuiz(
                type = QuizType.SCENARIO_CHOICE,
                prompt = "You notice tiny flower buds forming on your basil, but you want more leaves for cooking. What is the best move?",
                options = listOf("Pinch off the buds and keep harvesting", "Move it into deeper shade", "Stop watering for a week"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 25,
            rewardLabel = "Flavor Keeper badge",
        ),
        Lesson(
            id = "basil_moisture",
            title = "Keep the soil evenly moist",
            summary = "Basil dislikes the dramatic swing between bone-dry and soaked roots.",
            concept = "Aim for lightly moist soil most of the time. Consistency helps basil stay tender and less stressed.",
            keyTakeaway = "Reward: Harvest Rhythm badge — build a dependable care loop.",
            quiz = LessonQuiz(
                type = QuizType.MULTIPLE_CHOICE,
                prompt = "Which watering rhythm suits basil best?",
                options = listOf("Consistent, lightly moist soil", "Let it fully dry for many days", "Flood it daily until water pools"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 30,
            rewardLabel = "Harvest Rhythm badge",
        ),
    )

    private val tomatoLessons = listOf(
        Lesson(
            id = "tomato_support",
            title = "Support fruiting stems early",
            summary = "Tomatoes grow fast and benefit from cages or stakes before they get heavy.",
            concept = "Early support prevents stem damage, improves airflow, and makes later fruit load easier to manage.",
            keyTakeaway = "Reward: Stem Saver badge — prepare for bigger growth.",
            quiz = LessonQuiz(
                type = QuizType.MULTIPLE_CHOICE,
                prompt = "What should you prioritize for a tomato starter?",
                options = listOf("Support and strong sun", "Deep shade and dry soil", "No airflow around the plant"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 20,
            rewardLabel = "Stem Saver badge",
        ),
        Lesson(
            id = "tomato_deep_water",
            title = "Water deeply, not constantly",
            summary = "Tomatoes do better with thorough watering sessions than with tiny splashes all day.",
            concept = "Deep watering encourages stronger root systems, while shallow frequent splashes often leave roots near the surface.",
            keyTakeaway = "Reward: Root Runner badge — train deeper roots.",
            quiz = LessonQuiz(
                type = QuizType.TRUE_FALSE,
                prompt = "True or false: Deep, consistent watering is usually better for tomatoes than frequent tiny splashes.",
                options = listOf("True", "False"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 25,
            rewardLabel = "Root Runner badge",
        ),
        Lesson(
            id = "tomato_feeding",
            title = "Feed the flowering plant",
            summary = "Tomatoes burn a lot of energy once they start pushing flowers and fruit.",
            concept = "Balanced feeding supports blooms, fruit set, and stronger stems. It does not replace sunlight or watering.",
            keyTakeaway = "Reward: Bloom Booster badge — support the fruiting phase.",
            quiz = LessonQuiz(
                type = QuizType.SCENARIO_CHOICE,
                prompt = "Your tomato has flowers forming, pale older leaves, and strong sun exposure. What care change helps most?",
                options = listOf("Start a consistent feeding routine", "Move it into shade", "Stop watering completely"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 25,
            rewardLabel = "Bloom Booster badge",
        ),
        Lesson(
            id = "tomato_airflow",
            title = "Leave room for airflow",
            summary = "Crowded foliage traps humidity and makes tomato care harder as the plant thickens up.",
            concept = "Good spacing and airflow help reduce disease pressure and keep leaves drying faster after watering.",
            keyTakeaway = "Reward: Garden Coach badge — manage a more resilient plant.",
            quiz = LessonQuiz(
                type = QuizType.MULTIPLE_CHOICE,
                prompt = "Why does airflow matter around tomatoes?",
                options = listOf("It helps reduce moisture-related disease pressure", "It replaces fertilizer", "It makes sunlight unnecessary"),
                correctAnswerIndex = 0,
            ),
            rewardXp = 30,
            rewardLabel = "Garden Coach badge",
        ),
    )
}
