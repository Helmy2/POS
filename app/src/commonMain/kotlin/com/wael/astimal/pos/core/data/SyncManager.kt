package com.wael.astimal.pos.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Manages the last synchronization timestamp.
 */
interface SyncManager {
    // Sync date tracking
    suspend fun lastDeletedSyncDate(): String
    suspend fun lastSyncDate(): String

    suspend fun updateLastDeletedSyncDate(newDate: String)
    suspend fun updateLastSyncDate(newDate: String)

    suspend fun restSyncDate()
}

class SyncManagerImpl(
    private val dataStore: DataStore<Preferences>,
) : SyncManager {

    private object PreferencesKeys {
        val LAST_DELETED_SYNC_DATE = stringPreferencesKey("last_deleted_sync_date")
        val LAST_SYNC_DATE = stringPreferencesKey("last_sync_date")
    }

    override suspend fun lastDeletedSyncDate(): String {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_DELETED_SYNC_DATE] ?: getDefaultSyncDate()
        }.first()
    }

    override suspend fun lastSyncDate(): String {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_DATE] ?: getDefaultSyncDate()
        }.first()
    }

    override suspend fun updateLastDeletedSyncDate(newDate: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_DELETED_SYNC_DATE] = newDate
        }
    }

    override suspend fun updateLastSyncDate(newDate: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_DATE] = newDate
        }
    }

    override suspend fun restSyncDate() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LAST_SYNC_DATE)
            preferences.remove(PreferencesKeys.LAST_DELETED_SYNC_DATE)
        }
    }

    /**
     * Provides a default date string for the very first sync.
     * For example, one month in the past.
     */
    private fun getDefaultSyncDate(): String {
        return "1970-01-01"
    }
}
