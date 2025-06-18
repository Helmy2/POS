package com.wael.astimal.pos.features.user.presentation.setting

import com.wael.astimal.pos.core.base.mvi.Reducer

class SettingsReducer : Reducer<SettingsContract.State, SettingsContract.Event, Nothing> {
    override fun reduce(
        previousState: SettingsContract.State,
        event: SettingsContract.Event
    ): Pair<SettingsContract.State, Nothing?> {
        return when (event) {
            is SettingsContract.Event.ThemeDialogVisibilityChanged ->
                previousState.copy(showThemeDialog = event.show) to null

            is SettingsContract.Event.LanguageDialogVisibilityChanged ->
                previousState.copy(showLanguageDialog = event.show) to null

            is SettingsContract.Event.UserDataLoaded ->
                previousState.copy(user = event.user) to null

            is SettingsContract.Event.ThemeModeLoaded ->
                previousState.copy(themeMode = event.themeMode) to null

            is SettingsContract.Event.LanguageLoaded ->
                previousState.copy(language = event.language) to null

            is SettingsContract.Event.LoadSettings,
            is SettingsContract.Event.LogoutClicked,
            is SettingsContract.Event.ThemeChanged,
            is SettingsContract.Event.LanguageChanged -> previousState to null
        }
    }
}
