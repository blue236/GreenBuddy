package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RewardState

data class LessonPersistenceOutcome(
    val analyticsEvent: AnalyticsEvent,
    val starterId: String,
    val lessonProgress: LessonProgress,
    val missionProgress: DailyMissionProgress,
    val rewardState: RewardState,
    val unlockedStarterId: String?,
)

data class CarePersistenceOutcome(
    val analyticsEvent: AnalyticsEvent,
    val starterId: String,
    val updatedCareState: PlantCareState,
    val missionProgress: DailyMissionProgress,
    val rewardState: RewardState,
)

class ActionPersistenceCoordinator {
    fun lessonOutcome(
        starterId: String,
        lessonId: String,
        lessonProgress: LessonProgress,
        missionProgress: DailyMissionProgress,
        rewardState: RewardState,
        unlockedStarterId: String?,
    ): LessonPersistenceOutcome = LessonPersistenceOutcome(
        analyticsEvent = AnalyticsEvent(
            "lesson_completed",
            mapOf(
                "lesson_id" to lessonId,
                "starter_id" to starterId,
            ),
        ),
        starterId = starterId,
        lessonProgress = lessonProgress,
        missionProgress = missionProgress,
        rewardState = rewardState,
        unlockedStarterId = unlockedStarterId,
    )

    fun careOutcome(
        starterId: String,
        actionName: String,
        wasHelpful: Boolean,
        updatedCareState: PlantCareState,
        missionProgress: DailyMissionProgress,
        rewardState: RewardState,
    ): CarePersistenceOutcome = CarePersistenceOutcome(
        analyticsEvent = AnalyticsEvent(
            "care_action",
            mapOf(
                "action" to actionName,
                "starter_id" to starterId,
                "helpful" to wasHelpful.toString(),
            ),
        ),
        starterId = starterId,
        updatedCareState = updatedCareState,
        missionProgress = missionProgress,
        rewardState = rewardState,
    )
}
