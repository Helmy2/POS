package com.wael.astimal.pos.features.management.presentation.employee_account

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccount
import com.wael.astimal.pos.features.user.domain.entity.User

data class EmployeeAccountState(
    val isLoading: Boolean = false,
    val employees: List<User> = emptyList(),
    val selectedEmployee: User? = null,
    val employeeAccount: EmployeeAccount? = null,
    @StringRes val error: Int? = null
)

sealed interface EmployeeAccountEvent {
    data class SelectEmployee(val employee: User) : EmployeeAccountEvent
    data object ClearError : EmployeeAccountEvent
}