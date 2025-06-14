package com.wael.astimal.pos.features.management.presentation.employee_payment

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.user.domain.entity.User

data class EmployeePaymentState(
    val isLoading: Boolean = false,
    val employees: List<User> = emptyList(),
    val selectedEmployee: User? = null,
    val amount: String = "",
    val transactionType: EmployeeTransactionType = EmployeeTransactionType.SALARY,
    val notes: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null
)

sealed interface EmployeePaymentEvent {
    data class SelectEmployee(val employee: User?) : EmployeePaymentEvent
    data class SelectTransactionType(val type: EmployeeTransactionType) : EmployeePaymentEvent
    data class UpdateAmount(val amount: String) : EmployeePaymentEvent
    data class UpdateNotes(val notes: String) : EmployeePaymentEvent
    data object SavePayment : EmployeePaymentEvent
    data object ClearError : EmployeePaymentEvent
    data object ClearSnackbar : EmployeePaymentEvent
}
