package com.blue236.greenbuddy.model

data class Lesson(
    val id: String,
    val title: String,
    val summary: String,
    val concept: String,
    val quizPrompt: String,
    val quizOptions: List<String>,
    val correctAnswerIndex: Int,
    val rewardXp: Int,
)

data class LessonProgress(
    val currentLessonIndex: Int = 0,
    val completedLessonIds: Set<String> = emptySet(),
    val totalXp: Int = 0,
) {
    val completedCount: Int = completedLessonIds.size
}
