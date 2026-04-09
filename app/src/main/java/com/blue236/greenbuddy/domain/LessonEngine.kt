package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.DailyMissionProgress
import com.blue236.greenbuddy.model.Lesson
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.advanceWith
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.model.isComplete
import com.blue236.greenbuddy.model.nextUnlockableStarterId
import com.blue236.greenbuddy.model.recordLessonCompletion
import java.time.LocalDate

data class LessonActionResult(
    val accepted: Boolean,
    val lesson: Lesson? = null,
    val lessonProgress: LessonProgress,
    val missionProgress: DailyMissionProgress,
    val rewardState: RewardState,
    val missionRewardOutcome: MissionRewardOutcome? = null,
    val unlockedStarterId: String? = null,
)

class LessonEngine(
    private val missionEngine: MissionEngine,
) {
    fun submitAnswer(
        selectedAnswerIndex: Int,
        lessons: List<Lesson>,
        lessonProgress: LessonProgress,
        missionProgress: DailyMissionProgress,
        rewardState: RewardState,
        careState: PlantCareState,
        today: LocalDate,
        ownedStarterIds: Set<String>,
    ): LessonActionResult {
        val currentLesson = lessonProgress.currentLessonOrNull(lessons) ?: return LessonActionResult(
            accepted = false,
            lessonProgress = lessonProgress,
            missionProgress = missionProgress,
            rewardState = rewardState,
        )
        if (selectedAnswerIndex != currentLesson.quiz.correctAnswerIndex) {
            return LessonActionResult(
                accepted = false,
                lesson = currentLesson,
                lessonProgress = lessonProgress,
                missionProgress = missionProgress,
                rewardState = rewardState,
            )
        }

        val updatedLessonProgress = lessonProgress.advanceWith(currentLesson.id, currentLesson.rewardXp, lessons.size)
        val missionOutcome = missionEngine.evaluateCompletionRewards(
            progress = missionProgress.recordLessonCompletion(today),
            rewardState = rewardState.rewardForLesson(currentLesson.rewardXp),
            lessonProgress = updatedLessonProgress,
            careState = careState,
            today = today,
        )
        val unlockedStarterId = if (updatedLessonProgress.isComplete(lessons)) nextUnlockableStarterId(ownedStarterIds) else null

        return LessonActionResult(
            accepted = true,
            lesson = currentLesson,
            lessonProgress = updatedLessonProgress,
            missionProgress = missionOutcome.progress,
            rewardState = missionOutcome.rewardState,
            missionRewardOutcome = missionOutcome,
            unlockedStarterId = unlockedStarterId,
        )
    }
}
