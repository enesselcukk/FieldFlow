package com.example.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.EventRecordDao
import com.example.data.local.dao.GeofenceEventDao
import com.example.data.local.dao.GeofenceZoneDao
import com.example.data.local.dao.LocationDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.entity.EventRecordEntity
import com.example.data.local.entity.GeofenceEventEntity
import com.example.data.local.entity.GeofenceZoneEntity
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.NotificationEntity

@Database(
    entities = [
        LocationEntity::class,
        GeofenceZoneEntity::class,
        GeofenceEventEntity::class,
        EventRecordEntity::class,
        NotificationEntity::class
    ],
    version = 6,
    exportSchema = false
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun geofenceZoneDao(): GeofenceZoneDao
    abstract fun geofenceEventDao(): GeofenceEventDao
    abstract fun eventRecordDao(): EventRecordDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE location_records ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_location_records_is_synced ON location_records(is_synced)")
                db.execSQL("ALTER TABLE event_records ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_event_records_is_synced ON event_records(is_synced)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE location_records ADD COLUMN synced_at INTEGER")
                db.execSQL("ALTER TABLE event_records ADD COLUMN synced_at INTEGER")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        extra_arg TEXT,
                        is_read INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_timestamp ON notifications(timestamp)")
            }
        }
    }
}
