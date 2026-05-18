package com.example.fieldflow.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.domain.constants.NOTIF_TYPE_BATTERY
import com.example.domain.constants.NOTIF_TYPE_GEOFENCE
import com.example.domain.constants.NOTIF_TYPE_INTERNET
import com.example.domain.constants.NOTIF_TYPE_LOCATION
import com.example.domain.model.NotificationRecord
import com.example.domain.usecase.notification.SaveNotificationUseCase
import com.example.fieldflow.R
import com.example.fieldflow.ui.activity.MainActivity
import com.example.fieldflow.constants.BATTERY_CRITICAL_THRESHOLD
import com.example.fieldflow.constants.CHANNEL_GEOFENCE
import com.example.fieldflow.constants.CHANNEL_SYSTEM
import com.example.fieldflow.constants.CHANNEL_TRACKING
import com.example.fieldflow.constants.EXTRA_NOTIF_EXTRA_ARG
import com.example.fieldflow.constants.EXTRA_NOTIF_TIMESTAMP
import com.example.fieldflow.constants.EXTRA_NOTIF_TYPE
import com.example.fieldflow.constants.EXTRA_NAVIGATE_TO
import com.example.fieldflow.constants.NAV_NOTIFICATION_DETAIL
import com.example.fieldflow.constants.NOTIFICATION_ID_BATTERY_LOW
import com.example.fieldflow.constants.NOTIFICATION_ID_GEOFENCE_BASE
import com.example.fieldflow.constants.NOTIFICATION_ID_INTERNET_LOST
import com.example.fieldflow.constants.NOTIFICATION_ID_LOCATION_DISABLED
import com.example.fieldflow.constants.RC_TRACKING
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val saveNotification: SaveNotificationUseCase
) {

    private val nm: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun saveRecord(type: String, timestamp: Long, extraArg: String? = null) {
        scope.launch {
            saveNotification(NotificationRecord(type = type, timestamp = timestamp, extraArg = extraArg))
        }
    }

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
            .setContentIntent(launchAppIntent())
            .setOngoing(true)
            .setSilent(true)
            .build()

    fun sendGeofenceExitAlert(zoneKey: Int, zoneName: String) {
        val now = System.currentTimeMillis()
        val notification = NotificationCompat.Builder(context, CHANNEL_GEOFENCE)
            .setContentTitle(context.getString(R.string.notif_geofence_title))
            .setContentText(context.getString(R.string.notif_geofence_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(
                buildDetailIntent(
                    requestCode = NOTIFICATION_ID_GEOFENCE_BASE + zoneKey,
                    type = NOTIF_TYPE_GEOFENCE,
                    timestamp = now,
                    extraArg = zoneName
                )
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(NOTIFICATION_ID_GEOFENCE_BASE + zoneKey, notification)
        saveRecord(NOTIF_TYPE_GEOFENCE, now, zoneName)
    }

    fun sendInternetLostAlert() {
        val now = System.currentTimeMillis()
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setContentTitle(context.getString(R.string.notif_internet_lost_title))
            .setContentText(context.getString(R.string.notif_internet_lost_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(
                buildDetailIntent(
                    requestCode = NOTIFICATION_ID_INTERNET_LOST,
                    type = NOTIF_TYPE_INTERNET,
                    timestamp = now
                )
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(NOTIFICATION_ID_INTERNET_LOST, notification)
        saveRecord(NOTIF_TYPE_INTERNET, now)
    }

    fun cancelInternetLostAlert() {
        nm.cancel(NOTIFICATION_ID_INTERNET_LOST)
    }

    fun sendLocationServiceDisabledAlert() {
        val now = System.currentTimeMillis()
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setContentTitle(context.getString(R.string.notif_location_disabled_title))
            .setContentText(context.getString(R.string.notif_location_disabled_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(
                buildDetailIntent(
                    requestCode = NOTIFICATION_ID_LOCATION_DISABLED,
                    type = NOTIF_TYPE_LOCATION,
                    timestamp = now
                )
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(NOTIFICATION_ID_LOCATION_DISABLED, notification)
        saveRecord(NOTIF_TYPE_LOCATION, now)
    }

    fun cancelLocationServiceDisabledAlert() {
        nm.cancel(NOTIFICATION_ID_LOCATION_DISABLED)
    }

    fun sendBatteryLowAlert(level: Int) {
        val now = System.currentTimeMillis()
        val priority = if (level <= BATTERY_CRITICAL_THRESHOLD)
            NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setContentTitle(context.getString(R.string.notif_battery_low_title))
            .setContentText(context.getString(R.string.notif_battery_low_text, level))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(
                buildDetailIntent(
                    requestCode = NOTIFICATION_ID_BATTERY_LOW,
                    type = NOTIF_TYPE_BATTERY,
                    timestamp = now,
                    extraArg = level.toString()
                )
            )
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setPriority(priority)
            .build()
        nm.notify(NOTIFICATION_ID_BATTERY_LOW, notification)
        saveRecord(NOTIF_TYPE_BATTERY, now, level.toString())
    }

    fun cancelBatteryLowAlert() {
        nm.cancel(NOTIFICATION_ID_BATTERY_LOW)
    }

    private fun buildDetailIntent(
        requestCode: Int,
        type: String,
        timestamp: Long,
        extraArg: String? = null
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAV_NOTIFICATION_DETAIL)
            putExtra(EXTRA_NOTIF_TYPE, type)
            putExtra(EXTRA_NOTIF_TIMESTAMP, timestamp)
            if (extraArg != null) putExtra(EXTRA_NOTIF_EXTRA_ARG, extraArg)
        }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun launchAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, RC_TRACKING, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
