package com.example.domain.repository

import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLocationInterval(seconds: Int)
}
