package com.wael.astimal.pos.features.management.presentation.employee_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.domain.repository.EmployeeAccountRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmployeeAccountViewModel(
    private val employeeAccountRepository: EmployeeAccountRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EmployeeAccountState())
    val state: StateFlow<EmployeeAccountState> = _state.asStateFlow()

    init {
        loadEmployees()
    }

    fun onEvent(event: EmployeeAccountEvent) {
        when (event) {
            is EmployeeAccountEvent.SelectEmployee -> {
                _state.update { it.copy(selectedEmployee = event.employee, isLoading = true) }
                fetchAccountDetails(event.employee.id)
            }
            is EmployeeAccountEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            userRepository.getEmployeesFlow()
                .catch { _state.update { it.copy(error = R.string.error_loading_employees) } }
                .collect { employees ->
                    _state.update { it.copy(employees = employees) }
                }
        }
    }

    private fun fetchAccountDetails(employeeId: Long) {
        employeeAccountRepository.getEmployeeAccount(employeeId)
            .onEach { account ->
                _state.update {
                    it.copy(
                        employeeAccount = account,
                        isLoading = false
                    )
                }
            }
            .catch {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = R.string.error_fetching_account_details
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}