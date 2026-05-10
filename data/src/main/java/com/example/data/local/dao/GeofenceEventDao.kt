package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.GeofenceEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface GeofenceEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GeofenceEventEntity)

    @Query("SELECT * FROM geofence_events ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<GeofenceEventEntity>>
}
