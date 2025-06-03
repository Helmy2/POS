package com.wael.astimal.pos.features.user.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.user.domain.entity.UserSession
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val sessionManger: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.onStart {
        observeCurrentUser()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoginState())

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: Flow<LoginEffect> = _effect

    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsernameChanged -> updateEmail(event.username)
            is LoginEvent.PasswordChanged -> updatePassword(event.password)
            LoginEvent.TogglePasswordVisibility -> togglePasswordVisibility()
            LoginEvent.Login -> login()
        }
    }

    // Observe current user for the desktop login with google
    private fun observeCurrentUser() {
        viewModelScope.launch {
            sessionManger.isUserLongedIn().collectLatest {
                if (it) _effect.emit(LoginEffect.NavigateToHome)
            }
        }
    }

    private fun updateEmail(value: String) {
        _state.update { it.copy(username = value, usernameError = null) }
    }

    private fun updatePassword(value: String) {
        _state.update { it.copy(password = value, passwordError = null) }
    }

    private fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val result = sessionManger.saveSession(
                UserSession(
                    userId = 413, // Assuming userId is not needed for login
                    userName = state.value.username,
                    userEmail = null, // Assuming email is not needed for login
                    userRole = null, // Assuming role is not needed for login
                    authToken = null // Assuming token is not needed for login
                )
            )
            handleAuthResult(result) {
                _effect.emit(LoginEffect.NavigateToHome)
            }
        }
    }

    private suspend fun handleAuthResult(result: Result<Unit>, onSuccess: suspend () -> Unit) {
        _state.update { it.copy(loading = false) }
        result.fold(
            onSuccess = { onSuccess() },
            onFailure = { _effect.emit(LoginEffect.ShowError(R.string.something_went_wrong)) },
        )
    }
}

