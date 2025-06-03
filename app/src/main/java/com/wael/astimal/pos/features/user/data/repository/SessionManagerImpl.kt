package com.wael.astimal.pos.features.user.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.wael.astimal.pos.features.user.domain.entity.UserRole
import com.wael.astimal.pos.features.user.domain.entity.UserSession
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SessionManagerImpl(
    private val dataStore: DataStore<Preferences>
) : SessionManager {

    override fun getCurrentSession(): Flow<UserSession> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val userId = preferences[SessionManager.USER_ID] ?: UserSession.DEFAULT_USER_ID
            val userName = preferences[SessionManager.USER_NAME] ?: ""
            val userEmail = preferences[SessionManager.USER_EMAIL] ?: ""
            val roleName = preferences[SessionManager.USER_ROLE]
            val userRole = try {
                if (roleName != null) UserRole.valueOf(roleName) else UserRole.UNKNOWN
            } catch (_: IllegalArgumentException) {
                UserRole.UNKNOWN
            }
            val authToken = preferences[SessionManager.AUTH_TOKEN]?.let {
                Crypto.decrypt(it)
            }.toString()

            UserSession(
                userId = userId,
                userName = userName,
                userEmail = userEmail,
                userRole = userRole,
                authToken = authToken,
            )
        }
    }

    override suspend fun saveSession(session: UserSession): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                dataStore.edit { preferences ->
                    preferences[SessionManager.USER_ID] = session.userId
                    preferences[SessionManager.USER_NAME] = session.userName
                    preferences[SessionManager.USER_EMAIL] = session.userEmail
                    preferences[SessionManager.USER_ROLE] = session.userRole.name
                    preferences[SessionManager.AUTH_TOKEN] =
                        Crypto.encrypt(session.authToken.toByteArray())
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