package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccount
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import kotlinx.coroutines.flow.Flow

interface EmployeeAccountRepository {
    fun getEmployeeAccount(employeeId: Long): Flow<EmployeeAccount?>
    suspend fun addManualPayment(transaction: EmployeeAccountTransaction): Result<Unit>
    suspend fun updateManualPayment(transaction: EmployeeAccountTransaction): Result<Unit>
    suspend fun deleteManualPayment(transactionId: Long): Result<Unit>
}