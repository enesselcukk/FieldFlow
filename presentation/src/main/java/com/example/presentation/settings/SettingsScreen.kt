package com.example.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.settings.components.SettingsContent

@Composable
fun SettingsScreen(
    isActivated: Boolean,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    SettingsContent(
        prefs = prefs,
        isActivated = isActivated,
        onLanguageSelected = viewModel::setLanguage,
        onThemeSelected = viewModel::setTheme,
        onLocationIntervalSelected = viewModel::setLocationInterval,
        modifier = modifier,
    )
}
