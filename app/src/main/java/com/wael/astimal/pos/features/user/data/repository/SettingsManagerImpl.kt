package com.wael.astimal.pos.features.user.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import kotlin.coroutines.cancellation.CancellationException

class SettingsManagerImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsManager {

    override fun getThemeMode(): Flow<ThemeMode> {
        return dataStore.data
            .catch { exception ->
                // Always re-throw CancellationException
                if (exception is CancellationException) {
                    throw exception
                }
               emit(emptyPreferences())
            }
            .map { preferences ->
                val themeName = preferences[stringPreferencesKey(SettingsManager.THEME_KEY)]
                if (themeName == null) {
                    ThemeMode.System
                } else {
                    try {
                        ThemeMode.valueOf(themeName)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        ThemeMode.System
                    }
                }
            }
            .flowOn(Dispatchers.Default)
    }


    override suspend fun changeTheme(
        mode: ThemeMode
    ) {
        return withContext(Dispatchers.IO) {
            try {
                dataStore.edit {
                    it[stringPreferencesKey(SettingsManager.THEME_KEY)] = mode.name
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }
        }
    }

    override fun getLanguage(): Flow<Language> {
        return dataStore.data
            .catch { exception ->
                if (exception is CancellationException) {
                    throw exception
                }
                emit(emptyPreferences())
            }
            .map { preferences ->
                val langName = preferences[stringPreferencesKey(SettingsManager.LANGUAGE_KEY)]
                if (langName == null) {
                    Language.English
                } else {
                    try {
                        Language.valueOf(langName)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        Language.English
                    }
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun changeLanguage(
        language: Language
    ) {
        return withContext(Dispatchers.IO) {
            try {
                dataStore.edit {
                    it[stringPreferencesKey(SettingsManager.LANGUAGE_KEY)] = language.name
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }
        }
    }
}

