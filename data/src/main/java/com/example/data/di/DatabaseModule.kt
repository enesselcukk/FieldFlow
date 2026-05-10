package com.example.data.di

import android.content.Context
import androidx.room.Room
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "fieldflow.db")
            .addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .build()

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
