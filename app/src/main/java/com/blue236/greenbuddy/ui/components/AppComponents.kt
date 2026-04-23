package com.blue236.greenbuddy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.StarterPlantOption
import com.blue236.greenbuddy.model.localizedCareTip
import com.blue236.greenbuddy.model.localizedSubtitle
import com.blue236.greenbuddy.model.localizedTitle

@Composable
fun StatCard(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                trailingContent?.invoke()
            }
            content()
        }
    }
}

@Composable
fun GreenBuddyCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cardModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

@Composable
fun GreenBuddyHeroCard(
    modifier: Modifier = Modifier,
    gradientStart: Color = MaterialTheme.colorScheme.primaryContainer,
    gradientEnd: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(gradientStart, gradientEnd))),
        ) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        trailingContent?.invoke()
    }
}

enum class GreenBuddyButtonVariant { Primary, Secondary, Ghost }

@Composable
fun GreenBuddyButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: GreenBuddyButtonVariant = GreenBuddyButtonVariant.Primary,
) {
    when (variant) {
        GreenBuddyButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
        GreenBuddyButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        GreenBuddyButtonVariant.Ghost -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = CircleShape,
            border = BorderStroke(0.dp, Color.Transparent),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun GreenBuddyChip(
    label: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
) {
    SuggestionChip(
        onClick = onClick ?: {},
        label = { Text(if (emoji != null) "$emoji $label" else label, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier,
        shape = CircleShape,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreenBuddyTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                }
            }
        },
        actions = { trailingContent?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@Composable
fun CareStatRow(label: String, value: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("$value%", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun StarterPlantCard(option: StarterPlantOption, selected: Boolean, onClick: () -> Unit) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 2.dp else 1.dp
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier.fillMaxWidth().border(borderWidth, borderColor, MaterialTheme.shapes.large).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(72.dp).background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Text(option.previewEmoji, style = MaterialTheme.typography.headlineLarge)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(option.localizedTitle(localeTag), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(option.localizedSubtitle(localeTag), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.lesson_focus, option.localizedCareTip(localeTag)),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Box(
                Modifier.size(32.dp).background(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (selected) "✓" else "",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
