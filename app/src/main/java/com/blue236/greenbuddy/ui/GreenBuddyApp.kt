package com.blue236.greenbuddy.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.AppLanguage
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.hapticConstant
import com.blue236.greenbuddy.model.LessonCatalog
import com.blue236.greenbuddy.model.RealPlantCareAction
import com.blue236.greenbuddy.model.StarterPlants
import com.blue236.greenbuddy.model.Tab
import com.blue236.greenbuddy.model.currentLessonOrNull
import com.blue236.greenbuddy.ui.screens.DexScreen
import com.blue236.greenbuddy.ui.screens.HomeScreen
import com.blue236.greenbuddy.ui.screens.LearnScreen
import com.blue236.greenbuddy.ui.screens.OnboardingScreen
import com.blue236.greenbuddy.ui.screens.ProfileScreen
import com.blue236.greenbuddy.ui.screens.SettingsScreen
import com.blue236.greenbuddy.ui.state.GreenBuddyViewModel
import com.blue236.greenbuddy.ui.theme.GreenBuddyTheme

private val bottomNavigationTabs = listOf(Tab.HOME, Tab.LEARN, Tab.DEX, Tab.PROFILE)

@Composable
fun GreenBuddyApp(initialTab: Tab = Tab.HOME, viewModel: GreenBuddyViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        viewModel.onAppVisible()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(initialTab) { viewModel.selectTab(initialTab) }
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_START) viewModel.onAppVisible() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    GreenBuddyAppContent(
        uiState,
        viewModel::selectTab,
        viewModel::selectStarter,
        viewModel::completeOnboarding,
        viewModel::submitCurrentLessonAnswer,
        viewModel::performCareAction,
        viewModel::clearFeedbackEvent,
        viewModel::acknowledgeGrowthStage,
        viewModel::purchaseCosmetic,
        viewModel::equipCosmetic,
        viewModel::setRealPlantModeEnabled,
        viewModel::logRealPlantCare,
        viewModel::setSelectedWeatherCity,
        viewModel::setAppLanguage,
    )
}

@Composable
fun GreenBuddyAppContent(
    uiState: GreenBuddyUiState,
    onSelectTab: (Tab) -> Unit,
    onSelectStarter: (String) -> Unit,
    onContinueOnboarding: () -> Unit,
    onSubmitLessonAnswer: (Int) -> Boolean,
    onPerformCareAction: (CareAction) -> Unit,
    onClearFeedbackEvent: (Long) -> Unit,
    onAcknowledgeGrowthStage: () -> Unit,
    onPurchaseCosmetic: (CosmeticItem) -> Unit,
    onEquipCosmetic: (String) -> Unit,
    onSetRealPlantModeEnabled: (Boolean) -> Unit,
    onLogRealPlantCare: (RealPlantCareAction) -> Unit,
    onSetSelectedWeatherCity: (String) -> Unit,
    onSetAppLanguage: (AppLanguage) -> Unit,
) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val view = LocalView.current
    val lessons = LessonCatalog.forSpecies(uiState.selectedStarter.companion.species, localeTag)
    LaunchedEffect(uiState.feedbackEvent?.id) {
        uiState.feedbackEvent?.let { event ->
            view.performHapticFeedback(event.type.hapticConstant())
            onClearFeedbackEvent(event.id)
        }
    }
    val currentLesson = uiState.lessonProgress.currentLessonOrNull(lessons)
    if (!uiState.onboardingComplete) {
        OnboardingScreen(
            uiState.starterOptions,
            uiState.selectedStarterId,
            currentLesson?.title ?: stringResource(R.string.starter_setup),
            onSelectStarter,
            onContinueOnboarding,
        )
        return
    }
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavigationTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        icon = { Text(if (uiState.selectedTab == tab) "●" else "○") },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding).padding(horizontal = 0.dp)
        when (uiState.selectedTab) {
            Tab.HOME -> HomeScreen(
                modifier,
                uiState.selectedStarter,
                lessons,
                uiState.lessonProgress,
                uiState.plantCareState,
                uiState.dailyMissionSet,
                uiState.growthStageState,
                uiState.ownedStarterIds.size,
                uiState.rewardState,
                uiState.rewardFeedback,
                uiState.realPlantModeState,
                uiState.weatherSnapshot,
                uiState.weatherAdvice,
                uiState.companionStateSnapshot,
                onPerformCareAction,
                onAcknowledgeGrowthStage,
                onSetRealPlantModeEnabled,
                onLogRealPlantCare,
            )
            Tab.LEARN -> LearnScreen(modifier, uiState.selectedStarter, lessons, uiState.lessonProgress, uiState.plantCareState, onSubmitLessonAnswer)
            Tab.DEX -> DexScreen(modifier, uiState.inventoryEntries, uiState.ownedStarterIds, onSelectStarter)
            Tab.PROFILE -> ProfileScreen(
                modifier,
                uiState.selectedStarter,
                uiState.lessonProgress,
                uiState.dailyMissionSet,
                uiState.growthStageState,
                uiState.ownedStarterIds.size,
                uiState.rewardState,
                uiState.realPlantModeState,
                onOpenSettings = { onSelectTab(Tab.SETTINGS) },
                onAcknowledgeGrowthStage = onAcknowledgeGrowthStage,
                onPurchaseCosmetic = onPurchaseCosmetic,
                onEquipCosmetic = onEquipCosmetic,
            )
            Tab.SETTINGS -> SettingsScreen(
                modifier,
                uiState.weatherSnapshot.city.id,
                uiState.appLanguage,
                onNavigateBack = { onSelectTab(Tab.PROFILE) },
                onSetSelectedWeatherCity = onSetSelectedWeatherCity,
                onSetAppLanguage = onSetAppLanguage,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GreenBuddyAppPreview() {
    GreenBuddyTheme {
        GreenBuddyAppContent(GreenBuddyUiState(onboardingComplete = true, starterOptions = StarterPlants.options), {}, {}, {}, { false }, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

private val Tab.labelRes: Int
    get() = when (this) {
        Tab.HOME -> R.string.tab_home
        Tab.LEARN -> R.string.tab_learn
        Tab.DEX -> R.string.tab_dex
        Tab.PROFILE -> R.string.tab_profile
        Tab.SETTINGS -> R.string.tab_settings
    }
