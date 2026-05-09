package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.GeofenceZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceZoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GeofenceZoneEntity)

    @Query("DELETE FROM geofence_zones WHERE id = :zoneId")
    suspend fun deleteById(zoneId: Long)

    @Query("SELECT * FROM geofence_zones")
    suspend fun getAll(): List<GeofenceZoneEntity>

    @Query("SELECT * FROM geofence_zones")
    fun observeAll(): Flow<List<GeofenceZoneEntity>>
}
