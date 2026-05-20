package com.example.utils.settings

import android.content.Context
import androidx.core.content.edit
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.domain.model.UserPreferences
import com.example.utils.KEY_LANGUAGE_CODE
import com.example.utils.KEY_LOCATION_INTERVAL
import com.example.utils.KEY_THEME_NAME
import com.example.utils.PREFS_NAME

object SettingsBootstrapPreferences {

    fun readUserPreferencesSync(context: Context): UserPreferences {
        val p = prefs(context)
        val languageCode = p.getString(KEY_LANGUAGE_CODE, null)
        val language = when (languageCode) {
            AppLanguage.TURKISH.code -> AppLanguage.TURKISH
            else -> AppLanguage.ENGLISH
        }
        val theme = when (p.getString(KEY_THEME_NAME, null)) {
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            AppTheme.DARK.name -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
        val locationIntervalSeconds = if (p.contains(KEY_LOCATION_INTERVAL)) {
            p.getInt(KEY_LOCATION_INTERVAL, 60)
        } else {
            60
        }
        return UserPreferences(
            language = language,
            theme = theme,
            locationIntervalSeconds = locationIntervalSeconds,
        )
    }

    fun writeLanguageCodeSync(context: Context, languageCode: String) {
        prefs(context).edit(commit = true) {
            putString(KEY_LANGUAGE_CODE, languageCode)
        }
    }

    fun writeThemeNameSync(context: Context, themeName: String) {
        prefs(context).edit(commit = true) {
            putString(KEY_THEME_NAME, themeName)
        }
    }

    fun writeLocationIntervalSync(context: Context, seconds: Int) {
        prefs(context).edit(commit = true) {
            putInt(KEY_LOCATION_INTERVAL, seconds)
        }
    }

    fun writeAllSync(context: Context, userPrefs: UserPreferences) {
        prefs(context).edit(commit = true) {
            putString(KEY_LANGUAGE_CODE, userPrefs.language.code)
            putString(KEY_THEME_NAME, userPrefs.theme.name)
            putInt(KEY_LOCATION_INTERVAL, userPrefs.locationIntervalSeconds)
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
