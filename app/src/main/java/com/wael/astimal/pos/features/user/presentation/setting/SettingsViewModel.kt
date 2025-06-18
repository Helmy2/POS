package com.wael.astimal.pos.features.user.presentation.setting

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val sessionManager: SessionManager,
    private val navigationController: NavigationController
) : BaseViewModel<SettingsContract.State, SettingsContract.Event, Nothing>(
    reducer = SettingsReducer(),
    initialState = SettingsContract.State()
) {

    override fun handleEvent(event: SettingsContract.Event) {
        when (event) {
            is SettingsContract.Event.LoadSettings -> loadInitialData()
            is SettingsContract.Event.LogoutClicked -> logout()
            is SettingsContract.Event.ThemeChanged -> updateTheme(event.themeMode)
            is SettingsContract.Event.LanguageChanged -> updateLanguage(event.language)
            else -> setState(event)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            launch {
                sessionManager.getCurrentUser().collectLatest { user ->
                    setState(SettingsContract.Event.UserDataLoaded(user))
                    if (user == null) {
                        navigateToLogin()
                    }
                }
            }
            launch {
                settingsManager.getThemeMode().collectLatest { themeMode ->
                    setState(SettingsContract.Event.ThemeModeLoaded(themeMode))
                }
            }
            launch {
                settingsManager.getLanguage().collectLatest { language ->
                    setState(SettingsContract.Event.LanguageLoaded(language))
                }
            }
        }
    }

    private fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            settingsManager.changeTheme(mode)
        }
    }

    private fun updateLanguage(language: Language) {
        viewModelScope.launch {
            settingsManager.changeLanguage(language)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            navigateToLogin()
            delay(100)
            sessionManager.clearSession()
        }
    }

    private suspend fun navigateToLogin() {
        navigationController.navigate(
            destination = Destination.Login,
            popUpToRoute = Destination.Dashboard,
            inclusive = true
        )
    }
}
