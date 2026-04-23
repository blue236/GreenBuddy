package com.blue236.greenbuddy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blue236.greenbuddy.model.CareAction
import com.blue236.greenbuddy.model.CompanionEmotion
import com.blue236.greenbuddy.ui.theme.GreenBuddyColors

@Composable
fun CompanionAvatarBubble(
    emoji: String,
    emotion: CompanionEmotion,
    modifier: Modifier = Modifier,
    size: Int = 72,
) {
    val ringColor by animateColorAsState(
        targetValue = when (emotion) {
            CompanionEmotion.PROUD, CompanionEmotion.EXCITED -> GreenBuddyColors.leafGold
            CompanionEmotion.CURIOUS -> MaterialTheme.colorScheme.tertiary
            CompanionEmotion.CALM -> MaterialTheme.colorScheme.secondary
            CompanionEmotion.WORRIED -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(600),
        label = "emotionRing",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "companionBob")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bobY",
    )

    Box(
        modifier = modifier
            .offset(y = bobOffset.dp)
            .size(size.dp)
            .border(3.dp, ringColor, CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size * 0.5f).sp)
    }
}

@Composable
fun EmotionBanner(
    emotion: CompanionEmotion,
    emotionLabel: String,
    familiarityLabel: String,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (emotion) {
        CompanionEmotion.PROUD, CompanionEmotion.EXCITED -> MaterialTheme.colorScheme.primaryContainer
        CompanionEmotion.CURIOUS -> MaterialTheme.colorScheme.tertiaryContainer
        CompanionEmotion.CALM -> MaterialTheme.colorScheme.secondaryContainer
        CompanionEmotion.WORRIED -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (emotion) {
        CompanionEmotion.PROUD, CompanionEmotion.EXCITED -> MaterialTheme.colorScheme.onPrimaryContainer
        CompanionEmotion.CURIOUS -> MaterialTheme.colorScheme.onTertiaryContainer
        CompanionEmotion.CALM -> MaterialTheme.colorScheme.onSecondaryContainer
        CompanionEmotion.WORRIED -> MaterialTheme.colorScheme.onErrorContainer
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "● $emotionLabel",
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "● $familiarityLabel",
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun CareActionButton(
    action: CareAction,
    emoji: String,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val labelColor = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 24.sp)
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
        )
    }
}

@Composable
fun CareStatBar(
    label: String,
    emoji: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    val trackColor = when {
        value <= 33 -> MaterialTheme.colorScheme.errorContainer
        value <= 66 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val progressColor = when {
        value <= 33 -> MaterialTheme.colorScheme.error
        value <= 66 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 14.sp)
                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            Text("$value%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = progressColor)
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = progressColor,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
        )
    }
}
