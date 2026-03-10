package com.blue236.greenbuddy.model

fun LessonProgress.normalizedFor(lessons: List<Lesson>): LessonProgress {
    if (lessons.isEmpty()) return copy(currentLessonIndex = 0)

    val maxIndex = if (isComplete(lessons)) lessons.size else lessons.lastIndex
    return copy(currentLessonIndex = currentLessonIndex.coerceIn(0, maxIndex))
}

fun LessonProgress.isComplete(lessons: List<Lesson>): Boolean =
    lessons.isNotEmpty() && completedLessonIds.size >= lessons.size

fun LessonProgress.currentLessonOrNull(lessons: List<Lesson>): Lesson? =
    if (isComplete(lessons)) null else lessons.getOrNull(currentLessonIndex)

fun LessonProgress.advanceWith(
    completedLessonId: String,
    rewardXp: Int,
    totalLessons: Int,
): LessonProgress {
    if (completedLessonId in completedLessonIds) return this

    val updatedCompletedLessonIds = completedLessonIds + completedLessonId
    val isNowComplete = totalLessons > 0 && updatedCompletedLessonIds.size >= totalLessons

    return copy(
        currentLessonIndex = if (isNowComplete) totalLessons else currentLessonIndex + 1,
        completedLessonIds = updatedCompletedLessonIds,
        totalXp = totalXp + rewardXp,
    )
}
