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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.GreenBuddyUiState
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
import com.blue236.greenbuddy.ui.state.GreenBuddyViewModel
import com.blue236.greenbuddy.ui.theme.GreenBuddyTheme

@Composable
fun GreenBuddyApp(initialTab: Tab = Tab.HOME, viewModel: GreenBuddyViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) { viewModel.onAppVisible(); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    LaunchedEffect(initialTab) { viewModel.selectTab(initialTab) }
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_START) viewModel.onAppVisible() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    GreenBuddyAppContent(uiState, viewModel::selectTab, viewModel::selectStarter, viewModel::completeOnboarding, viewModel::submitCurrentLessonAnswer, viewModel::performCareAction, viewModel::acknowledgeGrowthStage, viewModel::purchaseCosmetic, viewModel::equipCosmetic, viewModel::setRealPlantModeEnabled, viewModel::logRealPlantCare)
}

@Composable
fun GreenBuddyAppContent(uiState: GreenBuddyUiState, onSelectTab: (Tab) -> Unit, onSelectStarter: (String) -> Unit, onContinueOnboarding: () -> Unit, onSubmitLessonAnswer: (Int) -> Boolean, onPerformCareAction: (CareAction) -> Unit, onAcknowledgeGrowthStage: () -> Unit, onPurchaseCosmetic: (CosmeticItem) -> Unit, onEquipCosmetic: (String) -> Unit, onSetRealPlantModeEnabled: (Boolean) -> Unit, onLogRealPlantCare: (RealPlantCareAction) -> Unit) {
    val lessons = LessonCatalog.forSpecies(uiState.selectedStarter.companion.species)
    val currentLesson = uiState.lessonProgress.currentLessonOrNull(lessons)
    if (!uiState.onboardingComplete) { OnboardingScreen(uiState.starterOptions, uiState.selectedStarterId, currentLesson?.title ?: "Starter setup", onSelectStarter, onContinueOnboarding); return }
    Scaffold(bottomBar = { NavigationBar { Tab.entries.forEach { tab -> NavigationBarItem(selected = uiState.selectedTab == tab, onClick = { onSelectTab(tab) }, icon = { Text(if (uiState.selectedTab == tab) "●" else "○") }, label = { Text(tab.label) }) } } }) { innerPadding ->
        val modifier = Modifier.padding(innerPadding).padding(horizontal = 0.dp)
        when (uiState.selectedTab) {
            Tab.HOME -> HomeScreen(modifier, uiState.selectedStarter, lessons, uiState.lessonProgress, uiState.plantCareState, uiState.dailyMissionSet, uiState.growthStageState, uiState.ownedStarterIds.size, uiState.rewardState, uiState.rewardFeedback, uiState.realPlantModeState, onPerformCareAction, onAcknowledgeGrowthStage, onSetRealPlantModeEnabled, onLogRealPlantCare)
            Tab.LEARN -> LearnScreen(modifier, uiState.selectedStarter, lessons, uiState.lessonProgress, uiState.plantCareState, onSubmitLessonAnswer)
            Tab.DEX -> DexScreen(modifier, uiState.inventoryEntries, onSelectStarter)
            Tab.PROFILE -> ProfileScreen(modifier, uiState.selectedStarter, lessons, uiState.lessonProgress, uiState.plantCareState, uiState.dailyMissionSet, uiState.growthStageState, uiState.ownedStarterIds.size, uiState.rewardState, uiState.realPlantModeState, onAcknowledgeGrowthStage, onPurchaseCosmetic, onEquipCosmetic)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GreenBuddyAppPreview() { GreenBuddyTheme { GreenBuddyAppContent(GreenBuddyUiState(onboardingComplete = true, starterOptions = StarterPlants.options), {}, {}, {}, { false }, {}, {}, {}, {}, {}, {}) } }
