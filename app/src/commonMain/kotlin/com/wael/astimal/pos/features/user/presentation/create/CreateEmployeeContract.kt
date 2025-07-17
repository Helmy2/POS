package com.wael.astimal.pos.features.user.presentation.create

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.user.domain.entity.User

object CreateEmployeeContract {

    data class State(
        val currentUser: User? = null,
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val arName: String = "",
        val enName: String = "",
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false
    ) : Reducer.ViewState {
        val canSave: Boolean
            get() = email.isNotBlank() &&
                    password.isNotBlank() &&
                    confirmPassword.isNotBlank() &&
                    enName.isNotBlank() &&
                    password == confirmPassword &&
                    password.length >= 6
        val canEdit: Boolean get() = currentUser?.isAdmin == true
    }

    sealed interface Event : Reducer.ViewEvent {
        data class EmailChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        data class ConfirmPasswordChanged(val value: String) : Event
        data class ArNameChanged(val value: String) : Event
        data class EnNameChanged(val value: String) : Event
        data class UserLoaded(val user: User?) : Event
        data object TogglePasswordVisibility : Event
        data object ToggleConfirmPasswordVisibility : Event
        data object CreateClicked : Event
        data object CreateSucceeded : Event
        data object CreateFailed : Event
    }

    sealed interface Effect : Reducer.ViewEffect {
        data object NavigateBack : Effect
    }
}
