package com.wael.astimal.pos.features.management.presentation.employee_account

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.user.domain.entity.User

object EmployeeAccountContract {

    data class DialogState(
        val show: Boolean = false,
        val selectedTransaction: EmployeeAccountTransaction? = null,
        val selectedEmployee: User? = null,
        val amount: String = "",
        val transactionType: EmployeeTransactionType = EmployeeTransactionType.SALARY,
        val notes: String = ""
    )

    data class State(
        val isLoading: Boolean = true,
        val currentUser: User? = null,
        val employeesForDropdown: List<User> = emptyList(),
        val transactions: List<EmployeeAccountTransaction> = emptyList(),
        val searchQuery: String = "",
        val dialogState: DialogState = DialogState()
    ) : Reducer.ViewState {
        // TODO : Implement logic to determine if the user can edit transactions
        val canUserEdit: Boolean get() = true
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data object LoadInitialData : Event
        data class SearchQueryChanged(val query: String) : Event
        data object AddTransactionClicked : Event
        data class EditTransactionClicked(val transaction: EmployeeAccountTransaction) : Event
        data class DeleteTransactionClicked(val transaction: EmployeeAccountTransaction) : Event
        data object SaveChangesClicked : Event
        data object DismissDialog : Event

        // Dialog Input Changes
        data class DialogEmployeeSelected(val employee: User?) : Event
        data class DialogTransactionTypeSelected(val type: EmployeeTransactionType) : Event
        data class DialogAmountChanged(val amount: String) : Event
        data class DialogNotesChanged(val notes: String) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class DropdownDataLoaded(val employees: List<User>) : Event
        data class TransactionsLoaded(val transactions: List<EmployeeAccountTransaction>) : Event
        data object SaveSucceeded : Event

        data object NavigateBack : Event
    }
}
