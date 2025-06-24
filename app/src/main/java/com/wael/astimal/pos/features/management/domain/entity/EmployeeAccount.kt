package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.user.domain.entity.User

enum class SourceTransactionType {
    SALE,
    SALE_RETURN;

    fun getStringResId(): Int {
        return when (this) {
            SALE -> R.string.sale_commission
            SALE_RETURN -> R.string.sale_return_commission
        }
    }
}

data class SaleCommission(
    val employeeId: Long,
    val sourceTransactionId: Long,
    val sourceTransactionType: SourceTransactionType,
    val commissionAmount: Double,
    val sourceInvoiceNumber: String,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val isSynced: Boolean,
) : Item

enum class EmployeeTransactionType {
    COMMISSION,
    SALARY,
    DEDUCTION,
    ADVANCE,
    BONUS,
    DIFFERANCE;

    fun getStringResId(): Int {
        return when (this) {
            COMMISSION -> R.string.commission
            SALARY -> R.string.salary
            DEDUCTION -> R.string.deduction
            ADVANCE -> R.string.advance
            BONUS -> R.string.bonus
            DIFFERANCE -> R.string.difference
        }
    }
}

data class EmployeeAccountTransaction(
    val employee: User,
    val createdByEmployee: User,
    val type: EmployeeTransactionType,
    val amount: Double,
    val relatedCommission: SaleCommission?,
    val notes: String?,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
    override val isSynced: Boolean = false,
) : Item

fun EmployeeAccountTransaction.toEntity(): EmployeeAccountTransactionEntity {
    return EmployeeAccountTransactionEntity(
        localId = id.local,
        serverId = id.server,
        employeeId = employee.id,
        createdByEmployeeId = createdByEmployee.id,
        type = type,
        amount = amount,
        relatedCommissionId = relatedCommission?.id?.local,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}


fun EmployeeAccountTransaction.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val queryLower = query.lowercase()
    return employee.name.contains(queryLower) ||
            createdByEmployee.name.contains(queryLower) ||
            notes?.contains(queryLower, ignoreCase = true) == true ||
            type.name.contains(queryLower, ignoreCase = true)
}