package com.wael.astimal.pos.features.user.domain.repository

import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface SessionManager {
    fun isUserLoggedInFlow(): Flow<Boolean>
    fun getCurrentUser(): Flow<User?>
    suspend fun saveSession(
        userId: Long,
        authToken: String,
    ): Result<Unit>

    suspend fun clearSession()
    fun getAuthToken(): Flow<String?>

    companion object {
        val USER_ID = longPreferencesKey("user_id")
        val AUTH_TOKEN = byteArrayPreferencesKey("auth_token")
    }
}