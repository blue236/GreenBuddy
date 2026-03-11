package com.blue236.greenbuddy.model

import java.time.LocalDate

enum class DailyMissionType {
    COMPLETE_LESSON,
    PERFORM_CARE_ACTION,
    KEEP_STAT_ABOVE_THRESHOLD,
}

enum class CareStatType(val label: String) {
    HYDRATION("Hydration"),
    SUNLIGHT("Sunlight"),
    NUTRITION("Nutrition"),
}

data class DailyMission(
    val id: String,
    val type: DailyMissionType,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
)

data class DailyMissionSet(
    val date: LocalDate,
    val missions: List<DailyMission>,
    val currentStreak: Int,
    val longestStreak: Int,
    val allCompletedToday: Boolean,
    val dailyRewardClaimed: Boolean,
    val streakRewardClaimedForStreak: Int?,
    val dailyRewardTokens: Int = DAILY_REWARD_TOKENS,
    val streakRewardTokens: Int = STREAK_REWARD_TOKENS,
) {
    val completedCount: Int = missions.count { it.isCompleted }
    val totalCount: Int = missions.size
    val streakRewardMilestoneReached: Boolean = currentStreak > 0 && currentStreak % STREAK_REWARD_EVERY_DAYS == 0
    val pendingStreakReward: Boolean = streakRewardMilestoneReached && streakRewardClaimedForStreak != currentStreak

    companion object {
        const val DAILY_REWARD_TOKENS = 15
        const val STREAK_REWARD_TOKENS = 10
        const val STREAK_REWARD_EVERY_DAYS = 3
    }
}

data class DailyMissionProgress(
    val missionDate: String = "",
    val completedCareActionsToday: Int = 0,
    val completedLessonsToday: Int = 0,
    val claimedDailyRewardDate: String? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val streakRewardClaimedForStreak: Int? = null,
)

private data class ThresholdConfig(
    val statType: CareStatType,
    val threshold: Int,
)

fun DailyMissionProgress.resolveForToday(
    today: LocalDate,
    lessonProgress: LessonProgress,
    careState: PlantCareState,
): DailyMissionSet {
    val normalized = normalizedFor(today)
    val thresholdConfig = thresholdConfigFor(today)

    val statValue = when (thresholdConfig.statType) {
        CareStatType.HYDRATION -> careState.hydration
        CareStatType.SUNLIGHT -> careState.sunlight
        CareStatType.NUTRITION -> careState.nutrition
    }

    val completedTypes = buildSet {
        if (normalized.completedLessonsToday > 0) add(DailyMissionType.COMPLETE_LESSON)
        if (normalized.completedCareActionsToday > 0) add(DailyMissionType.PERFORM_CARE_ACTION)
        if (statValue >= thresholdConfig.threshold) add(DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD)
    }
    val allCompletedToday = completedTypes.size == DailyMissionType.entries.size

    return DailyMissionSet(
        date = today,
        missions = listOf(
            DailyMission(
                id = missionId(today, DailyMissionType.COMPLETE_LESSON),
                type = DailyMissionType.COMPLETE_LESSON,
                title = "Finish one lesson",
                description = "Complete today’s lesson quiz to keep growth moving.",
                isCompleted = DailyMissionType.COMPLETE_LESSON in completedTypes,
            ),
            DailyMission(
                id = missionId(today, DailyMissionType.PERFORM_CARE_ACTION),
                type = DailyMissionType.PERFORM_CARE_ACTION,
                title = "Do one care action",
                description = "Water, fertilize, or give your plant a sun bath.",
                isCompleted = DailyMissionType.PERFORM_CARE_ACTION in completedTypes,
            ),
            DailyMission(
                id = missionId(today, DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD),
                type = DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD,
                title = "Keep ${thresholdConfig.statType.label} above ${thresholdConfig.threshold}",
                description = "Hold ${thresholdConfig.statType.label.lowercase()} at ${thresholdConfig.threshold}+ for today.",
                isCompleted = DailyMissionType.KEEP_STAT_ABOVE_THRESHOLD in completedTypes,
            ),
        ),
        currentStreak = normalized.currentStreak,
        longestStreak = normalized.longestStreak,
        allCompletedToday = allCompletedToday,
        dailyRewardClaimed = normalized.claimedDailyRewardDate == today.toString(),
        streakRewardClaimedForStreak = normalized.streakRewardClaimedForStreak,
    )
}

fun DailyMissionProgress.normalizedFor(today: LocalDate): DailyMissionProgress {
    val todayString = today.toString()
    val yesterdayString = today.minusDays(1).toString()
    val streakStillActive = lastCompletedDate == todayString || lastCompletedDate == yesterdayString

    if (missionDate == todayString) {
        return if (streakStillActive) {
            this
        } else {
            copy(
                currentStreak = 0,
                streakRewardClaimedForStreak = null,
            )
        }
    }

    return copy(
        missionDate = todayString,
        completedCareActionsToday = 0,
        completedLessonsToday = 0,
        currentStreak = if (streakStillActive) currentStreak else 0,
        streakRewardClaimedForStreak = if (lastCompletedDate == yesterdayString) streakRewardClaimedForStreak else null,
    )
}

fun DailyMissionProgress.recordCareAction(today: LocalDate): DailyMissionProgress {
    val normalized = normalizedFor(today)
    return normalized.copy(completedCareActionsToday = normalized.completedCareActionsToday + 1)
}

fun DailyMissionProgress.recordLessonCompletion(today: LocalDate): DailyMissionProgress {
    val normalized = normalizedFor(today)
    return normalized.copy(completedLessonsToday = normalized.completedLessonsToday + 1)
}

fun DailyMissionProgress.completeDailyMissions(today: LocalDate): DailyMissionProgress {
    val normalized = normalizedFor(today)
    val todayString = today.toString()
    if (normalized.claimedDailyRewardDate == todayString) return normalized

    val previousDate = normalized.lastCompletedDate?.let(LocalDate::parse)
    val newStreak = when {
        previousDate == null -> 1
        previousDate == today -> normalized.currentStreak
        previousDate == today.minusDays(1) -> normalized.currentStreak + 1
        else -> 1
    }

    return normalized.copy(
        claimedDailyRewardDate = todayString,
        currentStreak = newStreak,
        longestStreak = maxOf(normalized.longestStreak, newStreak),
        lastCompletedDate = todayString,
    )
}

fun DailyMissionProgress.claimStreakRewardIfEligible(today: LocalDate): DailyMissionProgress {
    val normalized = normalizedFor(today)
    val streak = normalized.currentStreak
    if (streak == 0 || streak % DailyMissionSet.STREAK_REWARD_EVERY_DAYS != 0) return normalized
    if (normalized.streakRewardClaimedForStreak == streak) return normalized
    return normalized.copy(streakRewardClaimedForStreak = streak)
}

private fun thresholdConfigFor(date: LocalDate): ThresholdConfig {
    val statType = when (Math.floorMod(date.dayOfYear, 3)) {
        0 -> CareStatType.HYDRATION
        1 -> CareStatType.SUNLIGHT
        else -> CareStatType.NUTRITION
    }
    val threshold = when (Math.floorMod(date.dayOfYear, 3)) {
        0 -> 65
        1 -> 70
        else -> 75
    }
    return ThresholdConfig(statType = statType, threshold = threshold)
}

private fun missionId(date: LocalDate, type: DailyMissionType): String = "${date}__${type.name}"
