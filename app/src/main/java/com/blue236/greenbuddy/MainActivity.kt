package com.blue236.greenbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.ui.theme.GreenBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreenBuddyTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GreenBuddyApp()
                }
            }
        }
    }
}

enum class Tab(val label: String) { HOME("Home"), LEARN("Learn"), DEX("Dex"), PROFILE("Profile") }

data class PlantCompanion(
    val name: String,
    val species: String,
    val stage: String,
    val hydration: Int,
    val sunlight: Int,
    val nutrition: Int,
    val mood: String,
    val lessonTitle: String,
    val nextEvolutionXp: Int,
    val currentXp: Int,
    val greeting: String,
    val careTip: String,
    val emoji: String,
)

data class StarterPlantOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val previewEmoji: String,
    val companion: PlantCompanion,
)

private val starterPlantOptions = listOf(
    StarterPlantOption(
        id = "monstera",
        title = "Monstera",
        subtitle = "Easygoing indoor starter",
        previewEmoji = "🌿",
        companion = PlantCompanion(
            name = "Leafling",
            species = "Monstera",
            stage = "Sprout",
            hydration = 72,
            sunlight = 81,
            nutrition = 46,
            mood = "Curious",
            lessonTitle = "Indirect light basics",
            nextEvolutionXp = 120,
            currentXp = 78,
            greeting = "Thanks for helping me learn about sunlight today!",
            careTip = "Rotate weekly for even leaf growth.",
            emoji = "🌿",
        )
    ),
    StarterPlantOption(
        id = "basil",
        title = "Basil",
        subtitle = "Fast-growing kitchen buddy",
        previewEmoji = "🌱",
        companion = PlantCompanion(
            name = "Pesto",
            species = "Basil",
            stage = "Seedling",
            hydration = 65,
            sunlight = 88,
            nutrition = 58,
            mood = "Energetic",
            lessonTitle = "Sun + water balance",
            nextEvolutionXp = 110,
            currentXp = 64,
            greeting = "A sunny windowsill and I’m ready to thrive.",
            careTip = "Pinch top leaves often to keep me bushy.",
            emoji = "🌱",
        )
    ),
    StarterPlantOption(
        id = "tomato",
        title = "Tomato",
        subtitle = "Rewarding fruiting challenge",
        previewEmoji = "🍅",
        companion = PlantCompanion(
            name = "Sunny",
            species = "Tomato",
            stage = "Starter",
            hydration = 78,
            sunlight = 92,
            nutrition = 61,
            mood = "Ambitious",
            lessonTitle = "Supporting fruiting plants",
            nextEvolutionXp = 140,
            currentXp = 82,
            greeting = "Give me support early and I’ll return the favor later.",
            careTip = "More sun means stronger stems and sweeter fruit.",
            emoji = "🍅",
        )
    )
)

@Composable
fun GreenBuddyApp() {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var selectedStarterId by rememberSaveable { mutableStateOf(starterPlantOptions.first().id) }
    var onboardingComplete by rememberSaveable { mutableStateOf(false) }

    val selectedStarter = starterPlantOptions.first { it.id == selectedStarterId }

    if (!onboardingComplete) {
        OnboardingScreen(
            options = starterPlantOptions,
            selectedStarterId = selectedStarterId,
            onSelectStarter = { selectedStarterId = it },
            onContinue = { onboardingComplete = true }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(if (selectedTab == tab) "●" else "○") },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            Tab.HOME -> HomeScreen(Modifier.padding(innerPadding), selectedStarter.companion)
            Tab.LEARN -> LearnScreen(Modifier.padding(innerPadding), selectedStarter.companion)
            Tab.DEX -> DexScreen(Modifier.padding(innerPadding), selectedStarterId)
            Tab.PROFILE -> ProfileScreen(Modifier.padding(innerPadding), selectedStarter)
        }
    }
}

