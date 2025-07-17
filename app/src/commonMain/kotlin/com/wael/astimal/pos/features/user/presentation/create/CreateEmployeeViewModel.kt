package com.wael.astimal.pos.features.user.presentation.create

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.employee_created_successfully
import pos.app.generated.resources.error_creating_employee
import pos.app.generated.resources.error_password_too_short
import pos.app.generated.resources.error_passwords_do_not_match
import pos.app.generated.resources.error_some_field_are_required

class CreateEmployeeViewModel(
    private val userRepository: UserRepository, private val snackbarController: SnackbarController
) : BaseViewModel<CreateEmployeeContract.State, CreateEmployeeContract.Event, CreateEmployeeContract.Effect>(
    reducer = CreateEmployeeReducer(), initialState = CreateEmployeeContract.State()
) {
    override fun handleEvent(event: CreateEmployeeContract.Event) {
        when (event) {
            is CreateEmployeeContract.Event.CreateClicked -> createUser()
            else -> setState(event)
        }
    }

    init {
        viewModelScope.launch {
            setState(
                CreateEmployeeContract.Event.UserLoaded(
                    userRepository.getCurrentUser()
                )
            )
        }
    }

    private fun createUser() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave) {
                val message = when {
                    currentState.password.length <= 6 -> StringResource.FromResource(Res.string.error_password_too_short)
                    currentState.password != currentState.confirmPassword -> StringResource.FromResource(
                        Res.string.error_passwords_do_not_match
                    )

                    else -> StringResource.FromResource(Res.string.error_some_field_are_required)
                }
                snackbarController.sendEvent(SnackbarEvent(message))
                return@launch
            }


            setState(CreateEmployeeContract.Event.CreateClicked)
            userRepository.createUser(
                email = currentState.email,
                arName = currentState.arName,
                enName = currentState.enName,
                password = currentState.password
            ).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.employee_created_successfully)))
                setState(CreateEmployeeContract.Event.CreateSucceeded)
            }.onFailure {
                snackbarController.sendEvent(
                    SnackbarEvent(
                        StringResource.FromResource(
                            Res.string.error_creating_employee, it.message ?: ""
                        )
                    )
                )
                setState(CreateEmployeeContract.Event.CreateFailed)
            }
        }
    }
}
