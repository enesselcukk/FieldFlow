package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "geofence_events",
    indices = [Index(value = ["timestamp"])]
)
internal data class GeofenceEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zoneId: Long,
    val zoneName: String,
    val timestamp: Long,
    val eventType: String   
)
