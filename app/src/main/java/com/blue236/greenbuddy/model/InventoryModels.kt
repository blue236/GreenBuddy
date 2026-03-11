package com.blue236.greenbuddy.model

data class PlantInventoryEntry(
    val option: StarterPlantOption,
    val isOwned: Boolean,
    val isActive: Boolean,
    val progress: LessonProgress,
    val careState: PlantCareState,
    val unlockRequirement: String,
)

fun defaultOwnedStarterIds(selectedStarterId: String): Set<String> = setOf(selectedStarterId)

fun nextUnlockableStarterId(ownedStarterIds: Set<String>): String? =
    StarterPlants.options.firstOrNull { it.id !in ownedStarterIds }?.id

fun unlockRequirementFor(
    option: StarterPlantOption,
    ownedStarterIds: Set<String>,
): String = when {
    option.id in ownedStarterIds -> "Ready in your greenhouse"
    option.id == nextUnlockableStarterId(ownedStarterIds) ->
        "Automatically unlocks when you complete any current plant track."
    else -> "Unlock earlier greenhouse companions first."
}

fun buildInventoryEntries(
    ownedStarterIds: Set<String>,
    selectedStarterId: String,
    lessonProgressByStarterId: Map<String, LessonProgress>,
    careStateByStarterId: Map<String, PlantCareState>,
): List<PlantInventoryEntry> = StarterPlants.options.map { option ->
    PlantInventoryEntry(
        option = option,
        isOwned = option.id in ownedStarterIds,
        isActive = option.id == selectedStarterId,
        progress = lessonProgressByStarterId[option.id] ?: LessonProgress(),
        careState = careStateByStarterId[option.id] ?: PlantCareState.from(option.companion),
        unlockRequirement = unlockRequirementFor(option, ownedStarterIds),
    )
}
