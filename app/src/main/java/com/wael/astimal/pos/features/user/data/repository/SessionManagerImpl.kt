package com.wael.astimal.pos.features.user.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                UserSession(
                    userId = preferences[SessionManager.USER_ID],
                    userName = preferences[SessionManager.USER_NAME],
                    userEmail = preferences[SessionManager.USER_EMAIL],
                    userRole = preferences[SessionManager.USER_ROLE],
                    authToken = preferences[SessionManager.AUTH_TOKEN],
                )
            }
    }

    override suspend fun saveSession(session: UserSession) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                session.userId?.let { preferences[SessionManager.USER_ID] = it } ?: preferences.remove(SessionManager.USER_ID)
                session.userName?.let { preferences[SessionManager.USER_NAME] = it } ?: preferences.remove(SessionManager.USER_NAME)
                session.userEmail?.let { preferences[SessionManager.USER_EMAIL] = it } ?: preferences.remove(SessionManager.USER_EMAIL)
                session.userRole?.let { preferences[SessionManager.USER_ROLE] = it } ?: preferences.remove(SessionManager.USER_ROLE)
                session.authToken?.let { preferences[SessionManager.AUTH_TOKEN] = it } ?: preferences.remove(SessionManager.AUTH_TOKEN)
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
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[SessionManager.AUTH_TOKEN]
            }
    }
}