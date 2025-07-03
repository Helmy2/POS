package com.wael.astimal.pos.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages the last synchronization timestamp.
 */
interface SyncManager {
    /**
     * A flow that emits the last saved sync date string in the required API format.
     * Returns a default past date if no sync has ever occurred.
     */
    fun getLastSyncDate(): Flow<String>

    /**
     * Updates the last sync date to the new value provided by the server.
     */
    suspend fun updateLastSyncDate(newDate: String)
}

class SyncManagerImpl(
    private val dataStore: DataStore<Preferences>
) : SyncManager {

    private object PreferencesKeys {
        val LAST_SYNC_DATE = stringPreferencesKey("last_sync_date")
    }

    override fun getLastSyncDate(): Flow<String> {
        return dataStore.data.map { preferences ->
            // todo
//            preferences[PreferencesKeys.LAST_SYNC_DATE] ?: getDefaultSyncDate()
            getDefaultSyncDate()
        }
    }

    override suspend fun updateLastSyncDate(newDate: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_DATE] = newDate
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
