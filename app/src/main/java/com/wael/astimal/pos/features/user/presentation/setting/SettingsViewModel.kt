package com.wael.astimal.pos.features.user.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.ThemeMode
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import com.wael.astimal.pos.features.user.presentation.login.LoginEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingManger: SettingsManager, private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.onStart {
            initializeUserData()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingsState()
        )

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: Flow<SettingsEffect> = _effect

    fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.Logout -> logout()
            is SettingsEvent.UpdateEditeNameDialog -> updateEditeNameDialog(event.show)
            is SettingsEvent.UpdateName -> updateName(event.name)
            is SettingsEvent.ConfirmUpdateName -> confirmUpdate()
            is SettingsEvent.UpdateThemeMode -> updateTheme(event.mode)
            is SettingsEvent.UpdateLanguage -> updateLanguage(event.language)
            is SettingsEvent.UpdateLanguageDialog -> updateLangeDialog(event.show)
            is SettingsEvent.UpdateThemeDialog -> updateThemeDialog(event.show)
        }
    }

    private fun updateThemeDialog(show: Boolean) {
        _state.update { it.copy(showThemeDialog = show) }
    }

    private fun updateLangeDialog(show: Boolean) {
        _state.update { it.copy(showLanguageDialog = show) }
    }

    private fun updateLanguage(language: Language) {
        viewModelScope.launch {
            settingManger.changeLanguage(language)
        }
    }

    private fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            settingManger.changeTheme(mode)
        }
    }


    private fun confirmUpdate() {
        // todo: Implement the logic to confirm the name update
    }

    private fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    private fun updateEditeNameDialog(show: Boolean) {
        _state.update { it.copy(showEditNameDialog = show) }
    }

    private fun initializeUserData() {
        viewModelScope.launch {
            launch {
                sessionManager.isUserLongedIn().collectLatest { isLoggedIn ->
                    if (isLoggedIn.not()) _effect.emit(SettingsEffect.NavigateToLogin)
                }
                sessionManager.getCurrentSession().collectLatest { result ->
                    _state.update { it.copy(userSession = result) }
                }
            }
            launch {
                settingManger.getThemeMode().collectLatest { result ->
                    _state.update { it.copy(themeMode = result) }
                }
            }
            launch {
                settingManger.getLanguage().collectLatest { result ->
                    _state.update { it.copy(language = result) }
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }
}