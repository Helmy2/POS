package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.repository.EmployeeAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class EmployeeAccountRepositoryImpl(
    private val employeeFinancesDao: EmployeeFinancesDao,
) : EmployeeAccountRepository {
    override fun getAllTransaction(): Flow<List<EmployeeAccountTransaction>> {
        return employeeFinancesDao.getAllTransactions().map { it.map { it -> it.toDomain() } }
    }

    override suspend fun addManualPayment(transaction: EmployeeAccountTransaction): Result<Unit> {
        return try {
            employeeFinancesDao.insertEmployeeTransaction(transaction.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateManualPayment(transaction: EmployeeAccountTransaction): Result<Unit> {
        return try {
            employeeFinancesDao.updateEmployeeTransaction(transaction.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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

private fun EmployeeAccountTransaction.toEntity(): EmployeeAccountTransactionEntity {
    return EmployeeAccountTransactionEntity(
        localId = this.localId,
        serverId = this.serverId,
        employeeId = this.employee?.id ?: throw KotlinNullPointerException(
            "employeeId is null in EmployeeAccountTransactionEntity"
        ),
        createdByEmployeeId = this.createdByEmployee?.id ?: throw KotlinNullPointerException(
            "createdByEmployee is null in EmployeeAccountTransactionEntity"
        ),
        type = this.type,
        amount = this.amount,
        relatedCommissionId = this.relatedCommissionId,
        notes = this.notes,
        creationDate = this.creationDate,
        isSynced = false,
        lastModificationDate = this.lastModificationDate
    )
}
