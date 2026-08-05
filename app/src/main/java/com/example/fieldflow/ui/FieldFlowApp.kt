package com.example.fieldflow.ui

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AppTheme
import com.example.fieldflow.activation.AppActivationStore
import com.example.fieldflow.navigation.MainNavigationHost
import com.example.fieldflow.ui.security.DeviceCompromiseWarningDialog
import com.example.fieldflow.ui.theme.FieldFlowTheme
import com.example.presentation.notification.NotificationListViewModel
import com.example.presentation.settings.SettingsViewModel
import com.example.utils.security.RootDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
internal fun FieldFlowApp(
    activity: ComponentActivity,
    pendingNavDestination: MutableStateFlow<String?>,
    activationStore: AppActivationStore
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle(
        lifecycleOwner = activity
    )

    val notificationViewModel: NotificationListViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val resolvedLanguage = prefs.language
    val resolvedTheme = prefs.theme

    var deviceCompromised by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        deviceCompromised = withContext(Dispatchers.IO) {
            RootDetector.isDeviceCompromised()
        }
    }
    var rootSecurityAcknowledged by rememberSaveable { mutableStateOf(false) }
    val showRootSecurityDialog = deviceCompromised && !rootSecurityAcknowledged

    val isDarkTheme = when (resolvedTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val baseConfiguration = LocalConfiguration.current
    val localizedContext = remember(activity, resolvedLanguage, baseConfiguration) {
        activity.createConfigurationContext(
            Configuration(baseConfiguration).apply {
                setLocale(Locale.forLanguageTag(resolvedLanguage.code))
            }
        )
    }

    FieldFlowTheme(
        darkTheme = isDarkTheme,
        dynamicColor = resolvedTheme == AppTheme.SYSTEM
    ) {
        CompositionLocalProvider(
            LocalConfiguration provides localizedContext.resources.configuration,
            LocalResources provides localizedContext.resources,
        ) {
            Box(Modifier.fillMaxSize()) {
                MainNavigationHost(
                    activity = activity,
                    activationStore = activationStore,
                    pendingNavDestination = pendingNavDestination,
                    notificationViewModel = notificationViewModel
                )
                if (showRootSecurityDialog) {
                    DeviceCompromiseWarningDialog(
                        onAcknowledge = { rootSecurityAcknowledged = true }
                    )
                }
            }
        }
    }
}