@Composable
fun OnboardingScreen(
    options: List<StarterPlantOption>,
    selectedStarterId: String,
    onSelectStarter: (String) -> Unit,
    onContinue: () -> Unit,
) {
    val selectedOption = options.first { it.id == selectedStarterId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text("Welcome to GreenBuddy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Build your first plant companion in under a minute. Pick a starter and begin the daily care + learning loop.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How it works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("1. Choose a starter plant")
                Text("2. Learn one short care concept each day")
                Text("3. Keep your companion happy and help it grow")
            }
        }

        Text("Choose your starter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                StarterPlantCard(
                    option = option,
                    selected = option.id == selectedStarterId,
                    onClick = { onSelectStarter(option.id) }
                )
            }
        }

        StatCard("Your current pick") {
            Text("${selectedOption.companion.name} · ${selectedOption.title}", fontWeight = FontWeight.SemiBold)
            Text(selectedOption.subtitle)
            Text("First lesson: ${selectedOption.companion.lessonTitle}")
        }

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start with ${selectedOption.title}")
        }
    }
}

@Composable
fun StarterPlantCard(
    option: StarterPlantOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val containerColor = if (selected) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFC8E6C9), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(option.previewEmoji, style = MaterialTheme.typography.headlineMedium)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(option.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(option.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Lesson focus: ${option.companion.lessonTitle}")
            }
            Text(if (selected) "Selected" else "Pick")
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, plant: PlantCompanion) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("GreenBuddy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Your starter companion is now tuned to ${plant.species} care.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${plant.name} · ${plant.species}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Stage: ${plant.stage} · Mood: ${plant.mood}")
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFFC8E6C9), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(plant.emoji, style = MaterialTheme.typography.displayMedium)
                }
                Text("\"${plant.greeting}\"")
            }
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Today’s lesson", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(plant.lessonTitle)
                Text("Starter tip: ${plant.careTip}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        StatCard("Care status") {
            CareStatRow("Hydration", plant.hydration)
            CareStatRow("Sunlight", plant.sunlight)
            CareStatRow("Nutrition", plant.nutrition)
        }

        StatCard("Growth progress") {
            Text("${plant.currentXp} / ${plant.nextEvolutionXp} XP to Young Plant")
            Spacer(Modifier.height(8.dp))
            Text("Daily loop: Learn → Quiz → Care → Reward")
        }
    }
}

@Composable
fun LearnScreen(modifier: Modifier = Modifier, plant: PlantCompanion) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Lesson prototype", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StatCard("Starter focus") {
            Text("Current companion: ${plant.name} the ${plant.species}")
            Text("Today’s concept: ${plant.lessonTitle}")
        }
        StatCard("Card 1") {
            Text(
                when (plant.species) {
                    "Monstera" -> "Monsteras prefer bright indirect light rather than harsh direct afternoon sun."
                    "Basil" -> "Basil grows best with steady moisture and plenty of sun, especially near a bright window."
                    else -> "Tomatoes need lots of direct sun and consistent feeding to support flowers and fruit."
                }
            )
        }
        StatCard("Quiz") {
            Text("What should you prioritize first for your ${plant.species} starter?")
            Spacer(Modifier.height(8.dp))
            Text(
                when (plant.species) {
                    "Monstera" -> "A. Harsh balcony sun\nB. Bright indirect light\nC. Very low light"
                    "Basil" -> "A. Sun + regular pinching\nB. Total shade\nC. Water once a month"
                    else -> "A. Deep shade\nB. Support + full sun\nC. No watering"
                }
            )
            Spacer(Modifier.height(8.dp))
            Text("Correct answer is the middle healthy habit for your starter.")
        }
        StatCard("Reward") { Text("On correct answer: XP +20, hydration +5, mood +8") }
    }
}

@Composable
fun DexScreen(modifier: Modifier = Modifier, selectedStarterId: String) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PlantDex", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        starterPlantOptions.forEach { option ->
            val prefix = if (option.id == selectedStarterId) "★" else "○"
            Card { Text("$prefix ${option.title} · ${option.subtitle}", modifier = Modifier.padding(16.dp)) }
        }
    }
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, starter: StarterPlantOption) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        StatCard("Progress") {
            Text("Level 1")
            Text("Streak 1 day")
            Text("Plants unlocked 3")
            Text("Coins 40")
        }
        StatCard("Starter setup") {
            Text("Chosen starter: ${starter.title}")
            Text("Companion: ${starter.companion.name}")
            Text("Focus: ${starter.companion.lessonTitle}")
        }
        StatCard("Roadmap focus") {
            Text("MVP: onboarding, daily lesson, care loop, growth state, PlantDex")
        }
    }
}

@Composable
fun StatCard(title: String, content: @Composable () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
fun CareStatRow(label: String, value: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text("$value%")
    }
}
