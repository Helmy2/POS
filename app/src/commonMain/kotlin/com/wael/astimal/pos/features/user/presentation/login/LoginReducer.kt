package com.wael.astimal.pos.features.user.presentation.login

import com.wael.astimal.pos.core.base.mvi.Reducer

class LoginReducer : Reducer<LoginContract.State, LoginContract.Event, Nothing> {
    override fun reduce(
        previousState: LoginContract.State,
        event: LoginContract.Event
    ): Pair<LoginContract.State, Nothing?> {
        return when (event) {
            is LoginContract.Event.EmailChanged ->
                previousState.copy(email = event.value) to null

            is LoginContract.Event.PasswordChanged ->
                previousState.copy(password = event.value) to null

            is LoginContract.Event.TogglePasswordVisibility ->
                previousState.copy(isPasswordVisible = !previousState.isPasswordVisible) to null

            is LoginContract.Event.LoginClicked ->
                previousState.copy(loading = true) to null

            is LoginContract.Event.LoginSuccess ->
                previousState.copy(loading = false) to null

            is LoginContract.Event.LoginFailure -> previousState.copy(
                loading = false,
            ) to null
        }
    }
}