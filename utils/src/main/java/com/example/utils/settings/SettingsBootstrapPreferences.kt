package com.example.utils.settings

import android.content.Context
import androidx.core.content.edit
import com.example.domain.model.UserPreferences
import com.example.utils.KEY_LANGUAGE_CODE
import com.example.utils.KEY_LOCATION_INTERVAL
import com.example.utils.KEY_THEME_NAME
import com.example.utils.PREFS_NAME

object SettingsBootstrapPreferences {

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
