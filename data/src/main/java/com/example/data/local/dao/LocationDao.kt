package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocationEntity)

    @Query("SELECT * FROM location_records WHERE timestamp >= :sinceMs ORDER BY timestamp ASC")
    fun getLocationsAfter(sinceMs: Long): Flow<List<LocationEntity>>

    @Query("DELETE FROM location_records WHERE timestamp < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long)
}
