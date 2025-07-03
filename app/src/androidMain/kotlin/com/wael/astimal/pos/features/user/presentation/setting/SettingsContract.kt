package com.wael.astimal.pos.features.user.presentation.setting

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.features.user.domain.entity.User

object SettingsContract {

    data class State(
        val user: User? = null,
        val showThemeDialog: Boolean = false,
        val showLanguageDialog: Boolean = false,
        val themeMode: ThemeMode = ThemeMode.System,
        val language: Language = Language.English,
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadSettings : Event
        data object LogoutClicked : Event
        data class ThemeDialogVisibilityChanged(val show: Boolean) : Event
        data class LanguageDialogVisibilityChanged(val show: Boolean) : Event
        data class ThemeChanged(val themeMode: ThemeMode) : Event
        data class LanguageChanged(val language: Language) : Event

        data class UserDataLoaded(val user: User?) : Event
        data class ThemeModeLoaded(val themeMode: ThemeMode) : Event
        data class LanguageLoaded(val language: Language) : Event
    }
}
