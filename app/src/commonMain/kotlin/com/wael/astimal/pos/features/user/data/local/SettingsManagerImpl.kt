package com.wael.astimal.pos.features.user.data.local

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException


class SettingsManagerImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsManager {

    companion object {
        private const val THEME_KEY = "themeKey"
        private const val LANGUAGE_KEY = "languageKey"
        private const val USER_ID_KEY = "userIdKey"
    }

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
                val themeName =
                    preferences[stringPreferencesKey(THEME_KEY)]
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
                    it[stringPreferencesKey(THEME_KEY)] = mode.name
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
                val langName =
                    preferences[stringPreferencesKey(LANGUAGE_KEY)]
                if (langName == null) {
                    Language.Arabic
                } else {
                    try {
                        Language.valueOf(langName)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        Language.Arabic
                    }
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun changeUserId(id: String) {
        return withContext(Dispatchers.IO) {
            try {
                dataStore.edit {
                    it[stringPreferencesKey(USER_ID_KEY)] = id
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getUserId(): String? {
        return try {
            dataStore.data.map {
                it[stringPreferencesKey(USER_ID_KEY)]
            }.first().takeIf { it?.isNotBlank() == true }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun changeLanguage(
        language: Language
    ) {
        return withContext(Dispatchers.IO) {
            try {
                dataStore.edit {
                    it[stringPreferencesKey(LANGUAGE_KEY)] = language.name
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }
        }
    }
}