package com.example.data.local.preferences

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object SettingsPreferencesKeys {
    val language = stringPreferencesKey("language")
    val theme = stringPreferencesKey("theme")
    val locationIntervalSeconds = intPreferencesKey("location_interval_seconds")
}
