package com.blue236.greenbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CompanionChatEngineTest {
    private val starter = StarterPlants.options.first { it.id == "basil" }
    private val careState = PlantCareState(hydration = 30, sunlight = 84, nutrition = 55)
    private val growthState = resolveGrowthStageState(
        starterId = starter.id,
        progress = LessonProgress(totalXp = 18),
        careState = careState,
    )
    private val missionSet = DailyMissionProgress(completedCareActionsToday = 1)
        .normalizedFor(LocalDate.now())
        .resolveForToday(LocalDate.now(), LessonProgress(), careState)
    private val weatherSnapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 1, 10))
    private val weatherAdvice = WeatherAdviceGenerator.adviceFor(starter, weatherSnapshot)

    @Test
    fun detectIntent_mapsKnownCategories() {
        assertEquals(CompanionChatIntent.STATUS_CHECK, CompanionChatEngine.detectIntent("How are you feeling?"))
        assertEquals(CompanionChatIntent.CARE_ADVICE, CompanionChatEngine.detectIntent("Do you need water?"))
        assertEquals(CompanionChatIntent.MISSION_HELP, CompanionChatEngine.detectIntent("What mission should I do today?"))
        assertEquals(CompanionChatIntent.GROWTH_QUESTION, CompanionChatEngine.detectIntent("How do you grow to the next stage?"))
        assertEquals(CompanionChatIntent.WEATHER_QUESTION, CompanionChatEngine.detectIntent("How is the weather for you?"))
        assertEquals(CompanionChatIntent.CASUAL_CHAT, CompanionChatEngine.detectIntent("You seem fun today"))
    }

    @Test
    fun detectIntent_usesRecentMemoryForShortFollowUps() {
        val memory = CompanionConversationMemory().withExchange(
            userMessage = "How are you growing?",
            userIntent = CompanionChatIntent.GROWTH_QUESTION,
            companionReply = "Quite nicely."
        )

        assertEquals(CompanionChatIntent.GROWTH_QUESTION, CompanionChatEngine.detectIntent("and now?", memory))
        assertEquals(CompanionChatIntent.GROWTH_QUESTION, CompanionChatEngine.detectIntent("more?", memory))
    }

    @Test
    fun detectIntent_mapsGermanAndKoreanKeywords() {
        assertEquals(CompanionChatIntent.STATUS_CHECK, CompanionChatEngine.detectIntent("Wie geht es dir?"))
        assertEquals(CompanionChatIntent.CARE_ADVICE, CompanionChatEngine.detectIntent("물 필요해?"))
        assertEquals(CompanionChatIntent.MISSION_HELP, CompanionChatEngine.detectIntent("Welche Mission zuerst?"))
        assertEquals(CompanionChatIntent.GROWTH_QUESTION, CompanionChatEngine.detectIntent("다음 성장 단계가 뭐야?"))
        assertEquals(CompanionChatIntent.WEATHER_QUESTION, CompanionChatEngine.detectIntent("Wie ist das Wetter für dich?"))
    }

    @Test
    fun createSnapshot_capturesCurrentState() {
        val realPlantMode = RealPlantModeState(enabled = true).logAction(
            action = RealPlantCareAction.WATERED,
            loggedAtEpochMillis = System.currentTimeMillis(),
            zoneId = ZoneId.systemDefault(),
        )

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = realPlantMode,
        )

        assertEquals("Basil", snapshot.starter.companion.species)
        assertEquals("Thirsty", snapshot.mood)
        assertEquals("Stable", snapshot.health)
        assertTrue(snapshot.realPlantSummary?.contains("Real-plant mode is on") == true)
    }

    @Test
    fun createSnapshot_omitsRealPlantSummaryWhenModeIsOff() {
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(enabled = false),
        )

        assertNull(snapshot.realPlantSummary)
    }

    @Test
    fun replyTo_usesStateAwareCareAdvice() {
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(enabled = false),
        )

        val reply = CompanionChatEngine.replyTo("Should I water you?", snapshot)

        assertEquals(CompanionChatIntent.CARE_ADVICE, reply.intent)
        assertTrue(reply.reply.contains("hydration is my lowest stat at 30"))
        assertTrue(reply.reply.contains(weatherAdvice.starterAdvice))
        assertTrue(reply.suggestionChips.contains("Should I water you?"))
    }

    @Test
    fun replyTo_reportsGrowthProgress() {
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        val reply = CompanionChatEngine.replyTo("How are you growing?", snapshot)

        assertEquals(CompanionChatIntent.GROWTH_QUESTION, reply.intent)
        assertTrue(reply.reply.contains(growthState.currentStage.title))
        assertTrue(reply.reply.contains("% of the way there"))
    }

    @Test
    fun replyTo_statusCheckStandsOnItsOwnWithoutRealPlantMode() {
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(enabled = false),
        )

        val reply = CompanionChatEngine.replyTo("How are you feeling?", snapshot)

        assertTrue(reply.reply.contains("Best quick win: watering."))
        assertTrue(reply.reply.contains("I’m currently ${growthState.currentStage.title.lowercase()}."))
        assertTrue(reply.reply.contains("You’ve got"))
        assertFalse(reply.reply.contains("Real-plant mode is off"))
    }

    @Test
    fun updatedMemoryFor_keepsRecentExchangesBounded() {
        var snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        repeat(6) { index ->
            val reply = CompanionChatEngine.replyTo("How are you feeling $index?", snapshot)
            val updatedMemory = CompanionChatEngine.updatedMemoryFor(reply, snapshot)
            snapshot = snapshot.copy(recentConversationMemory = updatedMemory)
        }

        assertEquals(CompanionConversationMemory.MAX_MESSAGES, snapshot.recentConversationMemory.messages.size)
        assertTrue(snapshot.recentConversationMemory.messages.first().text.contains("How are you feeling 2?"))
    }

    @Test
    fun proactiveCheckIn_prioritizesCurrentState() {
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        val proactive = CompanionChatEngine.proactiveCheckIn(snapshot)

        assertTrue(proactive.emotionLabel.isNotBlank())
        assertTrue(proactive.familiarityLabel.isNotBlank())
        assertTrue(proactive.bubble.isNotBlank())
        assertTrue(proactive.suggestionChips.isNotEmpty())
        assertTrue(proactive.suggestionChips.any { it.contains("water", ignoreCase = true) || it.contains("streak", ignoreCase = true) || it.contains("mission", ignoreCase = true) || it.contains("today", ignoreCase = true) })
    }

    @Test
    fun createSnapshot_addsDeterministicEmotionAndRelationshipContext() {
        val memory = CompanionConversationMemory()
            .withExchange("How are you growing?", CompanionChatIntent.GROWTH_QUESTION, "Pretty well.")
            .withExchange("And now?", CompanionChatIntent.GROWTH_QUESTION, "Still moving.")

        val richMissionSet = DailyMissionProgress(
            missionDate = LocalDate.now().toString(),
            completedCareActionsToday = 1,
            completedLessonsToday = 1,
            currentStreak = 3,
            longestStreak = 3,
            lastCompletedDate = LocalDate.now().minusDays(1).toString(),
        ).resolveForToday(LocalDate.now(), LessonProgress(), careState)

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState.copy(hydration = 70),
            growthStageState = growthState,
            dailyMissionSet = richMissionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
            recentConversationMemory = memory,
        )

        assertTrue(snapshot.relationship.familiarity == CompanionFamiliarity.WARM || snapshot.relationship.familiarity == CompanionFamiliarity.CLOSE)
        assertTrue(snapshot.continuity.emotion in CompanionEmotion.entries)
        assertTrue(snapshot.relationship.summary.isNotBlank())
        assertTrue(snapshot.continuity.emotionalSummary.isNotBlank())
    }

    @Test
    fun createSnapshot_prefersGrowthUnlockOverGenericStreakSignals() {
        val unlockState = growthState.copy(newlyUnlocked = true)
        val streakingMissionSet = DailyMissionProgress(
            missionDate = LocalDate.now().toString(),
            currentStreak = 2,
            longestStreak = 2,
            lastCompletedDate = LocalDate.now().minusDays(1).toString(),
        ).resolveForToday(LocalDate.now(), LessonProgress(), careState.copy(hydration = 72, sunlight = 84, nutrition = 70))

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState.copy(hydration = 72, sunlight = 84, nutrition = 70),
            growthStageState = unlockState,
            dailyMissionSet = streakingMissionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        assertEquals(CompanionContinuityEvent.GROWTH_UNLOCKED, snapshot.continuity.primaryEvent)
        assertEquals(CompanionEmotion.PROUD, snapshot.continuity.emotion)
    }

    @Test
    fun createSnapshot_marksHealthyPartialStreaksAsContinuingNotAtRisk() {
        val progressingMissionSet = DailyMissionProgress(
            missionDate = LocalDate.now().toString(),
            completedCareActionsToday = 1,
            currentStreak = 3,
            longestStreak = 3,
            lastCompletedDate = LocalDate.now().minusDays(1).toString(),
        ).resolveForToday(LocalDate.now(), LessonProgress(), careState.copy(hydration = 72, sunlight = 84, nutrition = 70))

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState.copy(hydration = 72, sunlight = 84, nutrition = 70),
            growthStageState = growthState,
            dailyMissionSet = progressingMissionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        assertEquals(CompanionContinuityEvent.STREAK_CONTINUING, snapshot.continuity.primaryEvent)
        assertEquals(CompanionEmotion.CALM, snapshot.continuity.emotion)
    }

    @Test
    fun createSnapshot_doesNotMarkLowCareAsStreakRiskWhenNoStreakExists() {
        val noStreakMissionSet = DailyMissionProgress(
            missionDate = LocalDate.of(2026, 7, 10).toString(),
            currentStreak = 0,
            longestStreak = 0,
            lastCompletedDate = null,
        ).resolveForToday(LocalDate.of(2026, 7, 10), LessonProgress(totalXp = 0), careState.copy(hydration = 35, sunlight = 60, nutrition = 60))

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState.copy(hydration = 35, sunlight = 60, nutrition = 60),
            growthStageState = resolveGrowthStageState(starter.id, LessonProgress(totalXp = 0), careState.copy(hydration = 35, sunlight = 60, nutrition = 60)),
            dailyMissionSet = noStreakMissionSet,
            weatherSnapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 7, 10)),
            weatherAdvice = WeatherAdviceGenerator.adviceFor(starter, SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 7, 10))),
            realPlantModeState = RealPlantModeState(),
        )

        assertTrue(snapshot.continuity.primaryEvent != CompanionContinuityEvent.STREAK_AT_RISK)
    }

    @Test
    fun createSnapshot_doesNotTreatThresholdOnlyCompletionAsContinuingProgress() {
        val thresholdOnlyMissionSet = DailyMissionProgress(
            missionDate = LocalDate.of(2026, 7, 10).toString(),
            currentStreak = 3,
            longestStreak = 3,
            lastCompletedDate = LocalDate.of(2026, 7, 9).toString(),
        ).resolveForToday(LocalDate.of(2026, 7, 10), LessonProgress(totalXp = 0), careState.copy(hydration = 80, sunlight = 80, nutrition = 80))

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState.copy(hydration = 80, sunlight = 80, nutrition = 80),
            growthStageState = resolveGrowthStageState(starter.id, LessonProgress(totalXp = 0), careState.copy(hydration = 80, sunlight = 80, nutrition = 80)),
            dailyMissionSet = thresholdOnlyMissionSet,
            weatherSnapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 7, 10)),
            weatherAdvice = WeatherAdviceGenerator.adviceFor(starter, SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 7, 10))),
            realPlantModeState = RealPlantModeState(),
        )

        assertTrue(snapshot.continuity.primaryEvent != CompanionContinuityEvent.STREAK_CONTINUING)
    }

    @Test
    fun proactiveCheckIn_zeroStreakBaselineDoesNotUseContinuingStreakTone() {
        val baselineCareState = PlantCareState(hydration = 46, sunlight = 46, nutrition = 46)
        val baselineGrowthState = resolveGrowthStageState(
            starterId = starter.id,
            progress = LessonProgress(totalXp = 0),
            careState = baselineCareState,
        )
        val baselineMissionSet = DailyMissionProgress(
            missionDate = LocalDate.of(2026, 7, 10).toString(),
            currentStreak = 0,
            longestStreak = 0,
            lastCompletedDate = null,
        ).resolveForToday(LocalDate.of(2026, 7, 10), LessonProgress(totalXp = 0), baselineCareState)
        val baselineWeatherSnapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 7, 10))
        val baselineWeatherAdvice = WeatherAdviceGenerator.adviceFor(starter, baselineWeatherSnapshot)

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = baselineCareState,
            growthStageState = baselineGrowthState,
            dailyMissionSet = baselineMissionSet,
            weatherSnapshot = baselineWeatherSnapshot,
            weatherAdvice = baselineWeatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        val proactive = CompanionChatEngine.proactiveCheckIn(snapshot)

        assertEquals(0, baselineMissionSet.completedCount)
        assertFalse(proactive.bubble.contains("Our streak", ignoreCase = true))
        assertFalse(proactive.bubble.contains("streak is holding", ignoreCase = true))
        assertFalse(proactive.bubble.contains("steady this pace", ignoreCase = true))
        assertFalse(proactive.bubble.contains("streak", ignoreCase = true))
        assertTrue(snapshot.continuity.primaryEvent != CompanionContinuityEvent.STREAK_CONTINUING)
    }

    @Test
    fun createSnapshot_prefersGrowthProgressOverStreakToneForNoStreakBaselineNearNextStage() {
        val baselineCareState = PlantCareState(hydration = 60, sunlight = 60, nutrition = 60)
        val baselineGrowthState = resolveGrowthStageState(
            starterId = starter.id,
            progress = LessonProgress(totalXp = 18),
            careState = baselineCareState,
        )
        val baselineMissionSet = DailyMissionProgress(
            missionDate = LocalDate.of(2026, 7, 10).toString(),
            currentStreak = 0,
            longestStreak = 0,
            lastCompletedDate = null,
        ).resolveForToday(LocalDate.of(2026, 7, 10), LessonProgress(totalXp = 18), baselineCareState)
        val baselineWeatherSnapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 7, 10))
        val baselineWeatherAdvice = WeatherAdviceGenerator.adviceFor(starter, baselineWeatherSnapshot)

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = baselineCareState,
            growthStageState = baselineGrowthState,
            dailyMissionSet = baselineMissionSet,
            weatherSnapshot = baselineWeatherSnapshot,
            weatherAdvice = baselineWeatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        assertEquals(0, baselineMissionSet.completedCount)
        assertEquals(CompanionContinuityEvent.GROWTH_PROGRESS, snapshot.continuity.primaryEvent)
    }

    @Test
    fun createSnapshot_doesNotInventContinuingStreakForBaselineNoProgressState() {
        val healthyCareState = careState.copy(hydration = 72, sunlight = 84, nutrition = 70)
        val baselineGrowthState = resolveGrowthStageState(
            starterId = starter.id,
            progress = LessonProgress(totalXp = 0),
            careState = healthyCareState,
        )
        val neutralWeatherSnapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 7, 10))
        val noProgressMissionSet = DailyMissionProgress(
            missionDate = LocalDate.now().toString(),
            currentStreak = 0,
            longestStreak = 0,
        ).resolveForToday(LocalDate.now(), LessonProgress(), healthyCareState)

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = healthyCareState,
            growthStageState = baselineGrowthState,
            dailyMissionSet = noProgressMissionSet,
            weatherSnapshot = neutralWeatherSnapshot,
            weatherAdvice = WeatherAdviceGenerator.adviceFor(starter, neutralWeatherSnapshot),
            realPlantModeState = RealPlantModeState(),
        )

        assertEquals(CompanionContinuityEvent.GROWTH_PROGRESS, snapshot.continuity.primaryEvent)
        assertEquals(CompanionEmotion.CALM, snapshot.continuity.emotion)
    }

    @Test
    fun replyTo_usesContinuityLeadForFollowUps() {
        val firstReply = CompanionChatEngine.replyTo(
            "How are you growing?",
            CompanionChatEngine.createSnapshot(
                starter = starter,
                careState = careState,
                growthStageState = growthState,
                dailyMissionSet = missionSet,
                weatherSnapshot = weatherSnapshot,
                weatherAdvice = weatherAdvice,
                realPlantModeState = RealPlantModeState(),
            ),
        )
        val memory = CompanionConversationMemory().withExchange(firstReply.userMessage, firstReply.intent, firstReply.reply)
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
            recentConversationMemory = memory,
        )

        val followUp = CompanionChatEngine.replyTo("and now?", snapshot)

        assertEquals(CompanionChatIntent.GROWTH_QUESTION, followUp.intent)
        assertTrue(followUp.reply.contains("Let’s keep following that thread.") || followUp.reply.contains("I’m still lit up") || followUp.reply.contains("I want to keep pulling"))
    }

    @Test
    fun replyTo_localizesGermanAndKoreanChatCopy() {
        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet,
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(enabled = false),
            languageTag = "de",
        )

        val germanReply = CompanionChatEngine.replyTo("", snapshot, languageTag = "de")
        val koreanReply = CompanionChatEngine.replyTo("", snapshot, languageTag = "ko")

        assertEquals("Wie geht es dir?", germanReply.userMessage)
        assertTrue(germanReply.suggestionChips.contains("Soll ich dich gießen?"))
        assertTrue(germanReply.reply.contains("durstig"))

        assertEquals("지금 기분이 어때?", koreanReply.userMessage)
        assertTrue(koreanReply.suggestionChips.contains("물 줘야 해?"))
        assertTrue(koreanReply.reply.contains("목말라요"))
    }

    @Test
    fun suggestionChipsForIntent_areDistinctAndBoundedWhenMultipleSignalsCompete() {
        val denseMissionSet = DailyMissionProgress(
            missionDate = LocalDate.now().toString(),
            completedCareActionsToday = 0,
            completedLessonsToday = 0,
            currentStreak = 2,
            longestStreak = 2,
            lastCompletedDate = LocalDate.now().minusDays(1).toString(),
        ).resolveForToday(LocalDate.now(), LessonProgress(totalXp = 18), careState)

        val snapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState.copy(hydration = 35),
            growthStageState = growthState,
            dailyMissionSet = denseMissionSet,
            weatherSnapshot = SeasonalWeatherProvider.snapshotFor("berlin", LocalDate.of(2026, 10, 10)),
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
        )

        val chips = CompanionChatEngine.suggestionChipsForIntent(snapshot, CompanionChatIntent.STATUS_CHECK, "en")

        assertEquals(chips.distinct(), chips)
        assertTrue(chips.size <= 4)
        assertTrue(chips.contains("Should I water you?"))
        assertTrue(chips.contains("How do I save the streak?"))
    }

    @Test
    fun createSnapshot_familiarityThresholdsStayDeterministicAtBoundaries() {
        val newSnapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet.copy(currentStreak = 0),
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
            recentConversationMemory = CompanionConversationMemory(),
        )
        val warmSnapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet.copy(currentStreak = 0),
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
            recentConversationMemory = CompanionConversationMemory()
                .withExchange("How are you?", CompanionChatIntent.STATUS_CHECK, "Doing okay.")
                .withExchange("Need water?", CompanionChatIntent.CARE_ADVICE, "A little."),
        )
        val closeSnapshot = CompanionChatEngine.createSnapshot(
            starter = starter,
            careState = careState,
            growthStageState = growthState,
            dailyMissionSet = missionSet.copy(currentStreak = 2),
            weatherSnapshot = weatherSnapshot,
            weatherAdvice = weatherAdvice,
            realPlantModeState = RealPlantModeState(),
            recentConversationMemory = CompanionConversationMemory()
                .withExchange("How are you?", CompanionChatIntent.STATUS_CHECK, "Doing okay.")
                .withExchange("Need water?", CompanionChatIntent.CARE_ADVICE, "A little.")
                .withExchange("How are you growing?", CompanionChatIntent.GROWTH_QUESTION, "Steadily.")
                .withExchange("What mission first?", CompanionChatIntent.MISSION_HELP, "Lesson first."),
        )

        assertEquals(CompanionFamiliarity.NEW, newSnapshot.relationship.familiarity)
        assertEquals(CompanionFamiliarity.WARM, warmSnapshot.relationship.familiarity)
        assertEquals(CompanionFamiliarity.CLOSE, closeSnapshot.relationship.familiarity)
    }
}
