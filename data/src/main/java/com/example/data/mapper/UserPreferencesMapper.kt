package com.example.data.mapper

import androidx.datastore.preferences.core.Preferences
import com.example.data.local.preferences.SettingsPreferencesKeys
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.domain.model.UserPreferences

internal fun Preferences.toUserPreferences(): UserPreferences =
    UserPreferences(
        language = when (this[SettingsPreferencesKeys.language]) {
            AppLanguage.TURKISH.code -> AppLanguage.TURKISH
            else -> AppLanguage.ENGLISH
        },
        theme = when (this[SettingsPreferencesKeys.theme]) {
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            AppTheme.DARK.name -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        },
        locationIntervalSeconds = this[SettingsPreferencesKeys.locationIntervalSeconds] ?: 60,
    )

internal fun AppLanguage.toPreferenceValue(): String = code

internal fun AppTheme.toPreferenceValue(): String = name
