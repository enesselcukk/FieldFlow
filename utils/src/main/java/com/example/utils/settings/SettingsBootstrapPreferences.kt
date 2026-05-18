package com.example.utils.settings

import android.content.Context
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.domain.model.UserPreferences
import com.example.utils.KEY_LANGUAGE_CODE
import com.example.utils.KEY_LOCATION_INTERVAL
import com.example.utils.KEY_THEME_NAME
import com.example.utils.PREFS_NAME


object SettingsBootstrapPreferences {

    fun readLanguageCode(context: Context): String? =
        prefs(context).getString(KEY_LANGUAGE_CODE, null)

    fun readThemeName(context: Context): String? =
        prefs(context).getString(KEY_THEME_NAME, null)

    fun writeLanguageCodeSync(context: Context, languageCode: String): Boolean =
        prefs(context).edit().putString(KEY_LANGUAGE_CODE, languageCode).commit()

    fun writeThemeNameSync(context: Context, themeName: String): Boolean =
        prefs(context).edit().putString(KEY_THEME_NAME, themeName).commit()

    fun writeLocationIntervalSync(context: Context, seconds: Int): Boolean =
        prefs(context).edit().putInt(KEY_LOCATION_INTERVAL, seconds).commit()

    fun writeAllSync(context: Context, userPrefs: UserPreferences): Boolean =
        prefs(context).edit()
            .putString(KEY_LANGUAGE_CODE, userPrefs.language.code)
            .putString(KEY_THEME_NAME, userPrefs.theme.name)
            .putInt(KEY_LOCATION_INTERVAL, userPrefs.locationIntervalSeconds)
            .commit()

    fun resolveLanguage(code: String?, fallback: AppLanguage): AppLanguage =
        when (code) {
            AppLanguage.TURKISH.code -> AppLanguage.TURKISH
            AppLanguage.ENGLISH.code -> AppLanguage.ENGLISH
            else -> fallback
        }

    fun resolveTheme(name: String?, fallback: AppTheme): AppTheme =
        when (name) {
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            AppTheme.DARK.name -> AppTheme.DARK
            AppTheme.SYSTEM.name -> AppTheme.SYSTEM
            else -> fallback
        }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
