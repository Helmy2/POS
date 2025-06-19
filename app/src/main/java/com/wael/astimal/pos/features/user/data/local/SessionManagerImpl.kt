package com.wael.astimal.pos.features.user.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.wael.astimal.pos.core.util.BASE_URL
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
            preferences[SessionManager.PASSWORD] = Crypto.encrypt(password)
            preferences[SessionManager.ACCESS_TOKEN] = accessToken
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        dataStore.edit { preferences ->
            preferences[SessionManager.ACCESS_TOKEN] = accessToken
            refreshToken?.let {
                preferences[SessionManager.REFRESH_TOKEN] = it
            }
        }
    }

    override suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            dataStore.edit { it.clear() }
        }
    }

    override fun getAccessToken(): Flow<String?> =
        dataStore.data.catchIO().map { it[SessionManager.ACCESS_TOKEN] }

    override fun getRefreshToken(): Flow<String?> =
        dataStore.data.catchIO().map { it[SessionManager.REFRESH_TOKEN] }

    override fun getSavedEmail(): Flow<String?> =
        dataStore.data.catchIO().map { it[SessionManager.EMAIL] }

    override fun getSavedPassword(): Flow<String?> {
        return dataStore.data.catchIO().map { preferences ->
            preferences[SessionManager.PASSWORD]?.let { encryptedPassword ->
                Crypto.decrypt(encryptedPassword)
            }
        }
    }

    override suspend fun getBearerTokens(): BearerTokens? {
        val accessToken = runBlocking { getAccessToken().first() }
        val refreshToken = runBlocking { getRefreshToken().first() }

        return if (accessToken != null && refreshToken != null) {
            BearerTokens(accessToken, refreshToken)
        } else {
            null
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
            client.post("${BASE_URL}/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(email, password)
                )
            }
        }.getOrNull()?.body<LoginResponse>()

        return response?.let {
            val newTokens = BearerTokens(it.token, it.token)
            saveTokens(it.token, it.token)
            newTokens
        }
    }

    private fun Flow<Preferences>.catchIO(): Flow<Preferences> {
        return this.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
    }
}