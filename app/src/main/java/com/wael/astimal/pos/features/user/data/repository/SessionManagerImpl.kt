package com.wael.astimal.pos.features.user.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.wael.astimal.pos.features.user.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.local.Crypto
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SessionManagerImpl(
    private val dataStore: DataStore<Preferences>,
    private val userDao: UserDao,
) : SessionManager {

    override fun isUserLoggedInFlow(): Flow<Boolean> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[SessionManager.USER_ID] != null
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val userId = preferences[SessionManager.USER_ID]
            userId ?: return@map null
            userDao.getUserById(userId)?.toDomain()
        }
    }

    override suspend fun saveSession(
        userId: Long,
        authToken: String,
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                dataStore.edit { preferences ->
                    preferences[SessionManager.USER_ID] = userId
                    preferences[SessionManager.AUTH_TOKEN] =
                        Crypto.encrypt(authToken.toByteArray())
                }
                Unit
            }
        }
    }

    override suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

    override fun getAuthToken(): Flow<String?> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[SessionManager.AUTH_TOKEN]?.let {
                Crypto.decrypt(it)
            }.toString()
        }
    }
}