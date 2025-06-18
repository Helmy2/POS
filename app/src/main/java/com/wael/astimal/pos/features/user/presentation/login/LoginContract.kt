package com.wael.astimal.pos.features.user.presentation.login

import com.wael.astimal.pos.core.base.mvi.Reducer

object LoginContract {

    data class State(
        val username: String = "",
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val loading: Boolean = false,
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data class UsernameChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        data object TogglePasswordVisibility : Event
        data object LoginClicked : Event

        data class LoginSuccess(val username: String) : Event
        data object LoginFailure : Event
    }
}