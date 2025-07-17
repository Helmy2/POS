package com.wael.astimal.pos.features.user.presentation.create

import com.wael.astimal.pos.core.base.mvi.Reducer

class CreateEmployeeReducer :
    Reducer<CreateEmployeeContract.State, CreateEmployeeContract.Event, CreateEmployeeContract.Effect> {
    override fun reduce(
        previousState: CreateEmployeeContract.State,
        event: CreateEmployeeContract.Event
    ): Pair<CreateEmployeeContract.State, CreateEmployeeContract.Effect?> {
        return when (event) {
            is CreateEmployeeContract.Event.EmailChanged -> previousState.copy(email = event.value) to null
            is CreateEmployeeContract.Event.PasswordChanged -> previousState.copy(password = event.value) to null
            is CreateEmployeeContract.Event.ConfirmPasswordChanged -> previousState.copy(
                confirmPassword = event.value
            ) to null

            is CreateEmployeeContract.Event.ArNameChanged -> previousState.copy(arName = event.value) to null
            is CreateEmployeeContract.Event.EnNameChanged -> previousState.copy(enName = event.value) to null
            is CreateEmployeeContract.Event.TogglePasswordVisibility -> previousState.copy(
                isPasswordVisible = !previousState.isPasswordVisible
            ) to null

            is CreateEmployeeContract.Event.ToggleConfirmPasswordVisibility -> previousState.copy(
                isConfirmPasswordVisible = !previousState.isConfirmPasswordVisible
            ) to null

            is CreateEmployeeContract.Event.UserLoaded -> previousState.copy(currentUser = event.user) to null

            is CreateEmployeeContract.Event.CreateClicked -> previousState.copy(isLoading = true) to null
            is CreateEmployeeContract.Event.CreateSucceeded -> previousState.copy(isLoading = false) to CreateEmployeeContract.Effect.NavigateBack
            is CreateEmployeeContract.Event.CreateFailed -> previousState.copy(isLoading = false) to null
        }
    }
}
