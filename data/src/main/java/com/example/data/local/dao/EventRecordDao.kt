package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EventRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EventRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EventRecordEntity): Long

    @Query("SELECT * FROM event_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<EventRecordEntity>>

    @Query("UPDATE event_records SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String)

    @Query("SELECT * FROM event_records WHERE is_synced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsynced(): List<EventRecordEntity>

    @Query("UPDATE event_records SET is_synced = 1, synced_at = :syncedAt WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>, syncedAt: Long)
}
