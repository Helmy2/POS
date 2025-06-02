package com.wael.astimal.pos.features.user.domain.repository

import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsManager {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun changeTheme(mode: ThemeMode)
    suspend fun changeLanguage(language: Language)
    fun getLanguage(): Flow<Language>

    companion object {
        const val THEME_KEY = "themeKey"
        const val LANGUAGE_KEY = "languageKey"
    }
}