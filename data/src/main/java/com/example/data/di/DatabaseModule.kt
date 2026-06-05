package com.example.data.di

import android.content.Context
import android.database.SQLException
import androidx.room.Room
import com.example.data.local.crypto.DatabasePassphraseStore
import com.example.data.local.crypto.SqlCipherDatabaseMigrator
import com.example.data.local.dao.EventRecordDao
import com.example.data.local.dao.GeofenceEventDao
import com.example.data.local.dao.GeofenceZoneDao
import com.example.data.local.dao.LocationDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton
import kotlin.text.Charsets.UTF_8
import android.util.Log

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    private const val TAG = "DatabaseModule"
    private const val LOG_MIGRATION_FAILED = "Migration reset or failed; continuing with empty encrypted DB if needed"
    private const val DB_NAME = "fieldflow.db"

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        passphraseStore: DatabasePassphraseStore
    ): AppDatabase {
        val passphrase = passphraseStore.getOrCreatePassphraseString()
        try {
            SqlCipherDatabaseMigrator.migratePlainDatabaseIfNeeded(context, passphrase)
        } catch (e: SQLException) {
            Log.e(TAG, LOG_MIGRATION_FAILED, e)
        }
        SqlCipherDatabaseMigrator.ensureNativeLibraryLoaded()
        val factory = SupportOpenHelperFactory(passphrase.toByteArray(UTF_8))
        return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .openHelperFactory(factory)
            .addMigrations(
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6
            )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideLocationDao(db: AppDatabase): LocationDao = db.locationDao()

    @Provides
    fun provideGeofenceZoneDao(db: AppDatabase): GeofenceZoneDao = db.geofenceZoneDao()

    @Provides
    fun provideGeofenceEventDao(db: AppDatabase): GeofenceEventDao = db.geofenceEventDao()

    @Provides
    fun provideEventRecordDao(db: AppDatabase): EventRecordDao = db.eventRecordDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
}
