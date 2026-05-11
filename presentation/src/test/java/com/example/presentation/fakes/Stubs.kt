package com.example.presentation.fakes

import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.domain.model.EventRecord
import com.example.domain.model.GeofenceEvent
import com.example.domain.model.GeofenceZone
import com.example.domain.model.LocationRecord
import com.example.domain.model.NotificationRecord
import com.example.domain.model.UserPreferences
import com.example.domain.repository.EventRepository
import com.example.domain.repository.GeofenceRepository
import com.example.domain.repository.LocationRepository
import com.example.domain.repository.NotificationRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.StatusRepository
import com.example.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

internal class MutableTrackingRepository(
    tracking: Boolean = false
) : TrackingRepository {

    private val flow = MutableStateFlow(tracking)
    override val isTracking: StateFlow<Boolean> = flow.asStateFlow()

    override fun startTracking() {
        flow.value = true
    }

    override fun stopTracking() {
        flow.value = false
    }

    override fun toggleTracking() {
        flow.value = !flow.value
    }

    fun set(value: Boolean) {
        flow.value = value
    }
}

internal class StubLocationFlowRepository(
    private val records: MutableStateFlow<List<LocationRecord>>
) : LocationRepository {

    override suspend fun insertLocation(record: LocationRecord) {
        records.value += record
    }

    override suspend fun deleteOlderThan(timestampMs: Long) {
        records.value = records.value.filterNot { it.timestamp < timestampMs }
    }

    override suspend fun deleteSyncedOlderThan(timestampMs: Long) {}

    override fun getLocationsAfter(timestampMs: Long): Flow<List<LocationRecord>> = records

    override suspend fun getUnsyncedLocations(): List<LocationRecord> = emptyList()

    override suspend fun markLocationsSynced(ids: List<Long>, syncedAt: Long) {}
}

internal class StubGeofenceRepository(
    private val zones: MutableStateFlow<List<GeofenceZone>>,
    private val events: MutableStateFlow<List<GeofenceEvent>>,
) : GeofenceRepository {

    var lastSavedZone: GeofenceZone? = null
        private set
    var lastDeletedZoneId: Long? = null
        private set

    override suspend fun saveZone(zone: GeofenceZone) {
        lastSavedZone = zone
        val assignedId = if (zone.id != 0L) zone.id else (zones.value.maxOfOrNull { it.id } ?: 0L) + 1L
        zones.value += zone.copy(id = assignedId)
    }

    override suspend fun deleteZone(zoneId: Long) {
        lastDeletedZoneId = zoneId
        zones.value = zones.value.filterNot { it.id == zoneId }
    }

    override suspend fun getAllZones(): List<GeofenceZone> = zones.value

    override fun observeAllZones(): Flow<List<GeofenceZone>> = zones

    override suspend fun saveEvent(event: GeofenceEvent) {
        val assignedId =
            if (event.id != 0L) event.id else (events.value.maxOfOrNull { it.id } ?: 0L) + 1L
        events.value += event.copy(id = assignedId)
    }

    override fun observeRecentEvents(limit: Int): Flow<List<GeofenceEvent>> =
        events.map { list ->
            list.sortedByDescending { it.timestamp }.take(limit)
        }
}

internal class StubStatusRepository(
    val connectivity: MutableStateFlow<Boolean> = MutableStateFlow(true),
    val locationEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true),
    val batteryLevel: MutableStateFlow<Int> = MutableStateFlow(72),
) : StatusRepository {

    override fun observeConnectivity(): Flow<Boolean> = connectivity

    override fun observeLocationEnabled(): Flow<Boolean> = locationEnabled

    override fun observeBatteryLevel(): Flow<Int> = batteryLevel
}

internal class StubSettingsRepository(
    initial: UserPreferences = UserPreferences()
) : SettingsRepository {

    private val store = MutableStateFlow(initial)

    override val preferences: Flow<UserPreferences> = store.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        store.value = store.value.copy(language = language)
    }

    override suspend fun setTheme(theme: AppTheme) {
        store.value = store.value.copy(theme = theme)
    }

    override suspend fun setLocationInterval(seconds: Int) {
        store.value = store.value.copy(locationIntervalSeconds = seconds)
    }
}

internal class RecordingEventRepository(
    private val events: MutableStateFlow<List<EventRecord>>,
) : EventRepository {

    var lastNoteUpdate: Pair<Long, String>? = null

    override suspend fun saveEvent(event: EventRecord) {}

    override fun observeAll(): Flow<List<EventRecord>> = events

    override suspend fun updateNote(id: Long, note: String) {
        lastNoteUpdate = id to note
    }

    override suspend fun getUnsyncedEvents(): List<EventRecord> = emptyList()

    override suspend fun markEventsSynced(ids: List<Long>, syncedAt: Long) {}
}

internal class StubNotificationRepository(
    private val notifications: MutableStateFlow<List<NotificationRecord>>,
    private val unread: MutableStateFlow<Int>,
) : NotificationRepository {

    private fun recountUnread() {
        unread.value = notifications.value.count { !it.isRead }
    }

    override suspend fun save(record: NotificationRecord) {
        val nextId =
            if (record.id != 0L) record.id else (notifications.value.maxOfOrNull { it.id } ?: 0L) + 1L
        notifications.value += record.copy(id = nextId)
        recountUnread()
    }

    override fun observeAll(): Flow<List<NotificationRecord>> = notifications

    override fun observeUnreadCount(): Flow<Int> = unread

    override suspend fun markAllRead() {
        notifications.value = notifications.value.map { it.copy(isRead = true) }
        unread.value = 0
    }

    override suspend fun delete(id: Long) {
        notifications.value = notifications.value.filterNot { it.id == id }
        recountUnread()
    }

    override suspend fun deleteAll() {
        notifications.value = emptyList()
        unread.value = 0
    }
}
