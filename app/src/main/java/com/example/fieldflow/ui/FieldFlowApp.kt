package com.example.fieldflow.ui

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AppTheme
import com.example.fieldflow.activation.AppActivationStore
import com.example.fieldflow.navigation.MainNavigationHost
import com.example.fieldflow.ui.theme.FieldFlowTheme
import com.example.presentation.notification.NotificationListViewModel
import com.example.presentation.settings.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

@Composable
fun FieldFlowApp(
    activity: ComponentActivity,
    pendingNavDestination: MutableStateFlow<String?>,
    activationStore: AppActivationStore
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle()

    val notificationViewModel: NotificationListViewModel = hiltViewModel()

    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = when (prefs.theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> systemInDarkTheme
    }

    val baseConfiguration = LocalConfiguration.current
    val localizedConfiguration = remember(baseConfiguration, prefs.language) {
        Configuration(baseConfiguration).apply {
            setLocale(Locale.forLanguageTag(prefs.language.code))
        }
    }

    LaunchedEffect(prefs.language) {
        val locale = Locale.forLanguageTag(prefs.language.code)
        Locale.setDefault(locale)
        @Suppress("DEPRECATION")
        activity.resources.updateConfiguration(
            Configuration(activity.resources.configuration).also { it.setLocale(locale) },
            activity.resources.displayMetrics
        )
    }

    FieldFlowTheme(
        darkTheme = isDarkTheme,
        dynamicColor = prefs.theme == AppTheme.SYSTEM
    ) {
        CompositionLocalProvider(LocalConfiguration provides localizedConfiguration) {
            MainNavigationHost(
                activity = activity,
                activationStore = activationStore,
                pendingNavDestination = pendingNavDestination,
                notificationViewModel = notificationViewModel
            )
        }
    }
}
