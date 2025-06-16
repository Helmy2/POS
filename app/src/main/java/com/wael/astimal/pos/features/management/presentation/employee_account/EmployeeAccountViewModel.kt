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
        viewModelScope.launch {
            val currentUser = sessionManager.getCurrentUser().first()
            _state.update {
                it.copy(
                    isAdmin = currentUser?.isAdmin == true,
                    currentUserId = currentUser?.id
                )
            }
        }
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
            is EmployeeAccountEvent.AddTransaction -> addTransaction()
            is EmployeeAccountEvent.EditTransactionClicked -> {
                _state.update {
                    it.copy(
                        transactionToEdit = event.transaction,
                        showEditDialog = true
                    )
                }
            }

            is EmployeeAccountEvent.DeleteTransactionClicked -> deleteTransaction(event.transaction)
            is EmployeeAccountEvent.SaveTransaction -> saveTransaction(event.transaction)
            is EmployeeAccountEvent.DismissEditDialog -> {
                _state.update { it.copy(showEditDialog = false, transactionToEdit = null) }
            }
        }
    }

    private fun addTransaction() {
        viewModelScope.launch {
            // Admins can add transactions for any selected employee.
            if (!_state.value.isAdmin) {
                viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_admin_only)) }
                return@launch
            }

            val currentState = _state.value
            val selectedEmployee = currentState.selectedEmployee
            val currentUserId = _state.value.currentUserId
            val amount = currentState.amount.toDoubleOrNull()

            if (currentUserId == null) {
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

            val newTransaction = EmployeeAccountTransaction(
                localId = 0,
                serverId = null,
                employeeId = selectedEmployee.id,
                createdByEmployeeId = currentUserId,
                type = currentState.transactionType,
                amount = amount,
                relatedCommissionId = null,
                notes = currentState.notes,
                date = System.currentTimeMillis(),
                isSynced = false
            )
            saveTransaction(newTransaction)
        }
    }

    private fun saveTransaction(transaction: EmployeeAccountTransaction) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = if (transaction.localId == 0L) {
                employeeAccountRepository.addManualPayment(transaction)
            } else {
                employeeAccountRepository.updateManualPayment(transaction)
            }

            result.fold(
                onSuccess = {
                    _eventFlow.emit(UiEvent.ShowSnackbar(R.string.transaction_saved_successfully))
                    _state.update {
                        it.copy(
                            isSaving = false,
                            showEditDialog = false,
                            transactionToEdit = null,
                            amount = "",
                            notes = ""
                        )
                    }
                },
                onFailure = {
                    _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_saving_transaction))
                    _state.update { it.copy(isSaving = false) }
                }
            )
        }
    }

    private fun deleteTransaction(transaction: EmployeeAccountTransaction) {
        viewModelScope.launch {
            val result = employeeAccountRepository.deleteManualPayment(transaction.localId)
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.transaction_deleted_successfully))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_deleting_transaction))
            }
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
}
