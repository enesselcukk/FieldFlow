package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EventRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EventRecordEntity): Long

    @Query("SELECT * FROM event_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<EventRecordEntity>>

    @Query("UPDATE event_records SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String)
}
