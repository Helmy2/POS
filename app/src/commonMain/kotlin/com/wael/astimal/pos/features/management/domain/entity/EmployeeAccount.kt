package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.management.data.local.entity.EmployeeTransactionEntity
import com.wael.astimal.pos.features.management.data.remote.dto.EmployeeTransactionDto
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.advance
import pos.app.generated.resources.bonus
import pos.app.generated.resources.commission_for_order
import pos.app.generated.resources.commission_for_responsibility
import pos.app.generated.resources.deduction
import pos.app.generated.resources.salary

enum class EmployeeTransactionType {
    COMMISSION_FOR_ORDER,
    COMMISSION_FOR_RESPONSIBILITY,
    SALARY,
    DEDUCTION,
    ADVANCE,
    BONUS;

    fun getStringResId(): StringResource {
        return when (this) {
            COMMISSION_FOR_ORDER -> Res.string.commission_for_order
            COMMISSION_FOR_RESPONSIBILITY -> Res.string.commission_for_responsibility
            SALARY -> Res.string.salary
            DEDUCTION -> Res.string.deduction
            ADVANCE -> Res.string.advance
            BONUS -> Res.string.bonus
        }
    }

    companion object {
        fun getSelectedList(): List<EmployeeTransactionType> {
            return listOf(SALARY, DEDUCTION, ADVANCE, BONUS)
        }
    }
}

data class EmployeeTransaction(
    val employee: User,
    val createdByEmployee: User,
    val type: EmployeeTransactionType,
    val amount: Double,
    val notes: String?,
    val invoiceId: String?,
    val id: String,
    val createdAt: Long,
    val updatedAt: Long = Clock.now(),
    val isSynced: Boolean = false,
)

fun EmployeeTransaction.toEntity(): EmployeeTransactionEntity {
    return EmployeeTransactionEntity(
        localId = id,
        employeeId = employee.id,
        createdByEmployeeId = createdByEmployee.id,
        type = type,
        amount = amount,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        invoiceId = invoiceId
    )
}


fun EmployeeTransaction.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val queryLower = query.lowercase()
    return employee.name.contains(queryLower) ||
            createdByEmployee.name.contains(queryLower) ||
            notes?.contains(queryLower, ignoreCase = true) == true ||
            type.name.contains(queryLower, ignoreCase = true)
}

fun EmployeeTransaction.toDto(): EmployeeTransactionDto {
    return EmployeeTransactionDto(
        id = id,
        transactionType = type.name.lowercase(),
        balance = amount,
        notes = notes,
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt.toDateString(),
        creatorId = createdByEmployee.id,
        employeeId = employee.id,
        invoiceId = invoiceId
    )
}