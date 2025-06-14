package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.user.domain.entity.User

enum class SourceTransactionType {
    SALE,
    SALE_RETURN
}

data class SaleCommission(
    val localId: Long,
    val serverId: Int?,
    val employeeId: Long,
    val sourceTransactionId: Long,
    val sourceTransactionType: SourceTransactionType,
    val commissionAmount: Double,
    val isMain: Boolean,
    val date: Long,
    val isSynced: Boolean
)

enum class EmployeeTransactionType {
    COMMISSION,
    SALARY,
    DEDUCTION,
    ADVANCE,
    BONUS,
    WITHDRAWAL;

    fun getStringResId(): Int {
        return when (this) {
            COMMISSION -> R.string.commission
            SALARY -> R.string.salary
            DEDUCTION -> R.string.deduction
            ADVANCE -> R.string.advance
            BONUS -> R.string.bonus
            WITHDRAWAL -> R.string.withdrawal
        }
    }
}

data class EmployeeAccountTransaction(
    val localId: Long,
    val serverId: Int?,
    val employeeId: Long,
    val createdByEmployeeId: Long,
    val type: EmployeeTransactionType,
    val amount: Double,
    val relatedCommissionId: Long?,
    val notes: String?,
    val date: Long,
    val isSynced: Boolean
)

data class EmployeeAccount(
    val employee: User,
    val balance: Double,
    val transactions: List<EmployeeAccountTransaction>
)