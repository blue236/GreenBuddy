package com.blue236.greenbuddy.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun StatCard(title: String, containerColor: Color = MaterialTheme.colorScheme.surface, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = containerColor), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
fun CareStatRow(label: String, value: Int) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text("$value%", fontWeight = FontWeight.SemiBold) } }

@Composable
fun StarterPlantCard(option: StarterPlantOption, selected: Boolean, onClick: () -> Unit) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val containerColor = if (selected) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
    Card(modifier = Modifier.fillMaxWidth().border(2.dp, borderColor, RoundedCornerShape(16.dp)).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).background(Color(0xFFC8E6C9), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text(option.previewEmoji, style = MaterialTheme.typography.headlineMedium) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(option.localizedTitle(localeTag), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(option.localizedSubtitle(localeTag), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.lesson_focus, option.localizedCareTip(localeTag)))
            }
            Text(if (selected) stringResource(R.string.selected) else stringResource(R.string.pick))
        }
    }
}
