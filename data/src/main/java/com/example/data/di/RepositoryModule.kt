package com.example.data.di

import com.example.data.repository.EventRepositoryImpl
import com.example.data.repository.GeofenceRepositoryImpl
import com.example.data.repository.LocationRepositoryImpl
import com.example.data.repository.NotificationRepositoryImpl
import com.example.data.repository.StatusRepositoryImpl
import com.example.domain.repository.EventRepository
import com.example.domain.repository.GeofenceRepository
import com.example.domain.repository.LocationRepository
import com.example.domain.repository.NotificationRepository
import com.example.domain.repository.StatusRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStatusRepository(impl: StatusRepositoryImpl): StatusRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindGeofenceRepository(impl: GeofenceRepositoryImpl): GeofenceRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
