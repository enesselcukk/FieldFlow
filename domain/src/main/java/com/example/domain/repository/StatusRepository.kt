package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface StatusRepository {
    fun observeConnectivity(): Flow<Boolean>
    fun observeLocationEnabled(): Flow<Boolean>
    fun observeBatteryLevel(): Flow<Int>
}
