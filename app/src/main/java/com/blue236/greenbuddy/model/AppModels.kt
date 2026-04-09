package com.blue236.greenbuddy.model

data class AppPreferences(
    val onboardingComplete: Boolean = false,
    val selectedStarterId: String = StarterPlants.options.first().id,
    val ownedStarterIds: Set<String> = defaultOwnedStarterIds(StarterPlants.options.first().id),
    val lessonProgressByStarterId: Map<String, LessonProgress> = emptyMap(),
    val plantCareStateByStarterId: Map<String, PlantCareState> = emptyMap(),
    val dailyMissionProgress: DailyMissionProgress = DailyMissionProgress(),
    val seenGrowthStageRank: Int = 0,
    val rewardState: RewardState = RewardState(),
    val reminderState: ReminderState = ReminderState(),
    val realPlantModeStateByStarterId: Map<String, RealPlantModeState> = emptyMap(),
    val selectedWeatherCityId: String = WeatherCatalog.cityOptions.first().id,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val companionConversationMemoryByStarterId: Map<String, CompanionConversationMemory> = emptyMap(),
) {
    val selectedStarter: StarterPlantOption
        get() = StarterPlants.options.firstOrNull { it.id == selectedStarterId && it.id in ownedStarterIds }
            ?: StarterPlants.options.firstOrNull { it.id in ownedStarterIds }
            ?: StarterPlants.options.first()

    val lessonProgress: LessonProgress
        get() = lessonProgressByStarterId[selectedStarter.id] ?: LessonProgress()

    val plantCareState: PlantCareState
        get() = plantCareStateByStarterId[selectedStarter.id] ?: PlantCareState.from(selectedStarter.companion)

    val realPlantModeState: RealPlantModeState
        get() = realPlantModeStateByStarterId[selectedStarter.id] ?: RealPlantModeState()

    val companionConversationMemory: CompanionConversationMemory
        get() = companionConversationMemoryByStarterId[selectedStarter.id] ?: CompanionConversationMemory()
}

data class GreenBuddyUiState(
    val selectedTab: Tab = Tab.HOME,
    val starterOptions: List<StarterPlantOption> = StarterPlants.options,
    val lessons: List<Lesson> = LessonCatalog.forSpecies(StarterPlants.options.first().companion.species),
    val selectedStarterId: String = StarterPlants.options.first().id,
    val ownedStarterIds: Set<String> = defaultOwnedStarterIds(StarterPlants.options.first().id),
    val onboardingComplete: Boolean = false,
    val lessonProgressByStarterId: Map<String, LessonProgress> = emptyMap(),
    val plantCareStateByStarterId: Map<String, PlantCareState> = emptyMap(),
    val dailyMissionProgress: DailyMissionProgress = DailyMissionProgress(),
    val dailyMissionSet: DailyMissionSet? = null,
    val growthStageState: GrowthStageState = resolveGrowthStageState(
        starterId = StarterPlants.options.first().id,
        progress = LessonProgress(),
        careState = PlantCareState.from(StarterPlants.options.first().companion),
    ),
    val rewardState: RewardState = RewardState(),
    val rewardFeedback: String? = null,
    val feedbackEvent: FeedbackEvent? = null,
    val realPlantModeState: RealPlantModeState = RealPlantModeState(),
    val weatherSnapshot: WeatherSnapshot = SeasonalWeatherProvider.snapshotFor(WeatherCatalog.cityOptions.first().id),
    val weatherAdvice: WeatherAdvice = WeatherAdviceGenerator.adviceFor(StarterPlants.options.first(), SeasonalWeatherProvider.snapshotFor(WeatherCatalog.cityOptions.first().id)),
    val companionStateSnapshot: CompanionStateSnapshot = CompanionChatEngine.createSnapshot(
        starter = StarterPlants.options.first(),
        careState = PlantCareState.from(StarterPlants.options.first().companion),
        growthStageState = resolveGrowthStageState(
            starterId = StarterPlants.options.first().id,
            progress = LessonProgress(),
            careState = PlantCareState.from(StarterPlants.options.first().companion),
        ),
        dailyMissionSet = null,
        weatherSnapshot = SeasonalWeatherProvider.snapshotFor(WeatherCatalog.cityOptions.first().id),
        weatherAdvice = WeatherAdviceGenerator.adviceFor(StarterPlants.options.first(), SeasonalWeatherProvider.snapshotFor(WeatherCatalog.cityOptions.first().id)),
        realPlantModeState = RealPlantModeState(),
        recentConversationMemory = CompanionConversationMemory(),
    ),
    val companionHomeCheckIn: CompanionHomeCheckIn = CompanionChatEngine.proactiveCheckIn(
        CompanionChatEngine.createSnapshot(
            starter = StarterPlants.options.first(),
            careState = PlantCareState.from(StarterPlants.options.first().companion),
            growthStageState = resolveGrowthStageState(
                starterId = StarterPlants.options.first().id,
                progress = LessonProgress(),
                careState = PlantCareState.from(StarterPlants.options.first().companion),
            ),
            dailyMissionSet = null,
            weatherSnapshot = SeasonalWeatherProvider.snapshotFor(WeatherCatalog.cityOptions.first().id),
            weatherAdvice = WeatherAdviceGenerator.adviceFor(StarterPlants.options.first(), SeasonalWeatherProvider.snapshotFor(WeatherCatalog.cityOptions.first().id)),
            realPlantModeState = RealPlantModeState(),
            recentConversationMemory = CompanionConversationMemory(),
        ),
    ),
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
) {
    val selectedStarter: StarterPlantOption
        get() = starterOptions.firstOrNull { it.id == selectedStarterId && it.id in ownedStarterIds }
            ?: starterOptions.firstOrNull { it.id in ownedStarterIds }
            ?: starterOptions.first()

    val lessonProgress: LessonProgress
        get() = lessonProgressByStarterId[selectedStarter.id] ?: LessonProgress()

    val plantCareState: PlantCareState
        get() = plantCareStateByStarterId[selectedStarter.id] ?: PlantCareState.from(selectedStarter.companion)

    val inventoryEntries: List<PlantInventoryEntry>
        get() = buildInventoryEntries(
            ownedStarterIds = ownedStarterIds,
            selectedStarterId = selectedStarter.id,
            lessonProgressByStarterId = lessonProgressByStarterId,
            careStateByStarterId = plantCareStateByStarterId,
        )
}
