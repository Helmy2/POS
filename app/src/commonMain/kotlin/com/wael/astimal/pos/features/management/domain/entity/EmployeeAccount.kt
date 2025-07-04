package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.advance
import pos.app.generated.resources.bonus
import pos.app.generated.resources.commission
import pos.app.generated.resources.deduction
import pos.app.generated.resources.difference
import pos.app.generated.resources.salary
import pos.app.generated.resources.sale_commission
import pos.app.generated.resources.sale_return_commission

enum class SourceTransactionType {
    SALE,
    SALE_RETURN;

    fun getStringResId(): StringResource {
        return when (this) {
            SALE -> Res.string.sale_commission
            SALE_RETURN -> Res.string.sale_return_commission
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