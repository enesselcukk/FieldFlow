package com.example.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppTheme
import com.example.presentation.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThemeSettingsSection(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
) {
    SettingsSectionCard(title = stringResource(R.string.settings_theme_title)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedFilterChip(
                selected = selectedTheme == AppTheme.LIGHT,
                onClick = { onThemeSelected(AppTheme.LIGHT) },
                label = { Text(stringResource(R.string.settings_theme_light)) },
                colors = settingsSelectedChipColors(),
            )
            ElevatedFilterChip(
                selected = selectedTheme == AppTheme.DARK,
                onClick = { onThemeSelected(AppTheme.DARK) },
                label = { Text(stringResource(R.string.settings_theme_dark)) },
                colors = settingsSelectedChipColors(),
            )
            ElevatedFilterChip(
                selected = selectedTheme == AppTheme.SYSTEM,
                onClick = { onThemeSelected(AppTheme.SYSTEM) },
                label = { Text(stringResource(R.string.settings_theme_system)) },
                colors = settingsSelectedChipColors(),
            )
        }
    }
}
