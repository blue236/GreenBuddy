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

fun GrowthStageRule.localizedGrowthTitle(languageTag: String): String = when (id) {
    "sprout" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Keimling"; "ko" -> "새싹"; else -> title }
    "juvenile" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Jungpflanze"; "ko" -> "유년기"; else -> title }
    "climbing" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Kletterphase"; "ko" -> "등반 단계"; else -> title }
    "seedling" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Sämling"; "ko" -> "유묘"; else -> title }
    "bushy" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Buschig"; "ko" -> "풍성한 단계"; else -> title }
    "harvest-ready" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Erntebereit"; "ko" -> "수확 준비 완료"; else -> title }
    "starter" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Starter"; "ko" -> "스타터"; else -> title }
    "flowering" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Blühend"; "ko" -> "개화 단계"; else -> title }
    "fruiting" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Fruchtend"; "ko" -> "결실 단계"; else -> title }
    else -> title
}

fun GrowthStageRule.localizedGrowthAccentLabel(languageTag: String): String = when (id) {
    "sprout" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Gewöhnt sich an helles, indirektes Licht"; "ko" -> "밝은 간접광에 적응하는 중"; else -> accentLabel }
    "juvenile" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Die ersten geschlitzten Blätter entstehen"; "ko" -> "처음 갈라진 잎이 생기고 있어요"; else -> accentLabel }
    "climbing" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Bereit, sich zu strecken und zu klettern"; "ko" -> "길게 뻗고 타고 오를 준비가 됐어요"; else -> accentLabel }
    "seedling" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Zarte Stiele, schnelles tägliches Wachstum"; "ko" -> "연한 줄기로 매일 빠르게 자라는 중"; else -> accentLabel }
    "bushy" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Schön verzweigt und kompakt"; "ko" -> "가지가 잘 퍼지고 풍성해졌어요"; else -> accentLabel }
    "harvest-ready" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Dichte Blätter und küchenbereite Kraft"; "ko" -> "잎이 촘촘하고 바로 수확할 활력이 있어요"; else -> accentLabel }
    "starter" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Baut Wurzeln und Stängelkraft auf"; "ko" -> "뿌리와 줄기 힘을 기르는 중"; else -> accentLabel }
    "flowering" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Kräftige Stiele und erste Blüten"; "ko" -> "튼튼한 줄기와 첫 꽃이 보여요"; else -> accentLabel }
    "fruiting" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Fruchtansatz freigeschaltet"; "ko" -> "열매 맺기가 시작됐어요"; else -> accentLabel }
    else -> accentLabel
}

fun GrowthStageRule.localizedUnlockedMessage(languageTag: String): String = when (id) {
    "sprout" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Leafling hat Wurzeln geschlagen. Halte Lektionen und Pflege konstant, damit sich die ersten geschlitzten Blätter öffnen."; "ko" -> "리플링이 자리를 잡았어요. 레슨과 돌봄을 꾸준히 이어 가면 첫 갈라진 잎이 열려요."; else -> unlockedMessage }
    "juvenile" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Leafling hat ein größeres Blatt geschoben. Ausgewogene Pflege hat die junge Monstera-Stufe freigeschaltet."; "ko" -> "리플링이 더 큰 잎을 밀어 올렸어요. 균형 잡힌 돌봄으로 몬스테라 유년기가 열렸어요."; else -> unlockedMessage }
    "climbing" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Leafling klettert jetzt los. Abgeschlossene Lektionen und starke Pflege haben die reife Monstera-Form freigeschaltet."; "ko" -> "리플링이 이제 본격적으로 타고 올라가요. 레슨 완료와 탄탄한 돌봄으로 성숙한 몬스테라 단계가 열렸어요."; else -> unlockedMessage }
    "seedling" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Pesto wächst jetzt richtig an. Hier zählt Beständigkeit mehr als Perfektion."; "ko" -> "페스토가 올라오기 시작했어요. 여기서는 완벽함보다 꾸준함이 더 중요해요."; else -> unlockedMessage }
    "bushy" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Pesto hat sich schnell verzweigt. Gutes Licht und Feuchtigkeit haben die buschige Basilikum-Stufe freigeschaltet."; "ko" -> "페스토가 빠르게 가지를 쳤어요. 좋은 빛과 수분으로 풍성한 바질 단계가 열렸어요."; else -> unlockedMessage }
    "harvest-ready" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Pesto ist erntebereit. Mit Lektionen und guter Pflege hast du die letzte Basilikum-Stufe erreicht."; "ko" -> "페스토가 수확 준비를 마쳤어요. 레슨과 꾸준한 관리로 바질의 마지막 단계를 얻었어요."; else -> unlockedMessage }
    "starter" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Sunny hat sich etabliert. Tomaten brauchen noch etwas mehr Pflege, bevor sie richtig durchstarten."; "ko" -> "써니가 자리를 잡았어요. 토마토는 본격 성장 전에 더 많은 관리가 필요해요."; else -> unlockedMessage }
    "flowering" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Sunny blüht jetzt. Zusätzliche Pflege hat die Tomaten-Blühphase freigeschaltet."; "ko" -> "써니가 꽃을 피우고 있어요. 더 꼼꼼한 돌봄으로 토마토 개화 단계가 열렸어요."; else -> unlockedMessage }
    "fruiting" -> when (normalizedLanguageTag(languageTag)) { "de" -> "Sunny trägt jetzt Früchte. Du hast die höheren Tomaten-Hürden geschafft und die letzte Entwicklung erreicht."; "ko" -> "써니가 이제 열매를 맺고 있어요. 더 까다로운 토마토 기준을 넘겨 마지막 진화를 얻었어요."; else -> unlockedMessage }
    else -> unlockedMessage
}

fun GrowthStageState.localizedRequirementSummary(languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
    "de" -> nextStage?.let { "Nächste Stufe: ${it.localizedGrowthTitle(languageTag)} · Fortschritt ${readinessPercent}%" } ?: "Alle Wachstumsziele für diesen Starter erreicht."
    "ko" -> nextStage?.let { "다음 단계: ${it.localizedGrowthTitle(languageTag)} · 진행도 ${readinessPercent}%" } ?: "이 스타터의 성장 목표를 모두 달성했어요."
    else -> requirementSummary
}

fun GrowthStageState.localizedUnlockHint(languageTag: String): String = nextStage?.let {
    when (normalizedLanguageTag(languageTag)) {
        "de" -> "Erreiche ${it.requiredXp} XP und Pflegewert ${it.minimumCareScore}+ für ${it.localizedGrowthTitle(languageTag)}."
        "ko" -> "${it.localizedGrowthTitle(languageTag)} 단계를 열려면 XP ${it.requiredXp}와 돌봄 점수 ${it.minimumCareScore}+가 필요해요."
        else -> unlockHint
    }
} ?: currentStage.localizedUnlockedMessage(languageTag)

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
