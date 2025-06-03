package com.wael.astimal.pos.features.user.domain.repository

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wael.astimal.pos.features.user.domain.entity.UserSession
import kotlinx.coroutines.flow.Flow

interface SessionManager {
    fun getCurrentSession(): Flow<UserSession>
    suspend fun saveSession(session: UserSession)
    suspend fun clearSession()
    fun getAuthToken(): Flow<String?>

    companion object{
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }
}