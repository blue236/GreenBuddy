package com.blue236.greenbuddy.model

data class GrowthStageVisual(
    val title: String,
    val accentLabel: String,
    val emoji: String,
    val progress: Float,
    val milestoneText: String,
)

data class CompanionFeedback(
    val title: String,
    val message: String,
    val focusLabel: String,
)

fun LessonProgress.growthStageVisual(lessons: List<Lesson>): GrowthStageVisual {
    val completion = if (lessons.isEmpty()) 0f else completedCount.toFloat() / lessons.size.toFloat()
    return when {
        completion >= 1f -> GrowthStageVisual("Blooming", "Full starter growth", "🌿", 1f, "Your starter track is complete.")
        completion >= 0.66f -> GrowthStageVisual("Growing", "Visible new growth", "🌱", completion, "A few more lessons unlock the final stage.")
        completion >= 0.33f -> GrowthStageVisual("Sprouting", "Healthy progress", "🪴", completion, "Keep learning to grow stronger.")
        else -> GrowthStageVisual("Seedling", "Just getting started", "🌰", completion, "Finish lessons and steady care to evolve.")
    }
}

fun companionFeedback(
    plantName: String,
    careState: PlantCareState,
    progress: LessonProgress,
    lessons: List<Lesson>,
): CompanionFeedback {
    val currentLesson = progress.currentLessonOrNull(lessons)
    return when {
        careState.hydration <= 35 -> CompanionFeedback(
            title = "$plantName needs water",
            message = "Hydration is your most urgent stat right now.",
            focusLabel = "Water soon",
        )
        careState.sunlight <= 35 -> CompanionFeedback(
            title = "$plantName wants more light",
            message = "A sun bath would quickly improve the overall mood.",
            focusLabel = "Boost sunlight",
        )
        careState.nutrition <= 35 -> CompanionFeedback(
            title = "$plantName is low on nutrients",
            message = "A little fertilizer will help support steady growth.",
            focusLabel = "Feed the plant",
        )
        progress.isComplete(lessons) -> CompanionFeedback(
            title = "$plantName finished the starter path",
            message = "The lessons are done, so now the loop is about consistency and care.",
            focusLabel = "Maintain the streak",
        )
        currentLesson != null -> CompanionFeedback(
            title = "$plantName is ready for the next step",
            message = "Keep the care stats balanced, then finish \"${currentLesson.title}\".",
            focusLabel = "Next: ${currentLesson.title}",
        )
        else -> CompanionFeedback(
            title = "$plantName is waiting on you",
            message = "A lesson and one care action are enough to make progress today.",
            focusLabel = "Build momentum",
        )
    }
}
