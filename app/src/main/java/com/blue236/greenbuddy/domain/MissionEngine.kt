package com.blue236.greenbuddy.domain

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

data class MissionRewardOutcome(
    val progress: DailyMissionProgress,
    val rewardState: RewardState,
    val dailyAwarded: Boolean = false,
    val streakAwarded: Boolean = false,
    val dailyRewardTokensAwarded: Int = 0,
    val streakRewardTokensAwarded: Int = 0,
)

class MissionEngine {
    fun resolveToday(
        progress: DailyMissionProgress,
        lessonProgress: LessonProgress,
        careState: PlantCareState,
        today: LocalDate,
    ) = progress.normalizedFor(today).resolveForToday(today, lessonProgress, careState)

    fun evaluateCompletionRewards(
        progress: DailyMissionProgress,
        rewardState: RewardState,
        lessonProgress: LessonProgress,
        careState: PlantCareState,
        today: LocalDate,
    ): MissionRewardOutcome {
        val missionSet = progress.resolveForToday(today, lessonProgress, careState)
        if (!missionSet.allCompletedToday) return MissionRewardOutcome(progress.normalizedFor(today), rewardState)

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

        return MissionRewardOutcome(
            progress = updatedProgress,
            rewardState = updatedRewardState,
            dailyAwarded = daily,
            streakAwarded = streak,
            dailyRewardTokensAwarded = if (daily) DailyMissionSet.DAILY_REWARD_TOKENS else 0,
            streakRewardTokensAwarded = if (streak) DailyMissionSet.STREAK_REWARD_TOKENS else 0,
        )
    }
}
