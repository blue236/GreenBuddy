package com.blue236.greenbuddy.model

enum class QuizType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SCENARIO_CHOICE,
}

data class LessonQuiz(
    val type: QuizType,
    val prompt: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
)

data class Lesson(
    val id: String,
    val title: String,
    val summary: String,
    val concept: String,
    val keyTakeaway: String,
    val quiz: LessonQuiz,
    val rewardXp: Int,
    val rewardLabel: String,
)

data class LessonProgress(
    val currentLessonIndex: Int = 0,
    val completedLessonIds: Set<String> = emptySet(),
    val totalXp: Int = 0,
) {
    val completedCount: Int = completedLessonIds.size
}
