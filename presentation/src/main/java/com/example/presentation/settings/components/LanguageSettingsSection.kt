package com.example.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.presentation.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LanguageSettingsSection(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    SettingsSectionCard(title = stringResource(R.string.settings_language_title)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedFilterChip(
                selected = selectedLanguage == AppLanguage.TURKISH,
                onClick = { onLanguageSelected(AppLanguage.TURKISH) },
                label = { Text(stringResource(R.string.settings_language_turkish)) },
                colors = settingsSelectedChipColors(),
            )
            ElevatedFilterChip(
                selected = selectedLanguage == AppLanguage.ENGLISH,
                onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
                label = { Text(stringResource(R.string.settings_language_english)) },
                colors = settingsSelectedChipColors(),
            )
        }
    }
}
