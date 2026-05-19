package com.example.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.domain.model.UserPreferences

@Composable
internal fun SettingsContent(
    prefs: UserPreferences,
    isActivated: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onLocationIntervalSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LanguageSettingsSection(
            selectedLanguage = prefs.language,
            onLanguageSelected = onLanguageSelected,
        )
        LocationIntervalSettingsSection(
            selectedIntervalSeconds = prefs.locationIntervalSeconds,
            isActivated = isActivated,
            onIntervalSelected = onLocationIntervalSelected,
        )
        ThemeSettingsSection(
            selectedTheme = prefs.theme,
            onThemeSelected = onThemeSelected,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
