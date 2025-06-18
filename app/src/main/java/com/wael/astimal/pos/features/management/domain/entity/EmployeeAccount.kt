package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.features.user.domain.entity.User

enum class SourceTransactionType {
    SALE,
    SALE_RETURN
}

data class SaleCommission(
    val employeeId: Long,
    val sourceTransactionId: Long,
    val sourceTransactionType: SourceTransactionType,
    val commissionAmount: Double,
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
    val relatedCommissionId: Long?,
    val notes: String?,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val isSynced: Boolean,
) : Item