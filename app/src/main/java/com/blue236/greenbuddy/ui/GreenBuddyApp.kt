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
import com.blue236.greenbuddy.model.GreenBuddyUiState
import com.blue236.greenbuddy.model.LessonCatalog
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
fun GreenBuddyApp(
    initialTab: Tab = Tab.HOME,
    viewModel: GreenBuddyViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        viewModel.onAppVisible()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(initialTab) {
        viewModel.selectTab(initialTab)
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.onAppVisible()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    GreenBuddyAppContent(
        uiState = uiState,
        onSelectTab = viewModel::selectTab,
        onSelectStarter = viewModel::selectStarter,
        onContinueOnboarding = viewModel::completeOnboarding,
        onSubmitLessonAnswer = viewModel::submitCurrentLessonAnswer,
        onPerformCareAction = viewModel::performCareAction,
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
) {
    val lessons = LessonCatalog.forSpecies(uiState.selectedStarter.companion.species)
    val currentLesson = uiState.lessonProgress.currentLessonOrNull(lessons)

    if (!uiState.onboardingComplete) {
        OnboardingScreen(
            options = uiState.starterOptions,
            selectedStarterId = uiState.selectedStarterId,
            currentLessonTitle = currentLesson?.title ?: "Starter setup",
            onSelectStarter = onSelectStarter,
            onContinue = onContinueOnboarding,
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        icon = { Text(if (uiState.selectedTab == tab) "●" else "○") },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding).padding(horizontal = 0.dp)
        when (uiState.selectedTab) {
            Tab.HOME -> HomeScreen(
                modifier = modifier,
                starter = uiState.selectedStarter,
                lessons = lessons,
                progress = uiState.lessonProgress,
                careState = uiState.plantCareState,
                onPerformCareAction = onPerformCareAction,
            )
            Tab.LEARN -> LearnScreen(
                modifier = modifier,
                starter = uiState.selectedStarter,
                lessons = lessons,
                progress = uiState.lessonProgress,
                onSubmitAnswer = onSubmitLessonAnswer,
            )
            Tab.DEX -> DexScreen(modifier = modifier, options = uiState.starterOptions, selectedStarterId = uiState.selectedStarterId)
            Tab.PROFILE -> ProfileScreen(
                modifier = modifier,
                starter = uiState.selectedStarter,
                lessons = lessons,
                progress = uiState.lessonProgress,
                careState = uiState.plantCareState,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GreenBuddyAppPreview() {
    GreenBuddyTheme {
        GreenBuddyAppContent(
            uiState = GreenBuddyUiState(
                onboardingComplete = true,
                starterOptions = StarterPlants.options,
            ),
            onSelectTab = {},
            onSelectStarter = {},
            onContinueOnboarding = {},
            onSubmitLessonAnswer = { false },
            onPerformCareAction = {},
        )
    }
}
