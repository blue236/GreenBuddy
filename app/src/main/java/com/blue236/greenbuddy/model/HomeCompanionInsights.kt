package com.blue236.greenbuddy.model

data class GrowthStageVisual(
    val title: String,
    val emoji: String,
    val accentLabel: String,
    val progress: Float,
    val milestoneText: String,
)

data class CompanionFeedback(
    val title: String,
    val message: String,
    val focusLabel: String,
)

private data class GrowthStageDefinition(
    val minimumCompletionPercent: Int,
    val title: String,
    val emoji: String,
    val accentLabel: String,
)

private val growthStages = listOf(
    GrowthStageDefinition(0, "Sprout", "🌱", "Just started"),
    GrowthStageDefinition(25, "Seedling", "🌿", "Building roots"),
    GrowthStageDefinition(60, "Young Plant", "🪴", "Looking stronger"),
    GrowthStageDefinition(100, "Blooming", "✨", "Starter path complete"),
)

fun LessonProgress.growthStageVisual(lessons: List<Lesson>): GrowthStageVisual {
    val completionPercent = if (lessons.isEmpty()) 0 else (completedCount * 100) / lessons.size
    val stage = growthStages.last { completionPercent >= it.minimumCompletionPercent }
    val progress = if (lessons.isEmpty()) 0f else completedCount.toFloat() / lessons.size.toFloat()
    val nextStage = growthStages.firstOrNull { it.minimumCompletionPercent > completionPercent }

    return GrowthStageVisual(
        title = stage.title,
        emoji = stage.emoji,
        accentLabel = stage.accentLabel,
        progress = progress.coerceIn(0f, 1f),
        milestoneText = nextStage?.let {
            "${it.minimumCompletionPercent - completionPercent}% more lesson progress to reach ${it.title}."
        } ?: "Growth loop complete — keep caring to maintain a happy plant."
    )
}

fun companionFeedback(
    plantName: String,
    careState: PlantCareState,
    progress: LessonProgress,
    lessons: List<Lesson>,
): CompanionFeedback {
    val currentLesson = progress.currentLessonOrNull(lessons)
    val completedCount = progress.completedCount
    val totalLessons = lessons.size

    return when {
        careState.hydration <= 35 -> CompanionFeedback(
            title = "$plantName needs a drink",
            message = "I’m fading a bit. A quick water break would perk me up before the next lesson.",
            focusLabel = "Water now",
        )
        careState.sunlight <= 35 -> CompanionFeedback(
            title = "$plantName wants brighter light",
            message = "I can keep up, but I’d be much happier closer to a window or brighter spot.",
            focusLabel = "More sunlight",
        )
        careState.nutrition <= 35 -> CompanionFeedback(
            title = "$plantName is running low on nutrients",
            message = "A little plant food would help me turn your study streak into stronger growth.",
            focusLabel = "Feed soon",
        )
        progress.isComplete(lessons) && careState.averageScore >= 75 -> CompanionFeedback(
            title = "$plantName is thriving",
            message = "You finished the starter track and kept my care steady. This feels like a real routine now.",
            focusLabel = "Maintain the streak",
        )
        progress.isComplete(lessons) -> CompanionFeedback(
            title = "$plantName finished growing, but still needs care",
            message = "The lessons are done, but I still react to how you care for me. Keep the stats balanced.",
            focusLabel = "Care check",
        )
        completedCount == 0 -> CompanionFeedback(
            title = "$plantName is ready to learn with you",
            message = "Start the first lesson and I’ll begin changing as we build your plant-care basics.",
            focusLabel = "Begin lesson 1",
        )
        currentLesson != null && careState.averageScore >= 75 -> CompanionFeedback(
            title = "$plantName noticed your momentum",
            message = "Nice balance so far. Finish \"${currentLesson.title}\" and I’ll look even more grown-in.",
            focusLabel = "$completedCount/$totalLessons lessons done",
        )
        currentLesson != null -> CompanionFeedback(
            title = "$plantName is reacting to today’s routine",
            message = "We’ve completed $completedCount of $totalLessons lessons. Keep my care up, then tackle \"${currentLesson.title}\".",
            focusLabel = "Next: ${currentLesson.title}",
        )
        else -> CompanionFeedback(
            title = "$plantName is settling in",
            message = "We’re between milestones, but every care action still shows up in how I feel.",
            focusLabel = "Check care stats",
        )
    }
}
