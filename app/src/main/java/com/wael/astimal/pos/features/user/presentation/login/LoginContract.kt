package com.wael.astimal.pos.features.user.presentation.login

import androidx.annotation.StringRes

data class LoginState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    @StringRes val usernameError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val isPasswordVisible: Boolean = false,
)

sealed class LoginEvent {
    data class UsernameChanged(val username: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data object TogglePasswordVisibility : LoginEvent()
    data object Login : LoginEvent()
}

sealed class LoginEffect {
    data class ShowError(@StringRes val message: Int) : LoginEffect()
    data object NavigateToHome : LoginEffect()
}

