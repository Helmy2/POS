package com.wael.astimal.pos.features.user.presentation.employee

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.user.domain.entity.User

object EmployeeContract {

    data class State(
        val currentUser: User? = null,
        val selectedEmployee: User? = null,

        // Form Input State
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val arName: String = "",
        val enName: String = "",

        // UI State
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,

        // Search State
        val searchQuery: String = "",
        val isSearchActive: Boolean = SHOULD_SHOW_SHEATH_ON_START,
        val allEmployees: List<User> = emptyList()

    ) : Reducer.ViewState {
        val isNewEmployee: Boolean get() = selectedEmployee == null
        val canSave: Boolean
            get() = if (isNewEmployee) {
                email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        enName.isNotBlank() &&
                        password == confirmPassword &&
                        password.length >= 6
            } else {
                email.isNotBlank() && enName.isNotBlank() && (password.isEmpty() || (password.length >= 6 && password == confirmPassword))
            }
        val canEdit: Boolean get() = currentUser?.isAdmin == true
    }

    sealed interface Event : Reducer.ViewEvent {
        // Form Events
        data class EmailChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        data class ConfirmPasswordChanged(val value: String) : Event
        data class ArNameChanged(val value: String) : Event
        data class EnNameChanged(val value: String) : Event
        data object TogglePasswordVisibility : Event
        data object ToggleConfirmPasswordVisibility : Event

        // Data Loading Events
        data class CurrentUserLoaded(val user: User?) : Event
        data class AllEmployeesLoaded(val employees: List<User>) : Event

        // User Action Events
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object NewEmployeeClicked : Event
        data object BackClicked : Event

        // Search Events
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class EmployeeSelected(val employee: User) : Event

        // Async Operation Events
        data object SaveSucceeded : Event
        data object SaveFailed : Event
        data object DeleteSucceeded : Event
        data object DeleteFailed : Event
    }

    sealed interface Effect : Reducer.ViewEffect {
        data object NavigateBack : Effect
    }
}
