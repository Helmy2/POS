package com.wael.astimal.pos.features.user.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.wael.astimal.pos.core.util.ApiRoutes
import com.wael.astimal.pos.features.user.data.remote.dto.LoginRequest
import com.wael.astimal.pos.features.user.data.remote.dto.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SessionManagerImpl(
    private val dataStore: DataStore<Preferences>,
) : SessionManager {

    override suspend fun isUserLoggedIn(): Boolean {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[SessionManager.USER_ID] != null
        }.first()
    }

    override suspend fun getCurrentUserId(): Long? {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[SessionManager.USER_ID]
        }.first()
    }

    override suspend fun saveUserSession(
        userId: Long, email: String, password: String, accessToken: String
    ) {
        dataStore.edit { preferences ->
            preferences[SessionManager.USER_ID] = userId
            preferences[SessionManager.EMAIL] = email
            preferences[SessionManager.PASSWORD] = password
            preferences[SessionManager.ACCESS_TOKEN] = accessToken
        }
    }

    override suspend fun saveTokens(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[SessionManager.ACCESS_TOKEN] = accessToken
        }
    }

    override suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            dataStore.edit { it.clear() }
        }
    }

    override suspend fun getAccessToken(): String =
        dataStore.data.catchIO().map { it[SessionManager.ACCESS_TOKEN] }.first() ?: ""

    override fun getSavedEmail(): Flow<String?> =
        dataStore.data.catchIO().map { it[SessionManager.EMAIL] }

    override fun getSavedPassword(): Flow<String?> {
        return dataStore.data.catchIO().map { preferences ->
            preferences[SessionManager.PASSWORD]
        }
    }

    override suspend fun refreshBearerTokens(client: HttpClient): BearerTokens? {
        val email = runBlocking { getSavedEmail().first() }
        val password = runBlocking { getSavedPassword().first() }

        if (email == null || password == null) {
            // If we have no credentials, we can't refresh. Return null.
            return null
        }

        val response = runCatching {
            client.post(ApiRoutes.LOGIN) {
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(email, password)
                )
            }
        }.getOrNull()?.body<LoginResponse>()

        return response?.let {
            val newTokens = BearerTokens(it.token, null)
            saveTokens(it.token)
            newTokens
        }
    }

    private fun Flow<Preferences>.catchIO(): Flow<Preferences> {
        return this.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
    }
}