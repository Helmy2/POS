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
    private var transactions: List<EmployeeAccountTransaction> = emptyList()

    init {
        viewModelScope.launch {
            val currentUser = sessionManager.getCurrentUser().first()
            _state.update { it.copy(currentUser = currentUser) }
        }
        viewModelScope.launch {
            userRepository.getEmployeesFlow().catch {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_loading_employees))
            }.collect { employees ->
                _state.update { it.copy(employees = employees) }
            }
        }
        viewModelScope.launch {
            employeeAccountRepository.getAllTransaction().catch {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_loading_transactione))
            }.collect { result ->
                transactions = result
                _state.update { it.copy(transactions = result) }
            }
        }
    }

    fun onEvent(event: EmployeeAccountEvent) {
        when (event) {
            is EmployeeAccountEvent.SelectTransactionType -> _state.update { it.copy(transactionType = event.type) }
            is EmployeeAccountEvent.UpdateAmount -> _state.update { it.copy(amount = event.amount) }
            is EmployeeAccountEvent.UpdateNotes -> _state.update { it.copy(notes = event.notes) }
            is EmployeeAccountEvent.OpenNewTransaction -> _state.update {
                it.copy(
                    showEditDialog = true, selectedEmployee = null
                )
            }

            is EmployeeAccountEvent.EditTransactionClicked -> {
                _state.update {
                    it.copy(
                        selectedTransaction = event.transaction,
                        showEditDialog = true,
                        notes = event.transaction.notes ?: "",
                        amount = event.transaction.amount.toString(),
                        selectedEmployee = event.transaction.employee,
                    )
                }
            }

            is EmployeeAccountEvent.DeleteTransactionClicked -> deleteTransaction(event.transaction)
            is EmployeeAccountEvent.SaveTransaction -> addTransaction()
            is EmployeeAccountEvent.DismissEditDialog -> {
                _state.update { it.copy(showEditDialog = false, selectedTransaction = null) }
            }

            is EmployeeAccountEvent.SelectEmployee -> {
                _state.update { it.copy(selectedEmployee = event.employee) }
            }

            is EmployeeAccountEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                filterByQuery(query = event.query)
            }
        }
    }

    private fun filterByQuery(query: String) {
        _state.update {
            it.copy(
                transactions = transactions.filter { transaction ->
                    transaction.employee?.localizedName?.enName?.contains(
                        query,
                        ignoreCase = true
                    ) == true ||
                            transaction.employee?.localizedName?.arName?.contains(
                                query,
                                ignoreCase = true
                            ) == true
                            || transaction.createdByEmployee?.localizedName?.enName?.contains(
                        query,
                        ignoreCase = true
                    ) == true ||
                            transaction.createdByEmployee?.localizedName?.arName?.contains(
                                query,
                                ignoreCase = true
                            ) == true ||
                            transaction.notes?.contains(query, ignoreCase = true) == true ||
                            transaction.type.name.contains(query, ignoreCase = true)
                }
            )
        }
    }


    private fun addTransaction() {
        viewModelScope.launch {
            // Admins can add transactions for any selected employee.
            if (_state.value.currentUser?.isAdmin != true) {
                viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_admin_only)) }
                return@launch
            }

            val currentState = _state.value
            val selectedEmployee = currentState.selectedEmployee
            val currentUser = _state.value.currentUser
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
//todo
//            val newTransaction = _state.value.selectedTransaction?.copy(
//                employee = selectedEmployee,
//                createdByEmployee = currentUser,
//                type = currentState.transactionType,
//                amount = amount,
//                notes = currentState.notes,
//                isSynced = false
//            ) ?: EmployeeAccountTransaction(
//                id = ,
//                employee = selectedEmployee,
//                createdByEmployee = currentUser,
//                type = currentState.transactionType,
//                amount = amount,
//                relatedCommissionId = null,
//                notes = currentState.notes,
//
//            )
//            saveTransaction(newTransaction)
        }
    }

    private fun saveTransaction(transaction: EmployeeAccountTransaction) {
        //todo
//        viewModelScope.launch {
//            _state.update { it.copy(isSaving = true) }
//            val result = if (transaction.localId == 0L) {
//                employeeAccountRepository.addManualPayment(transaction)
//            } else {
//                employeeAccountRepository.updateManualPayment(transaction)
//            }
//
//            result.fold(onSuccess = {
//                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.transaction_saved_successfully))
//                _state.update {
//                    it.copy(
//                        isSaving = false,
//                        showEditDialog = false,
//                        selectedTransaction = null,
//                        amount = "",
//                        notes = "",
//                        loading = false,
//                    )
//                }
//            }, onFailure = {
//                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_saving_transaction))
//                _state.update { it.copy(isSaving = false) }
//            })
//        }
    }

    private fun deleteTransaction(transaction: EmployeeAccountTransaction) {
        viewModelScope.launch {
            val result = employeeAccountRepository.deleteManualPayment(transaction.id.local)
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.transaction_deleted_successfully))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_deleting_transaction))
            }
        }
    }
}
