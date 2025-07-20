package com.wael.astimal.pos.features.management.presentation.employee_account

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.management.domain.entity.matchesQuery
import com.wael.astimal.pos.features.management.domain.repository.EmployeeTransactionRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_cannot_edit
import pos.app.generated.resources.error_deleting_transaction
import pos.app.generated.resources.error_saving_transaction
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.transaction_deleted_successfully
import pos.app.generated.resources.transaction_saved_successfully

class EmployeeAccountViewModel(
    private val employeeTransactionRepository: EmployeeTransactionRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController
) : BaseViewModel<EmployeeAccountContract.State, EmployeeAccountContract.Event, Nothing>(
    reducer = EmployeeAccountReducer(), initialState = EmployeeAccountContract.State()
) {

    init {
        processEvent(EmployeeAccountContract.Event.LoadInitialData)
    }

    val filteredTransactionsState: StateFlow<List<EmployeeTransaction>> = combine(
        state, employeeTransactionRepository.getAllTransaction()
    ) { state, allTransactions ->
        setState(EmployeeAccountContract.Event.TransactionsLoaded(allTransactions))
        if (state.searchQuery.isBlank()) {
            allTransactions
        } else {
            allTransactions.filter { it.matchesQuery(state.searchQuery) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    override fun handleEvent(event: EmployeeAccountContract.Event) {
        when (event) {
            is EmployeeAccountContract.Event.LoadInitialData -> loadInitialData()
            is EmployeeAccountContract.Event.SaveChangesClicked -> saveTransaction()
            is EmployeeAccountContract.Event.DeleteTransactionClicked -> deleteTransaction(event.transaction)
            is EmployeeAccountContract.Event.NavigateBack -> {
                viewModelScope.launch { navigationController.navigateBack() }
            }

            else -> setState(event)
        }
    }

    private fun loadInitialData() {
        setState(EmployeeAccountContract.Event.LoadInitialData)
        viewModelScope.launch {
            setState(EmployeeAccountContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
        viewModelScope.launch {
            userRepository.getEmployeesFlow().collect { employees ->
                setState(EmployeeAccountContract.Event.DropdownDataLoaded(employees))
            }
        }
    }

    private fun saveTransaction() {
        viewModelScope.launch {
            val dialogState = state.value.dialogState
            val currentUser = state.value.currentUser

            if (currentUser == null || !state.value.canUserEdit) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_cannot_edit)))
                return@launch
            }

            val employee = dialogState.selectedEmployee
            val amount = dialogState.amount.toDoubleOrNull()

            if (employee == null || amount == null || amount == 0.0) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }

            val transactionToSave = EmployeeTransaction(
                id = dialogState.selectedTransaction?.id ?: "",
                employee = employee,
                createdByEmployee = currentUser,
                amount = amount,
                type = dialogState.transactionType,
                notes = dialogState.notes,
                createdAt = dialogState.selectedTransaction?.createdAt ?: Clock.now(),
                invoiceId = null
            )

            val result = employeeTransactionRepository.saveManualPayment(transactionToSave)

            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.transaction_saved_successfully)))
                setState(EmployeeAccountContract.Event.SaveSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_saving_transaction)))
            }
        }
    }

    private fun deleteTransaction(transaction: EmployeeTransaction) {
        viewModelScope.launch {
            employeeTransactionRepository.deleteManualPayment(transaction.id).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.transaction_deleted_successfully)))
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_deleting_transaction)))
            }
        }
    }
}
