package com.example.fieldflow.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fieldflow.MainActivity
import com.example.fieldflow.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val nm: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_TRACKING,
                    context.getString(R.string.notif_channel_tracking_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = context.getString(R.string.notif_channel_tracking_desc)
                    setShowBadge(false)
                }
            )
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_GEOFENCE,
                    context.getString(R.string.notif_channel_geofence_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notif_channel_geofence_desc)
                }
            )
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    context.getString(R.string.notif_channel_system_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notif_channel_system_desc)
                }
            )
        }
    }

    fun buildTrackingNotification(): Notification =
        NotificationCompat.Builder(context, CHANNEL_TRACKING)
            .setContentTitle(context.getString(R.string.notif_tracking_title))
            .setContentText(context.getString(R.string.notif_tracking_text))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(launchAppIntent(RC_TRACKING))
            .setOngoing(true)
            .setSilent(true)
            .build()

    fun sendGeofenceExitAlert(zoneKey: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_GEOFENCE)
            .setContentTitle(context.getString(R.string.notif_geofence_title))
            .setContentText(context.getString(R.string.notif_geofence_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(launchAppIntent(NOTIFICATION_ID_GEOFENCE_BASE + zoneKey))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(NOTIFICATION_ID_GEOFENCE_BASE + zoneKey, notification)
    }

    fun sendInternetLostAlert() {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setContentTitle(context.getString(R.string.notif_internet_lost_title))
            .setContentText(context.getString(R.string.notif_internet_lost_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(launchAppIntent(NOTIFICATION_ID_INTERNET_LOST))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(NOTIFICATION_ID_INTERNET_LOST, notification)
    }

    fun cancelInternetLostAlert() {
        nm.cancel(NOTIFICATION_ID_INTERNET_LOST)
    }

    fun sendLocationServiceDisabledAlert() {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setContentTitle(context.getString(R.string.notif_location_disabled_title))
            .setContentText(context.getString(R.string.notif_location_disabled_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(launchAppIntent(NOTIFICATION_ID_LOCATION_DISABLED))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(NOTIFICATION_ID_LOCATION_DISABLED, notification)
    }

    fun cancelLocationServiceDisabledAlert() {
        nm.cancel(NOTIFICATION_ID_LOCATION_DISABLED)
    }

    private fun launchAppIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_TRACKING = "location_tracking_channel"
        const val CHANNEL_GEOFENCE = "geofence_alert_channel"
        const val CHANNEL_SYSTEM = "system_alert_channel"

        const val NOTIFICATION_ID_TRACKING = 1001
        const val NOTIFICATION_ID_GEOFENCE_BASE = 2000
        const val NOTIFICATION_ID_INTERNET_LOST = 3001
        const val NOTIFICATION_ID_LOCATION_DISABLED = 3002

        private const val RC_TRACKING = 0
    }
}
