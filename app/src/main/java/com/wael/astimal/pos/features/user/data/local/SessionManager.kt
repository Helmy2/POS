package com.wael.astimal.pos.features.user.data.local

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow

/**
 * Manages the user's session, including login state, credentials, and authentication tokens.
 */
interface SessionManager {
    suspend fun isUserLoggedIn(): Boolean

    suspend fun getCurrentUserId(): Long?

    /**
     * Saves the user ID, credentials, and tokens to persistent storage.
     */
    suspend fun saveUserSession(
        userId: Long,
        email: String,
        password: String,
        accessToken: String,
    )

    suspend fun saveTokens(accessToken: String)

    suspend fun clearSession()

    suspend fun getAccessToken(): String

    fun getSavedEmail(): Flow<String?>
    fun getSavedPassword(): Flow<String?> // Will be encrypted

    companion object {
        val USER_ID = longPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password_encrypted")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    suspend fun refreshBearerTokens(client: HttpClient): BearerTokens?
}