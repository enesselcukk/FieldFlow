package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.preferences.SettingsPreferencesKeys
import com.example.data.mapper.toPreferenceValue
import com.example.data.mapper.toUserPreferences
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.domain.model.UserPreferences
import com.example.domain.repository.SettingsRepository
import com.example.utils.settings.SettingsBootstrapPreferences
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

    override val preferences: Flow<UserPreferences> =
        context.settingsDataStore.data.map { it.toUserPreferences() }

    override suspend fun setLanguage(language: AppLanguage) {
        SettingsBootstrapPreferences.writeLanguageCodeSync(context, language.code)
        context.settingsDataStore.edit {
            it[SettingsPreferencesKeys.language] = language.toPreferenceValue()
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        SettingsBootstrapPreferences.writeThemeNameSync(context, theme.name)
        context.settingsDataStore.edit {
            it[SettingsPreferencesKeys.theme] = theme.toPreferenceValue()
        }
    }

    override suspend fun setLocationInterval(seconds: Int) {
        context.settingsDataStore.edit {
            it[SettingsPreferencesKeys.locationIntervalSeconds] = seconds
        }
        SettingsBootstrapPreferences.writeLocationIntervalSync(context, seconds)
    }
}
