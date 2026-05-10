package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocationEntity)

    @Query("SELECT * FROM location_records WHERE timestamp >= :sinceMs ORDER BY timestamp ASC")
    fun getLocationsAfter(sinceMs: Long): Flow<List<LocationEntity>>

    @Query("DELETE FROM location_records WHERE timestamp < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long)

    @Query("DELETE FROM location_records WHERE is_synced = 1 AND timestamp < :beforeMs")
    suspend fun deleteSyncedOlderThan(beforeMs: Long)

    @Query("SELECT * FROM location_records WHERE is_synced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsynced(): List<LocationEntity>

    @Query("UPDATE location_records SET is_synced = 1, synced_at = :syncedAt WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>, syncedAt: Long)
}
