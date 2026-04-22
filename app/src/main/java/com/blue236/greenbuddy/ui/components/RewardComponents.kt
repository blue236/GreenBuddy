package com.blue236.greenbuddy.ui.components

import androidx.compose.ui.res.stringResource
import com.blue236.greenbuddy.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blue236.greenbuddy.model.CosmeticItem
import com.blue236.greenbuddy.model.RewardState
import com.blue236.greenbuddy.model.localizedDescription
import com.blue236.greenbuddy.model.localizedName
import com.blue236.greenbuddy.ui.theme.GreenBuddyColors

@Composable
fun LeafTokenDisplay(
    amount: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🍃", fontSize = if (large) 24.sp else 16.sp)
        Text(
            "$amount",
            style = if (large) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GreenBuddyColors.leafGold,
        )
    }
}

@Composable
fun WalletHeader(
    leafTokens: Int,
    equippedCosmetic: CosmeticItem?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LeafTokenDisplay(amount = leafTokens, large = true)
        equippedCosmetic?.let {
            Text(
                "${it.emoji} ${it.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

enum class CosmeticStatus { Unowned, Affordable, Owned, Equipped }

@Composable
fun CosmeticShopCard(
    item: CosmeticItem,
    rewardState: RewardState,
    localeTag: String,
    onPurchase: () -> Unit,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUnlocked = item.id in rewardState.unlockedCosmeticIds
    val isEquipped = rewardState.equippedCosmeticId == item.id
    val tokensNeeded = rewardState.tokensNeededFor(item)
    val isAffordable = tokensNeeded == 0 && !isUnlocked

    val status = when {
        isEquipped -> CosmeticStatus.Equipped
        isUnlocked -> CosmeticStatus.Owned
        isAffordable -> CosmeticStatus.Affordable
        else -> CosmeticStatus.Unowned
    }

    val cardBorder = if (status == CosmeticStatus.Affordable) {
        Modifier.border(2.dp, GreenBuddyColors.leafGold, MaterialTheme.shapes.medium)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(cardBorder)
            .background(
                when (status) {
                    CosmeticStatus.Equipped -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    CosmeticStatus.Affordable -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                MaterialTheme.shapes.medium,
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, fontSize = 24.sp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.localizedName(localeTag), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(item.localizedDescription(localeTag), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🍃", fontSize = 12.sp)
                Text("${item.cost}", style = MaterialTheme.typography.labelMedium, color = GreenBuddyColors.leafGold, fontWeight = FontWeight.Bold)
            }
        }
        when (status) {
            CosmeticStatus.Equipped -> OutlinedButton(onClick = {}, enabled = false, shape = CircleShape) {
                Text(stringResource(R.string.equipped), style = MaterialTheme.typography.labelMedium)
            }
            CosmeticStatus.Owned -> Button(onClick = onEquip, shape = CircleShape) {
                Text(stringResource(R.string.equip), style = MaterialTheme.typography.labelMedium)
            }
            CosmeticStatus.Affordable -> Button(
                onClick = onPurchase,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = GreenBuddyColors.leafGold),
            ) {
                Text(stringResource(R.string.buy), style = MaterialTheme.typography.labelMedium)
            }
            CosmeticStatus.Unowned -> OutlinedButton(onClick = onPurchase, enabled = false, shape = CircleShape) {
                Text("🔒 $tokensNeeded", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
