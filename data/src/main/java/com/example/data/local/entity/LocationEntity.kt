package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location_records",
    indices = [Index(value = ["timestamp"]), Index(value = ["is_synced"])]
)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    @ColumnInfo(name = "is_synced", defaultValue = "0") val isSynced: Boolean = false,
    @ColumnInfo(name = "synced_at") val syncedAt: Long? = null
)
