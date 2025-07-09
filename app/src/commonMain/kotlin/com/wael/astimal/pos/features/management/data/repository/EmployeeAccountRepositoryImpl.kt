package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.management.data.local.dao.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.EmployeeAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class EmployeeAccountRepositoryImpl(
    private val employeeFinancesDao: EmployeeFinancesDao,
) : EmployeeAccountRepository {
    override fun getAllTransaction(): Flow<List<EmployeeAccountTransaction>> {
        return employeeFinancesDao.getAllTransactions().map { it.map { it -> it.toDomain() } }
    }

    override suspend fun saveManualPayment(transaction: EmployeeAccountTransaction): Result<Unit> {
        return runCatching {
            employeeFinancesDao.insertOrUpdateEmployeeTransaction(transaction.toEntity())
        }
    }

    override suspend fun deleteManualPayment(transactionId: Long): Result<Unit> {
        return try {
            employeeFinancesDao.deleteEmployeeTransaction(transactionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}