package com.example.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.EventRecordDao
import com.example.data.local.dao.GeofenceEventDao
import com.example.data.local.dao.GeofenceZoneDao
import com.example.data.local.dao.LocationDao
import com.example.data.local.entity.EventRecordEntity
import com.example.data.local.entity.GeofenceEventEntity
import com.example.data.local.entity.GeofenceZoneEntity
import com.example.data.local.entity.LocationEntity

@Database(
    entities = [
        LocationEntity::class,
        GeofenceZoneEntity::class,
        GeofenceEventEntity::class,
        EventRecordEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun geofenceZoneDao(): GeofenceZoneDao
    abstract fun geofenceEventDao(): GeofenceEventDao
    abstract fun eventRecordDao(): EventRecordDao
}
