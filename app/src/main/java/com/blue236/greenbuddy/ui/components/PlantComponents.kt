package com.blue236.greenbuddy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.PlantInventoryEntry
import com.blue236.greenbuddy.model.localizedGrowthTitle
import com.blue236.greenbuddy.model.localizedHealth
import com.blue236.greenbuddy.model.localizedMood
import com.blue236.greenbuddy.model.localizedRequirementSummary
import com.blue236.greenbuddy.model.localizedSubtitle
import com.blue236.greenbuddy.model.localizedTitle
import com.blue236.greenbuddy.model.resolveGrowthStageState
import com.blue236.greenbuddy.model.unlockRequirementFor

@Composable
fun PlantInventoryCard(
    entry: PlantInventoryEntry,
    ownedStarterIds: Set<String>,
    localeTag: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val option = entry.option
    val growthStageState = resolveGrowthStageState(option.id, entry.progress, entry.careState)
    val containerColor = when {
        entry.isActive -> MaterialTheme.colorScheme.primaryContainer
        entry.isOwned -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val borderModifier = if (entry.isActive) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .clickable(enabled = entry.isOwned) { onSelect(option.id) }
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    Modifier.size(64.dp).background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option.previewEmoji,
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (entry.isOwned) Color.Unspecified else Color.Unspecified.copy(alpha = 0.4f),
                    )
                }
                if (!entry.isOwned) {
                    Box(
                        Modifier.size(20.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🔒", fontSize = 10.sp)
                    }
                }
                if (entry.isActive) {
                    Box(
                        Modifier.size(16.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(option.localizedTitle(localeTag), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(option.localizedSubtitle(localeTag), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.isOwned) {
                    Text(
                        "${growthStageState.currentStage.emoji} ${growthStageState.currentStage.localizedGrowthTitle(localeTag)} · ${growthStageState.readinessPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    LinearProgressIndicator(
                        progress = { growthStageState.progressToNextStage },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        "XP ${entry.progress.totalXp} · ${entry.careState.localizedHealth(localeTag)} · ${entry.careState.localizedMood(localeTag)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        unlockRequirementFor(option, ownedStarterIds, localeTag),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                when {
                    entry.isActive -> stringResource(R.string.active)
                    entry.isOwned -> stringResource(R.string.switch_action)
                    else -> stringResource(R.string.locked)
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    entry.isActive -> MaterialTheme.colorScheme.primary
                    entry.isOwned -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

enum class QuizOptionState { Idle, Selected, Correct, Incorrect }

@Composable
fun QuizOptionTile(
    letterLabel: String,
    text: String,
    state: QuizOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = when (state) {
            QuizOptionState.Idle -> MaterialTheme.colorScheme.surfaceVariant
            QuizOptionState.Selected -> MaterialTheme.colorScheme.secondaryContainer
            QuizOptionState.Correct -> MaterialTheme.colorScheme.primaryContainer
            QuizOptionState.Incorrect -> MaterialTheme.colorScheme.errorContainer
        },
        animationSpec = tween(200),
        label = "quizBg",
    )
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            QuizOptionState.Idle -> Color.Transparent
            QuizOptionState.Selected -> MaterialTheme.colorScheme.secondary
            QuizOptionState.Correct -> MaterialTheme.colorScheme.primary
            QuizOptionState.Incorrect -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(200),
        label = "quizBorder",
    )
    val scale by animateFloatAsState(
        targetValue = if (state == QuizOptionState.Selected) 1.02f else 1.0f,
        animationSpec = spring(),
        label = "quizScale",
    )
    val labelColor = when (state) {
        QuizOptionState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
        QuizOptionState.Selected -> MaterialTheme.colorScheme.onSecondaryContainer
        QuizOptionState.Correct -> MaterialTheme.colorScheme.onPrimaryContainer
        QuizOptionState.Incorrect -> MaterialTheme.colorScheme.onErrorContainer
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .border(1.5.dp, borderColor, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .clickable(enabled = state == QuizOptionState.Idle || state == QuizOptionState.Selected, onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).background(
                when (state) {
                    QuizOptionState.Correct -> MaterialTheme.colorScheme.primary
                    QuizOptionState.Incorrect -> MaterialTheme.colorScheme.error
                    QuizOptionState.Selected -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                },
                CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                when (state) {
                    QuizOptionState.Correct -> "✓"
                    QuizOptionState.Incorrect -> "✗"
                    else -> letterLabel
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface,
            )
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = labelColor, modifier = Modifier.weight(1f))
    }
}

@Composable
fun LessonPathNode(
    index: Int,
    title: String,
    isCurrent: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(if (isCurrent) 32.dp else 24.dp).background(
                when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isCurrent -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
                CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (isCompleted) "✓" else "$index",
                style = if (isCurrent) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted || isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            title,
            style = if (isCurrent) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isCurrent -> MaterialTheme.colorScheme.onSurface
                isCompleted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
