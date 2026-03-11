package com.blue236.greenbuddy.model

import kotlin.math.roundToInt

data class GrowthStageRule(
    val rank: Int,
    val id: String,
    val title: String,
    val accentLabel: String,
    val emoji: String,
    val requiredXp: Int,
    val minimumCareScore: Int,
    val unlockedMessage: String,
)

data class SpeciesGrowthProfile(
    val starterId: String,
    val species: String,
    val stages: List<GrowthStageRule>,
)

data class GrowthStageState(
    val currentStage: GrowthStageRule,
    val nextStage: GrowthStageRule?,
    val progressToNextStage: Float,
    val readinessPercent: Int,
    val requirementSummary: String,
    val unlockHint: String,
    val newlyUnlocked: Boolean,
)

object GrowthCatalog {
    private val profiles = listOf(
        SpeciesGrowthProfile(
            starterId = "monstera",
            species = "Monstera",
            stages = listOf(
                GrowthStageRule(
                    rank = 0,
                    id = "sprout",
                    title = "Sprout",
                    accentLabel = "Settling into bright indirect light",
                    emoji = "🌱",
                    requiredXp = 0,
                    minimumCareScore = 0,
                    unlockedMessage = "Leafling has taken root. Keep lessons and care steady to open the first fenestrated leaves.",
                ),
                GrowthStageRule(
                    rank = 1,
                    id = "juvenile",
                    title = "Juvenile",
                    accentLabel = "First split leaves are forming",
                    emoji = "🪴",
                    requiredXp = 20,
                    minimumCareScore = 60,
                    unlockedMessage = "Leafling pushed out a bigger leaf. Balanced care unlocked the juvenile Monstera stage.",
                ),
                GrowthStageRule(
                    rank = 2,
                    id = "climbing",
                    title = "Climbing",
                    accentLabel = "Ready to stretch and climb",
                    emoji = "🌿",
                    requiredXp = 45,
                    minimumCareScore = 72,
                    unlockedMessage = "Leafling is climbing now. Finished lessons plus strong care unlocked the mature Monstera form.",
                ),
            ),
        ),
        SpeciesGrowthProfile(
            starterId = "basil",
            species = "Basil",
            stages = listOf(
                GrowthStageRule(
                    rank = 0,
                    id = "seedling",
                    title = "Seedling",
                    accentLabel = "Tender stems, quick daily growth",
                    emoji = "🌱",
                    requiredXp = 0,
                    minimumCareScore = 0,
                    unlockedMessage = "Pesto is up and growing. Consistency matters more than perfection here.",
                ),
                GrowthStageRule(
                    rank = 1,
                    id = "bushy",
                    title = "Bushy",
                    accentLabel = "Pinched and branching nicely",
                    emoji = "🌿",
                    requiredXp = 20,
                    minimumCareScore = 55,
                    unlockedMessage = "Pesto branched out fast. Good light and moisture unlocked the bushy basil stage.",
                ),
                GrowthStageRule(
                    rank = 2,
                    id = "harvest-ready",
                    title = "Harvest Ready",
                    accentLabel = "Dense leaves and kitchen-ready vigor",
                    emoji = "🌿",
                    requiredXp = 45,
                    minimumCareScore = 68,
                    unlockedMessage = "Pesto is harvest ready. You earned the final basil stage with lessons plus strong upkeep.",
                ),
            ),
        ),
        SpeciesGrowthProfile(
            starterId = "tomato",
            species = "Tomato",
            stages = listOf(
                GrowthStageRule(
                    rank = 0,
                    id = "starter",
                    title = "Starter",
                    accentLabel = "Building roots and stem strength",
                    emoji = "🌿",
                    requiredXp = 0,
                    minimumCareScore = 0,
                    unlockedMessage = "Sunny is established. Tomatoes need more upkeep before they really take off.",
                ),
                GrowthStageRule(
                    rank = 1,
                    id = "flowering",
                    title = "Flowering",
                    accentLabel = "Strong stems and first blossoms",
                    emoji = "🌼",
                    requiredXp = 20,
                    minimumCareScore = 70,
                    unlockedMessage = "Sunny is flowering. Extra care discipline unlocked the tomato bloom stage.",
                ),
                GrowthStageRule(
                    rank = 2,
                    id = "fruiting",
                    title = "Fruiting",
                    accentLabel = "Fruit set unlocked",
                    emoji = "🍅",
                    requiredXp = 45,
                    minimumCareScore = 82,
                    unlockedMessage = "Sunny is fruiting now. You hit the tougher tomato thresholds and earned the final evolution.",
                ),
            ),
        ),
    )

