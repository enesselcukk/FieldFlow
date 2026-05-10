package com.example.fieldflow.navigation

import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.domain.model.IdentityInfo
import com.example.domain.model.NotificationRecord
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.fieldflow.R
import com.example.fieldflow.activation.AppActivationStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class MainNavRouter(
    private val activity: ComponentActivity,
    private val backStack: NavBackStack<NavKey>,
    private val scope: CoroutineScope,
    private val activationStore: AppActivationStore,
    private val setBiometricVerified: (Boolean) -> Unit,
) {
    fun onIdentityDetected(identity: IdentityInfo) {
        Toast.makeText(
            activity,
            activity.getString(R.string.detected_identity, identity.name, identity.surname),
            Toast.LENGTH_LONG
        ).show()
        backStack.add(ActivationRoute(identity.name, identity.surname))
    }

    fun onActivationCodeSuccess() {
        setBiometricVerified(true)
        scope.launch { activationStore.setActivated(true) }
        Toast.makeText(
            activity,
            activity.getString(R.string.activation_success),
            Toast.LENGTH_LONG
        ).show()
        backStack.clear()
        backStack.add(HomeRoute)
    }

    fun onBiometricAuthenticated() {
        setBiometricVerified(true)
        backStack.clear()
        backStack.add(HomeRoute)
    }

    fun navigateToMap() {
        backStack.add(MapRoute)
    }

    fun navigateToEventLog() {
        backStack.add(EventLogRoute)
    }

    fun navigateToNotificationList() {
        backStack.add(NotificationListRoute)
    }

    fun navigateToSettings() {
        backStack.add(SettingsRoute)
    }

    fun openNotificationDetail(record: NotificationRecord) {
        backStack.add(
            NotificationDetailRoute(
                type = record.type,
                timestamp = record.timestamp,
                extraArg = record.extraArg
            )
        )
    }

    fun popNotificationDetailThenEventLog() {
        backStack.removeLastOrNull()
        backStack.add(EventLogRoute)
    }

    fun clearToHome() {
        backStack.clear()
        backStack.add(HomeRoute)
    }

    companion object {
        const val ACTIVATION_CODE = "123456"
    }
}
