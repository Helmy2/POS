package com.wael.astimal.pos.features.user.data.local

import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsManager {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun changeTheme(mode: ThemeMode)
    suspend fun changeLanguage(language: Language)
    fun getLanguage(): Flow<Language>
    suspend fun changeUserId(id: String)
    suspend fun getUserId(): String?
    suspend fun saveFcmToken(token: String)
    suspend fun getFcmToken(): String?
}