    fun forStarter(starterId: String): SpeciesGrowthProfile =
        profiles.firstOrNull { it.starterId == starterId } ?: profiles.first()

    fun forSpecies(species: String): SpeciesGrowthProfile =
        profiles.firstOrNull { it.species == species } ?: profiles.first()
}

fun GrowthStageState.milestoneText(): String =
    nextStage?.let {
        "Next evolution: ${it.title} at ${it.requiredXp} XP and care score ${it.minimumCareScore}+"
    } ?: "Final growth stage unlocked. Keep care steady to hold it."

private fun nextStageUnlockHint(nextStage: GrowthStageRule): String =
    "Reach ${nextStage.requiredXp} XP and care score ${nextStage.minimumCareScore}+ to unlock ${nextStage.title}."

fun GrowthStageState.heroProgress(): Float = nextStage?.let { progressToNextStage } ?: 1f

fun resolveGrowthStageState(
    starterId: String,
    progress: LessonProgress,
    careState: PlantCareState,
    seenStageRank: Int = 0,
): GrowthStageState {
    val profile = GrowthCatalog.forStarter(starterId)
    val unlockedStage = profile.stages
        .filter { progress.totalXp >= it.requiredXp && careState.averageScore >= it.minimumCareScore }
        .maxByOrNull { it.rank }
        ?: profile.stages.first()
    val nextStage = profile.stages.firstOrNull { it.rank == unlockedStage.rank + 1 }

    val progressToNextStage = nextStage?.let {
        val xpSpan = (it.requiredXp - unlockedStage.requiredXp).coerceAtLeast(1)
        val careSpan = (it.minimumCareScore - unlockedStage.minimumCareScore).coerceAtLeast(1)
        val xpProgress = ((progress.totalXp - unlockedStage.requiredXp).coerceAtLeast(0)).toFloat() / xpSpan.toFloat()
        val careProgress = ((careState.averageScore - unlockedStage.minimumCareScore).coerceAtLeast(0)).toFloat() / careSpan.toFloat()
        minOf(1f, (xpProgress + careProgress) / 2f)
    } ?: 1f

    val readinessPercent = (progressToNextStage * 100).roundToInt().coerceIn(0, 100)
    val requirementSummary = nextStage?.let {
        val xpRemaining = (it.requiredXp - progress.totalXp).coerceAtLeast(0)
        val careRemaining = (it.minimumCareScore - careState.averageScore).coerceAtLeast(0)
        buildString {
            append(it.title)
            append(" needs ")
            append(if (xpRemaining == 0) "XP ready" else "$xpRemaining XP more")
            append(" and ")
            append(if (careRemaining == 0) "care ready" else "$careRemaining care points more")
        }
    } ?: "All growth goals met for this starter."
    val unlockHint = nextStage?.let(::nextStageUnlockHint) ?: unlockedStage.unlockedMessage

    return GrowthStageState(
        currentStage = unlockedStage,
        nextStage = nextStage,
        progressToNextStage = progressToNextStage,
        readinessPercent = readinessPercent,
        requirementSummary = requirementSummary,
        unlockHint = unlockHint,
        newlyUnlocked = unlockedStage.rank > seenStageRank,
    )
}
