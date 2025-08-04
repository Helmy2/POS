package com.wael.astimal.pos.features.user.presentation.employee

import com.wael.astimal.pos.core.base.mvi.Reducer

class EmployeeReducer :
    Reducer<EmployeeContract.State, EmployeeContract.Event, EmployeeContract.Effect> {
    override fun reduce(
        previousState: EmployeeContract.State,
        event: EmployeeContract.Event
    ): Pair<EmployeeContract.State, EmployeeContract.Effect?> {
        return when (event) {
            // Form Events
            is EmployeeContract.Event.EmailChanged -> previousState.copy(email = event.value) to null
            is EmployeeContract.Event.PasswordChanged -> previousState.copy(password = event.value) to null
            is EmployeeContract.Event.ConfirmPasswordChanged -> previousState.copy(confirmPassword = event.value) to null
            is EmployeeContract.Event.ArNameChanged -> previousState.copy(arName = event.value) to null
            is EmployeeContract.Event.EnNameChanged -> previousState.copy(enName = event.value) to null
            is EmployeeContract.Event.CanHandlePrivatePartnerChanged -> previousState.copy(
                canHandlePrivatePartner = event.value
            ) to null
            is EmployeeContract.Event.TogglePasswordVisibility -> previousState.copy(
                isPasswordVisible = !previousState.isPasswordVisible
            ) to null

            is EmployeeContract.Event.ToggleConfirmPasswordVisibility -> previousState.copy(
                isConfirmPasswordVisible = !previousState.isConfirmPasswordVisible
            ) to null


            // Data Loading
            is EmployeeContract.Event.CurrentUserLoaded -> previousState.copy(currentUser = event.user) to null
            is EmployeeContract.Event.AllEmployeesLoaded -> previousState.copy(allEmployees = event.employees) to null

            // Search
            is EmployeeContract.Event.SearchQueryChanged -> previousState.copy(searchQuery = event.query) to null
            is EmployeeContract.Event.SearchActiveChanged -> previousState.copy(isSearchActive = event.isActive) to null
            is EmployeeContract.Event.EmployeeSelected -> previousState.copy(
                selectedEmployee = event.employee,
                email = event.employee.email ?: "",
                arName = event.employee.localizedName.arName ?: "",
                enName = event.employee.localizedName.enName ?: "",
                password = "",
                confirmPassword = "",
                isSearchActive = false,
                canHandlePrivatePartner = event.employee.canHandlePrivatePartner
            ) to null

            // User Actions
            is EmployeeContract.Event.NewEmployeeClicked, is EmployeeContract.Event.SaveSucceeded, EmployeeContract.Event.DeleteSucceeded -> previousState.copy(
                selectedEmployee = null,
                email = "",
                password = "",
                confirmPassword = "",
                arName = "",
                enName = ""
            ) to null

            is EmployeeContract.Event.BackClicked -> {
                if (previousState.isSearchActive) {
                    previousState.copy(isSearchActive = false) to null
                } else {
                    previousState to EmployeeContract.Effect.NavigateBack
                }
            }

            // Async Operations
            is EmployeeContract.Event.SaveClicked -> previousState.copy(isLoading = true) to null
            is EmployeeContract.Event.SaveFailed -> previousState.copy(isLoading = false) to null
            is EmployeeContract.Event.DeleteClicked -> previousState.copy(isLoading = true) to null
            is EmployeeContract.Event.DeleteFailed -> previousState.copy(isLoading = false) to null
        }
    }
}
