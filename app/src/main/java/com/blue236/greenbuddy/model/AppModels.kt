package com.blue236.greenbuddy.model

data class AppPreferences(
    val onboardingComplete: Boolean = false,
    val selectedStarterId: String = StarterPlants.options.first().id,
    val lessonProgress: LessonProgress = LessonProgress(),
) {
    val selectedStarter: StarterPlantOption
        get() = StarterPlants.options.firstOrNull { it.id == selectedStarterId } ?: StarterPlants.options.first()
}

data class GreenBuddyUiState(
    val selectedTab: Tab = Tab.HOME,
    val starterOptions: List<StarterPlantOption> = StarterPlants.options,
    val selectedStarterId: String = StarterPlants.options.first().id,
    val onboardingComplete: Boolean = false,
    val lessonProgress: LessonProgress = LessonProgress(),
) {
    val selectedStarter: StarterPlantOption
        get() = starterOptions.firstOrNull { it.id == selectedStarterId } ?: starterOptions.first()
}
