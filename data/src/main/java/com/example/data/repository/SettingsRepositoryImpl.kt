package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.domain.model.UserPreferences
import com.example.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

@Singleton
internal class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    private val languageKey = stringPreferencesKey("language")
    private val themeKey = stringPreferencesKey("theme")
    private val intervalKey = intPreferencesKey("location_interval_seconds")

    override val preferences: Flow<UserPreferences> = context.settingsDataStore.data.map { prefs ->
        UserPreferences(
            language = when (prefs[languageKey]) {
                AppLanguage.TURKISH.code -> AppLanguage.TURKISH
                else -> AppLanguage.ENGLISH
            },
            theme = when (prefs[themeKey]) {
                AppTheme.LIGHT.name -> AppTheme.LIGHT
                AppTheme.DARK.name -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            },
            locationIntervalSeconds = prefs[intervalKey] ?: 60
        )
    }

    override suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[languageKey] = language.code }
    }

    override suspend fun setTheme(theme: AppTheme) {
        context.settingsDataStore.edit { it[themeKey] = theme.name }
    }

    override suspend fun setLocationInterval(seconds: Int) {
        context.settingsDataStore.edit { it[intervalKey] = seconds }
    }
}
