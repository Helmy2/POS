package com.wael.astimal.pos.features.user.presentation.employee

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.employee_deleted_successfully
import pos.app.generated.resources.employee_saved_successfully
import pos.app.generated.resources.error_deleting_employee
import pos.app.generated.resources.error_password_too_short
import pos.app.generated.resources.error_passwords_do_not_match
import pos.app.generated.resources.error_saving_employee
import pos.app.generated.resources.error_some_field_are_required

class EmployeeViewModel(
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
) : BaseViewModel<EmployeeContract.State, EmployeeContract.Event, EmployeeContract.Effect>(
    reducer = EmployeeReducer(),
    initialState = EmployeeContract.State()
) {

    val filteredEmployeesState: StateFlow<List<User>> =
        combine(state, state.map { it.searchQuery }) { state, query ->
            if (query.isBlank()) {
                state.allEmployees
            } else {
                state.allEmployees.filter {
                    it.localizedName.enName!!.contains(query, ignoreCase = true) ||
                            it.localizedName.arName!!.contains(query, ignoreCase = true) ||
                            it.email!!.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCurrentUser()
        loadAllEmployees()
    }

    override fun handleEvent(event: EmployeeContract.Event) {
        when (event) {
            is EmployeeContract.Event.SaveClicked -> saveUser()
            is EmployeeContract.Event.DeleteClicked -> deleteUser()
            else -> setState(event)
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            setState(EmployeeContract.Event.CurrentUserLoaded(userRepository.getCurrentUser()))
        }
    }

    private fun loadAllEmployees() {
        viewModelScope.launch {
            userRepository.getEmployeesFlow().collect {
                println(it)
                setState(EmployeeContract.Event.AllEmployeesLoaded(it))
            }
        }
    }

    private fun saveUser() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave) {
                val message = when {
                    !currentState.isNewEmployee && currentState.password.isNotEmpty() && currentState.password.length < 6 -> StringResource.FromResource(
                        Res.string.error_password_too_short
                    )

                    !currentState.isNewEmployee && currentState.password.isNotEmpty() && currentState.password != currentState.confirmPassword -> StringResource.FromResource(
                        Res.string.error_passwords_do_not_match
                    )

                    else -> StringResource.FromResource(Res.string.error_some_field_are_required)
                }
                snackbarController.sendEvent(SnackbarEvent(message))
                return@launch
            }

            setState(EmployeeContract.Event.SaveClicked)
            val result = if (currentState.isNewEmployee) {
                userRepository.createUser(
                    email = currentState.email,
                    arName = currentState.arName,
                    enName = currentState.enName,
                    password = currentState.password
                )
            } else {
                userRepository.updateUser(
                    id = currentState.selectedEmployee!!.id,
                    arName = currentState.arName,
                    enName = currentState.enName,
                    password = currentState.password.takeIf { it.isNotEmpty() },
                    email = currentState.email
                )
            }

            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.employee_saved_successfully)))
                setState(EmployeeContract.Event.SaveSucceeded)
            }.onFailure {
                snackbarController.sendEvent(
                    SnackbarEvent(
                        StringResource.FromResource(
                            Res.string.error_saving_employee,
                            it.message ?: ""
                        )
                    )
                )
                setState(EmployeeContract.Event.SaveFailed)
            }
        }
    }

    private fun deleteUser() {
        viewModelScope.launch {
            val employeeIdToDelete = state.value.selectedEmployee?.id ?: return@launch
            setState(EmployeeContract.Event.DeleteClicked)
            userRepository.deleteUser(employeeIdToDelete).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.employee_deleted_successfully)))
                setState(EmployeeContract.Event.DeleteSucceeded)
            }.onFailure {
                snackbarController.sendEvent(
                    SnackbarEvent(
                        StringResource.FromResource(
                            Res.string.error_deleting_employee,
                            it.message ?: ""
                        )
                    )
                )
                setState(EmployeeContract.Event.DeleteFailed)
            }
        }
    }
}
