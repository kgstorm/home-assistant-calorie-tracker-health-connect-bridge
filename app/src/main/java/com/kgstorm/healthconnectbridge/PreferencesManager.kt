package com.kgstorm.healthconnectbridge

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages app preferences using DataStore
 */
class PreferencesManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        
        private val HA_URL_KEY = stringPreferencesKey("ha_url")
        private val HA_TOKEN_KEY = stringPreferencesKey("ha_token")
        private val LAST_SYNC_KEY = longPreferencesKey("last_sync")
    }

    /**
     * Save Home Assistant URL
     */
    suspend fun saveHomeAssistantUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[HA_URL_KEY] = url
        }
    }

    /**
     * Get Home Assistant URL
     */
    val homeAssistantUrl: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[HA_URL_KEY]
    }

    /**
     * Save Home Assistant access token
     */
    suspend fun saveHomeAssistantToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[HA_TOKEN_KEY] = token
        }
    }

    /**
     * Get Home Assistant access token
     */
    val homeAssistantToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[HA_TOKEN_KEY]
    }

    /**
     * Save last sync timestamp
     */
    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_KEY] = timestamp
        }
    }

    /**
     * Get last sync timestamp
     */
    val lastSyncTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_KEY]
    }

    /**
     * Check if settings are configured
     */
    suspend fun areSettingsConfigured(): Boolean {
        val preferences = context.dataStore.data.first()
        val url = preferences[HA_URL_KEY]
        val token = preferences[HA_TOKEN_KEY]
        return !url.isNullOrBlank() && !token.isNullOrBlank()
    }
}
