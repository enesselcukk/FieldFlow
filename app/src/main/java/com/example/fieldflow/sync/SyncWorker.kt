package com.example.fieldflow.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.domain.repository.EventRepository
import com.example.domain.repository.LocationRepository
import com.example.fieldflow.constants.SYNC_BACKOFF_SECONDS
import com.example.fieldflow.constants.SYNC_MAX_RETRY_COUNT
import com.example.fieldflow.constants.SYNC_PERIODIC_INTERVAL_HOURS
import com.example.fieldflow.constants.WORK_NAME_ONE_TIME
import com.example.fieldflow.constants.WORK_NAME_PERIODIC
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val unsyncedLocations = locationRepository.getUnsyncedLocations()
        val unsyncedEvents = eventRepository.getUnsyncedEvents()

        if (unsyncedLocations.isEmpty() && unsyncedEvents.isEmpty()) {
            return Result.success()
        }

        return try {
            val syncedAt = System.currentTimeMillis()

            if (unsyncedLocations.isNotEmpty()) {
                locationRepository.markLocationsSynced(unsyncedLocations.map { it.id }, syncedAt)
            }
            if (unsyncedEvents.isNotEmpty()) {
                eventRepository.markEventsSynced(unsyncedEvents.map { it.id }, syncedAt)
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < SYNC_MAX_RETRY_COUNT) Result.retry() else Result.failure()
        }
    }

    companion object {
        private fun buildConstraints() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(buildConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, SYNC_BACKOFF_SECONDS, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun schedulePeriodicFallback(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = SYNC_PERIODIC_INTERVAL_HOURS,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(buildConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, SYNC_BACKOFF_SECONDS, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
