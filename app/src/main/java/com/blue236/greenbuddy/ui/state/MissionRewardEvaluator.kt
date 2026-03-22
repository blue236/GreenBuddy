package com.blue236.greenbuddy.ui.state

import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.DailyMissionSet
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.claimStreakRewardIfEligible
import com.blue236.greenbuddy.model.completeDailyMissions
import com.blue236.greenbuddy.model.normalizedFor
import com.blue236.greenbuddy.model.resolveForToday
import java.time.LocalDate

internal fun evaluateMissionCompletionRewards(
    progress: DailyMissionProgress,
    rewardState: RewardState,
    lessonProgress: LessonProgress,
    careState: PlantCareState,
    today: LocalDate,
): RewardOutcome {
    val missionSet = progress.resolveForToday(today, lessonProgress, careState)
    if (!missionSet.allCompletedToday) return RewardOutcome(progress.normalizedFor(today), rewardState)

    var updatedProgress = progress.completeDailyMissions(today)
    var updatedRewardState = rewardState
    var daily = false
    var streak = false

    if (updatedProgress.claimedDailyRewardDate == today.toString() && progress.claimedDailyRewardDate != today.toString()) {
        updatedRewardState = updatedRewardState.rewardForDailyMissionCompletion()
        daily = true
    }

    val before = updatedProgress.streakRewardClaimedForStreak
    updatedProgress = updatedProgress.claimStreakRewardIfEligible(today)
    if (updatedProgress.streakRewardClaimedForStreak != before) {
        updatedRewardState = updatedRewardState.rewardForStreakBonus()
        streak = true
    }

    val feedbackMessageRes = when {
        daily && streak -> R.string.reward_feedback_daily_mission_complete_with_streak
        daily -> R.string.reward_feedback_daily_mission_complete
        else -> null
    }

    return RewardOutcome(
        progress = updatedProgress,
        rewardState = updatedRewardState,
        dailyAwarded = daily,
        streakAwarded = streak,
        feedbackMessageRes = feedbackMessageRes,
        dailyRewardTokensAwarded = if (daily) DailyMissionSet.DAILY_REWARD_TOKENS else 0,
        streakRewardTokensAwarded = if (streak) DailyMissionSet.STREAK_REWARD_TOKENS else 0,
    )
}

internal data class RewardOutcome(
    val progress: DailyMissionProgress,
    val rewardState: RewardState,
    val dailyAwarded: Boolean = false,
    val streakAwarded: Boolean = false,
    val feedbackMessageRes: Int? = null,
    val dailyRewardTokensAwarded: Int = 0,
    val streakRewardTokensAwarded: Int = 0,
)
