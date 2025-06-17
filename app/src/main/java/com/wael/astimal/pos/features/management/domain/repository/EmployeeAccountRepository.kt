package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import kotlinx.coroutines.flow.Flow

interface EmployeeAccountRepository {
    fun getAllTransaction(): Flow<List<EmployeeAccountTransaction>>
    suspend fun addManualPayment(transaction: EmployeeAccountTransactionEntity): Result<Unit>
    suspend fun updateManualPayment(transaction: EmployeeAccountTransactionEntity): Result<Unit>
    suspend fun deleteManualPayment(transactionId: Long): Result<Unit>
}