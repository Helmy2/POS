package com.wael.astimal.pos.features.user.presentation.setting

import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.features.user.domain.entity.User

data class SettingsState(
    val user: User? = null,
    val showEditNameDialog: Boolean = false,
    val showEditProfilePictureDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val name: String = "",
    val profilePictureLoading: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val language: Language = Language.English,
)