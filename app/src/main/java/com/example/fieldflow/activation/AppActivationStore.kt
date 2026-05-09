package com.example.fieldflow.activation

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.activationDataStore by preferencesDataStore(name = "activation_prefs")

class AppActivationStore(
    private val context: Context
) {
    private val activatedKey = booleanPreferencesKey("is_activated")

    val isActivated: Flow<Boolean> = context.activationDataStore.data
        .map { preferences: Preferences -> preferences[activatedKey] ?: false }

    suspend fun setActivated(value: Boolean) {
        context.activationDataStore.edit { preferences ->
            preferences[activatedKey] = value
        }
    }
}
