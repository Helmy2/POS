package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.local.entity.EmployeeTransactionEntity
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import kotlinx.coroutines.flow.Flow

interface EmployeeTransactionRepository {
    fun getAllTransaction(): Flow<List<EmployeeTransaction>>
    suspend fun saveManualPayment(transaction: EmployeeTransaction): Result<Unit>
    suspend fun deleteManualPayment(transactionId: String): Result<Unit>
    suspend fun getUnsyncedTransactions(): Result<List<EmployeeTransaction>>
    suspend fun getAllDeletedTransactions(): Result<List<EmployeeTransaction>>
    suspend fun syncWithServer(entities: List<EmployeeTransactionEntity>): Result<Unit>
    suspend fun deleteAll(ids: List<String>): Result<Unit>
}