package com.blue236.greenbuddy.domain

import com.blue236.greenbuddy.model.GrowthStageState
import com.blue236.greenbuddy.model.LessonProgress
import com.blue236.greenbuddy.model.PlantCareState
import com.blue236.greenbuddy.model.resolveGrowthStageState

class GrowthEngine : GrowthUnlockContract {
    fun resolve(starterId: String, lessonProgress: LessonProgress, careState: PlantCareState, seenStageRank: Int = 0): GrowthStageState =
        resolveGrowthStageState(starterId, lessonProgress, careState, seenStageRank)

    override fun didUnlock(starterId: String, lessonProgress: LessonProgress, careState: PlantCareState, previousGrowthStageRank: Int): Boolean =
        resolve(starterId, lessonProgress, careState, previousGrowthStageRank).newlyUnlocked
}
