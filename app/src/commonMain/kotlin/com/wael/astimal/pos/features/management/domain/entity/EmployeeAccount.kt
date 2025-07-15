package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.management.data.local.entity.EmployeeTransactionEntity
import com.wael.astimal.pos.features.management.data.remote.dto.EmployeeTransactionDto
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.advance
import pos.app.generated.resources.bonus
import pos.app.generated.resources.commission
import pos.app.generated.resources.deduction
import pos.app.generated.resources.difference
import pos.app.generated.resources.salary

enum class EmployeeTransactionType {
    COMMISSION,
    SALARY,
    DEDUCTION,
    ADVANCE,
    BONUS,
    DIFFERANCE;

    fun getStringResId(): StringResource {
        return when (this) {
            COMMISSION -> Res.string.commission
            SALARY -> Res.string.salary
            DEDUCTION -> Res.string.deduction
            ADVANCE -> Res.string.advance
            BONUS -> Res.string.bonus
            DIFFERANCE -> Res.string.difference
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
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
    override val isSynced: Boolean = false,
) : Item

fun EmployeeTransaction.toEntity(): EmployeeTransactionEntity {
    return EmployeeTransactionEntity(
        localId = id.local,
        serverId = id.serverStringId,
        employeeId = employee.id.local,
        createdByEmployeeId = createdByEmployee.id.local,
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
        id = id.serverStringId!!,
        transactionType = type.name.lowercase(),
        balance = amount,
        notes = notes,
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt.toDateString(),
        creatorId = createdByEmployee.id.serverStringId!!,
        employeeId = employee.id.serverStringId!!,
        invoiceId = invoiceId
    )
}