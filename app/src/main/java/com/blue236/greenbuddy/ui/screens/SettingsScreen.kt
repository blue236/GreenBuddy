package com.blue236.greenbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blue236.greenbuddy.R
import com.blue236.greenbuddy.model.AppLanguage
import com.blue236.greenbuddy.model.WeatherCatalog
import com.blue236.greenbuddy.model.systemLanguageLabel
import com.blue236.greenbuddy.ui.components.StatCard

private data class SettingsSection(
    val title: String,
    val description: String,
    val content: @Composable () -> Unit,
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    selectedWeatherCityId: String,
    appLanguage: AppLanguage,
    onNavigateBack: () -> Unit = {},
    onSetSelectedWeatherCity: (String) -> Unit,
    onSetAppLanguage: (AppLanguage) -> Unit,
) {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val systemLocaleTag = LocalConfiguration.current.locales[0]?.toLanguageTag().orEmpty()
    val sections = listOf(
        SettingsSection(
            title = stringResource(R.string.settings_category_localization),
            description = stringResource(R.string.settings_category_localization_description),
            content = {
                Text(stringResource(R.string.app_language), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.app_language_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(AppLanguage.SYSTEM, AppLanguage.ENGLISH, AppLanguage.GERMAN, AppLanguage.KOREAN).forEach { option ->
                        AssistChip(
                            onClick = { onSetAppLanguage(option) },
                            label = {
                                Text(
                                    (if (option == appLanguage) "✓ " else "") +
                                        systemLanguageLabel(option, systemLocaleTag, localeTag),
                                )
                            },
                        )
                    }
                }
            },
        ),
        SettingsSection(
            title = stringResource(R.string.settings_category_region),
            description = stringResource(R.string.settings_category_region_description),
            content = {
                Text(
                    stringResource(R.string.location_settings_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WeatherCatalog.cityOptions.forEach { option ->
                        AssistChip(
                            onClick = { onSetSelectedWeatherCity(option.id) },
                            label = {
                                Text(
                                    (if (option.id == selectedWeatherCityId) "✓ " else "") + option.defaultName,
                                )
                            },
                        )
                    }
                }
            },
        ),
        SettingsSection(
            title = stringResource(R.string.settings_category_coming_soon),
            description = stringResource(R.string.settings_category_coming_soon_description),
            content = {
                Text(
                    stringResource(R.string.settings_future_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        ),
    )

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.settings_title),
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back_to_profile),
                    )
                }
            },
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.settings_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            sections.forEach { section ->
                StatCard(section.title) {
                    Text(section.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    section.content()
                }
            }
        }
    }
}
