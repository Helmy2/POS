package com.wael.astimal.pos.features.management.presentation.employee_account

import com.wael.astimal.pos.core.base.mvi.Reducer
import kotlin.math.abs

class EmployeeAccountReducer :
    Reducer<EmployeeAccountContract.State, EmployeeAccountContract.Event, Nothing> {
    override fun reduce(
        previousState: EmployeeAccountContract.State, event: EmployeeAccountContract.Event
    ): Pair<EmployeeAccountContract.State, Nothing?> {
        return when (event) {
            is EmployeeAccountContract.Event.LoadInitialData -> previousState.copy(isLoading = true) to null

            is EmployeeAccountContract.Event.UserLoaded -> previousState.copy(currentUser = event.user) to null

            is EmployeeAccountContract.Event.DropdownDataLoaded -> previousState.copy(
                employeesForDropdown = event.employees
            ) to null

            is EmployeeAccountContract.Event.TransactionsLoaded -> previousState.copy(
                isLoading = false,
                transactions = event.transactions
            ) to null

            is EmployeeAccountContract.Event.SearchQueryChanged -> previousState.copy(searchQuery = event.query) to null

            is EmployeeAccountContract.Event.AddTransactionClicked -> previousState.copy(
                dialogState = EmployeeAccountContract.DialogState(
                    show = true
                )
            ) to null

            is EmployeeAccountContract.Event.EditTransactionClicked -> previousState.copy(
                dialogState = EmployeeAccountContract.DialogState(
                    show = true,
                    selectedTransaction = event.transaction,
                    selectedEmployee = event.transaction.employee,
                    amount = abs(event.transaction.amount).toString(),
                    transactionType = event.transaction.type,
                    notes = event.transaction.notes ?: ""
                )
            ) to null

            is EmployeeAccountContract.Event.DismissDialog, is EmployeeAccountContract.Event.SaveSucceeded -> previousState.copy(
                dialogState = EmployeeAccountContract.DialogState(show = false)
            ) to null

            // Dialog state updates
            is EmployeeAccountContract.Event.DialogEmployeeSelected -> previousState.copy(
                dialogState = previousState.dialogState.copy(selectedEmployee = event.employee)
            ) to null

            is EmployeeAccountContract.Event.DialogTransactionTypeSelected -> previousState.copy(
                dialogState = previousState.dialogState.copy(transactionType = event.type)
            ) to null

            is EmployeeAccountContract.Event.DialogAmountChanged -> previousState.copy(
                dialogState = previousState.dialogState.copy(
                    amount = event.amount
                )
            ) to null

            is EmployeeAccountContract.Event.DialogNotesChanged -> previousState.copy(
                dialogState = previousState.dialogState.copy(
                    notes = event.notes
                )
            ) to null

            // Events that trigger sagas in ViewModel but don't change state directly
            is EmployeeAccountContract.Event.DeleteTransactionClicked, is EmployeeAccountContract.Event.SaveChangesClicked, EmployeeAccountContract.Event.NavigateBack -> previousState to null
        }
    }
}
