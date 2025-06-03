package com.wael.astimal.pos.features.user.presentation.setting

import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.features.user.domain.entity.UserSession

data class SettingsState(
    val userSession: UserSession? = null,
    val showEditNameDialog: Boolean = false,
    val showEditProfilePictureDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val name: String = "",
    val profilePictureLoading: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val language: Language = Language.English,
)

sealed interface SettingsEvent {
    data object Logout : SettingsEvent
    data class UpdateEditeNameDialog(val show: Boolean) : SettingsEvent
    data class UpdateThemeDialog(val show: Boolean) : SettingsEvent
    data class UpdateLanguageDialog(val show: Boolean) : SettingsEvent
    data class UpdateName(val name: String) : SettingsEvent
    data class UpdateThemeMode(val mode: ThemeMode) : SettingsEvent
    data class UpdateLanguage(val language: Language) : SettingsEvent
    data object ConfirmUpdateName : SettingsEvent
}

sealed class SettingsEffect {
    data object NavigateToLogin : SettingsEffect()
}



