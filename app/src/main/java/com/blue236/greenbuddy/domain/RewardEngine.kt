package com.blue236.greenbuddy.domain

import android.content.Context
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.localizedLabel
import com.blue236.greenbuddy.model.localizedName
import com.blue236.greenbuddy.model.localizedTitle

class RewardEngine(
    private val context: Context,
) : RewardEngineContract {
    override fun lessonFeedback(
        rewardXp: Int,
        missionOutcome: MissionRewardOutcome,
    ): String = when {
        missionOutcome.dailyAwarded && missionOutcome.streakAwarded -> context.getString(
            R.string.reward_feedback_daily_mission_complete_with_streak,
            missionOutcome.dailyRewardTokensAwarded,
            missionOutcome.streakRewardTokensAwarded,
        )
        missionOutcome.dailyAwarded -> context.getString(
            R.string.reward_feedback_daily_mission_complete,
            missionOutcome.dailyRewardTokensAwarded,
        )
        else -> context.getString(
            R.string.reward_feedback_lesson_complete,
            rewardXp,
            RewardState.lessonTokenReward(rewardXp),
        )
    }

    override fun careFeedback(
        action: CareAction,
        languageTag: String,
        wasHelpful: Boolean,
        missionOutcome: MissionRewardOutcome,
    ): String = when {
        missionOutcome.dailyAwarded && missionOutcome.streakAwarded -> context.getString(
            R.string.reward_feedback_daily_mission_complete_with_streak,
            missionOutcome.dailyRewardTokensAwarded,
            missionOutcome.streakRewardTokensAwarded,
        )
        missionOutcome.dailyAwarded -> context.getString(
            R.string.reward_feedback_daily_mission_complete,
            missionOutcome.dailyRewardTokensAwarded,
        )
        wasHelpful -> context.getString(
            R.string.reward_feedback_care_helped,
            action.localizedLabel(languageTag),
            RewardState.careTokenReward(),
        )
        else -> context.getString(
            R.string.reward_feedback_care_no_effect,
            action.localizedLabel(languageTag),
        )
    }

    override fun cosmeticFeedback(item: CosmeticItem, languageTag: String): String = context.getString(
        R.string.reward_feedback_cosmetic_unlocked,
        item.localizedName(languageTag),
        item.emoji,
    )

    override fun greenhouseUnlockFeedback(baseFeedback: String, unlockedStarter: StarterPlantOption?, languageTag: String): String =
        unlockedStarter?.let { starter ->
            context.getString(
                R.string.reward_feedback_with_greenhouse_unlock,
                baseFeedback,
                context.getString(
                    R.string.reward_feedback_greenhouse_unlock,
                    starter.localizedTitle(languageTag),
                    starter.previewEmoji,
                ),
            )
        } ?: baseFeedback
}
