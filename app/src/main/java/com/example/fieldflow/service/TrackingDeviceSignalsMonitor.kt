package com.example.fieldflow.service

import android.content.Context
import android.os.Build
import com.example.domain.model.EventRecord
import com.example.domain.repository.StatusRepository
import com.example.domain.usecase.event.SaveEventUseCase
import com.example.fieldflow.constants.BATTERY_LOW_THRESHOLD
import com.example.fieldflow.notification.NotificationHelper
import com.example.fieldflow.sync.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject


internal class TrackingDeviceSignalsMonitor @Inject constructor(
    private val statusRepository: StatusRepository,
    private val saveEvent: SaveEventUseCase,
    private val notificationHelper: NotificationHelper,
    @param:ApplicationContext private val appContext: Context,
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            statusRepository.observeConnectivity()
                .drop(1)
                .collect { isOnline ->
                    saveEvent(
                        EventRecord(
                            timestamp = System.currentTimeMillis(),
                            type = if (isOnline) EventRecord.EventType.INTERNET_RESTORED
                            else EventRecord.EventType.INTERNET_LOST,
                        ),
                    )
                    if (isOnline) {
                        notificationHelper.cancelInternetLostAlert()
                        SyncWorker.schedule(appContext)
                    } else {
                        notificationHelper.sendInternetLostAlert()
                    }
                }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            scope.launch {
                statusRepository.observeLocationEnabled()
                    .drop(1)
                    .collect { isEnabled ->
                        saveEvent(
                            EventRecord(
                                timestamp = System.currentTimeMillis(),
                                type = if (isEnabled) EventRecord.EventType.LOCATION_SERVICE_ENABLED
                                else EventRecord.EventType.LOCATION_SERVICE_DISABLED,
                            ),
                        )
                        if (isEnabled) {
                            notificationHelper.cancelLocationServiceDisabledAlert()
                        } else {
                            notificationHelper.sendLocationServiceDisabledAlert()
                        }
                    }
            }
        }
        scope.launch {
            statusRepository.observeBatteryLevel()
                .drop(1)
                .collect { level ->
                    when {
                        level in 0..BATTERY_LOW_THRESHOLD ->
                            notificationHelper.sendBatteryLowAlert(level)
                        level > BATTERY_LOW_THRESHOLD ->
                            notificationHelper.cancelBatteryLowAlert()
                    }
                }
        }
    }
}
