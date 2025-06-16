package com.wael.astimal.pos.features.management.presentation.employee_account

import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccount
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.user.domain.entity.User

data class EmployeeAccountState(
    val loading: Boolean = false,
    val employees: List<User> = emptyList(),
    val selectedEmployee: User? = null,
    val employeeAccount: EmployeeAccount? = null,
    val amount: String = "",
    val transactionType: EmployeeTransactionType = EmployeeTransactionType.SALARY,
    val notes: String = "",

    val isSaving: Boolean = false,
    val isAdmin: Boolean = false,
    val currentUserId: Long? = null,
    val transactionToEdit: EmployeeAccountTransaction? = null,
    val showEditDialog: Boolean = false
)

sealed interface EmployeeAccountEvent {
    data class SelectEmployee(val employee: User) : EmployeeAccountEvent
    data class SelectTransactionType(val type: EmployeeTransactionType) : EmployeeAccountEvent
    data class UpdateAmount(val amount: String) : EmployeeAccountEvent
    data class UpdateNotes(val notes: String) : EmployeeAccountEvent
    data object AddTransaction : EmployeeAccountEvent
    data class EditTransactionClicked(val transaction: EmployeeAccountTransaction) :
        EmployeeAccountEvent

    data class DeleteTransactionClicked(val transaction: EmployeeAccountTransaction) :
        EmployeeAccountEvent

    data class SaveTransaction(val transaction: EmployeeAccountTransaction) : EmployeeAccountEvent
    data object DismissEditDialog : EmployeeAccountEvent
}
