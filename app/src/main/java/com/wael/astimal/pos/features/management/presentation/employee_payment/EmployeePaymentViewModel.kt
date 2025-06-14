package com.wael.astimal.pos.features.management.presentation.employee_payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.repository.EmployeeAccountRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmployeePaymentViewModel(
    private val employeeAccountRepository: EmployeeAccountRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(EmployeePaymentState())
    val state: StateFlow<EmployeePaymentState> = _state.asStateFlow()

    init {
        loadEmployees()
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            userRepository.getEmployeesFlow().collect { employees ->
                _state.update { it.copy(employees = employees) }
            }
        }
    }

    fun onEvent(event: EmployeePaymentEvent) {
        when (event) {
            is EmployeePaymentEvent.SelectEmployee -> _state.update { it.copy(selectedEmployee = event.employee) }
            is EmployeePaymentEvent.SelectTransactionType -> _state.update { it.copy(transactionType = event.type) }
            is EmployeePaymentEvent.UpdateAmount -> _state.update { it.copy(amount = event.amount) }
            is EmployeePaymentEvent.UpdateNotes -> _state.update { it.copy(notes = event.notes) }
            is EmployeePaymentEvent.SavePayment -> savePayment()
            is EmployeePaymentEvent.ClearError -> _state.update { it.copy(error = null) }
            is EmployeePaymentEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun savePayment() {
        viewModelScope.launch {
            val currentState = _state.value
            val selectedEmployee = currentState.selectedEmployee
            val currentUser = sessionManager.getCurrentUser().first()
            var amount = currentState.amount.toDoubleOrNull()

            if (currentUser == null) {
                _state.update { it.copy(error = R.string.user_not_identified) }
                return@launch
            }
            if (selectedEmployee == null) {
                _state.update { it.copy(error = R.string.please_select_an_employee) }
                return@launch
            }
            if (amount == null || amount <= 0) {
                _state.update { it.copy(error = R.string.invalid_amount) }
                return@launch
            }

            if (currentState.transactionType == EmployeeTransactionType.DEDUCTION || currentState.transactionType == EmployeeTransactionType.WITHDRAWAL) {
                amount *= -1
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
                _state.update {
                    it.copy(
                        snackbarMessage = R.string.payment_saved_successfully,
                        selectedEmployee = null,
                        amount = "",
                        notes = ""
                    )
                }
            }, onFailure = {
                _state.update { it.copy(error = R.string.error_saving_payment) }
            })
        }
    }
}
