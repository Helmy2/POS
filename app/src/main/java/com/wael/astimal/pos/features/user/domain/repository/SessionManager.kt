package com.wael.astimal.pos.features.user.domain.repository

import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface SessionManager {
    fun getCurrentUser(): Flow<User?>
    suspend fun saveSession(
        userId: Int,
        authToken: String,
    ): Result<Unit>

    suspend fun clearSession()
    fun getAuthToken(): Flow<String?>

    companion object {
        val USER_ID = intPreferencesKey("user_id")
        val AUTH_TOKEN = byteArrayPreferencesKey("auth_token")
    }
}