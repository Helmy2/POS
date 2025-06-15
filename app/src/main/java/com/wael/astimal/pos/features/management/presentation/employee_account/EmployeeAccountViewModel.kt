package com.wael.astimal.pos.features.management.presentation.employee_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.repository.EmployeeAccountRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmployeeAccountViewModel(
    private val employeeAccountRepository: EmployeeAccountRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(EmployeeAccountState())
    val state: StateFlow<EmployeeAccountState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadEmployees()
    }

    fun onEvent(event: EmployeeAccountEvent) {
        when (event) {
            is EmployeeAccountEvent.SelectEmployee -> {
                _state.update { it.copy(selectedEmployee = event.employee, loading = true) }
                fetchAccountDetails(event.employee.id)
            }
            is EmployeeAccountEvent.SelectTransactionType -> _state.update { it.copy(transactionType = event.type) }
            is EmployeeAccountEvent.UpdateAmount -> _state.update { it.copy(amount = event.amount) }
            is EmployeeAccountEvent.UpdateNotes -> _state.update { it.copy(notes = event.notes) }
            is EmployeeAccountEvent.SavePayment -> savePayment()
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            userRepository.getEmployeesFlow()
                .catch {
                    _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_loading_employees))
                }
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
                        loading = false
                    )
                }
            }
            .catch {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_fetching_account_details))
            }
            .launchIn(viewModelScope)
    }

    private fun savePayment() {
        viewModelScope.launch {
            val currentState = _state.value
            val selectedEmployee = currentState.selectedEmployee
            val currentUser = sessionManager.getCurrentUser().first()
            val amount = currentState.amount.toDoubleOrNull()

            if (currentUser == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.user_not_identified))
                return@launch
            }
            if (selectedEmployee == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.please_select_an_employee))
                return@launch
            }
            if (amount == null || amount <= 0) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.invalid_amount))
                return@launch
            }

            val transaction = EmployeeAccountTransaction(
                localId = 0,
                serverId = null,
                employeeId = selectedEmployee.id,
                createdByEmployeeId = currentUser.id,
                type = currentState.transactionType,
                amount = amount,
                relatedCommissionId = null,
                notes = currentState.notes,
                date = System.currentTimeMillis(),
                isSynced = false
            )

            employeeAccountRepository.addManualPayment(transaction).fold(onSuccess = {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.payment_saved_successfully))
            }, onFailure = {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_saving_payment))
            })
        }
    }
}