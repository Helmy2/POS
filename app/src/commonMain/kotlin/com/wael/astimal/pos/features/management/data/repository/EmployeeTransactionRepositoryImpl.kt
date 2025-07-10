package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.management.data.local.dao.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.data.local.entity.EmployeeTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.EmployeeTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class EmployeeTransactionRepositoryImpl(
    private val employeeFinancesDao: EmployeeFinancesDao,
) : EmployeeTransactionRepository {
    override fun getAllTransaction(): Flow<List<EmployeeTransaction>> {
        return employeeFinancesDao.getAllTransactions().map { it.map { it -> it.toDomain() } }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveManualPayment(transaction: EmployeeTransaction): Result<Unit> {
        return runCatching {
            val toInsert = if (transaction.id.serverStringId == null) {
                transaction.toEntity().copy(serverId = Uuid.random().toString())
            } else {
                transaction.toEntity()
            }
            employeeFinancesDao.insertOrUpdate(toInsert)
        }
    }

    override suspend fun deleteManualPayment(transactionId: Long): Result<Unit> {
        return try {
            employeeFinancesDao.softDeleteEmployeeTransaction(transactionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnsyncedTransactions(): Result<List<EmployeeTransaction>> {
        return runCatching {
            employeeFinancesDao.getUnsyncedTransactions().map { it.toDomain() }
        }
    }

    override suspend fun getAllDeletedTransactions(): Result<List<EmployeeTransaction>> {
        return runCatching {
            employeeFinancesDao.getAllDeletedTransactions().map { it.toDomain() }
        }
    }

    override suspend fun hardDeleteByServerId(serverId: String): Result<Unit> {
        return runCatching {
            employeeFinancesDao.hardDeleteTransactionById(serverId)
        }
    }

    override suspend fun syncWithServer(entities: List<EmployeeTransactionEntity>): Result<Unit> {
        return runCatching {
            entities.map { serverEntity ->
                val existingLocal = employeeFinancesDao.getTransactionBySeverId(
                    serverEntity.serverId ?: throw Exception("serverId is null")
                )

                serverEntity.copy(
                    localId = existingLocal?.localId ?: 0L,
                )
            }.forEach {
                employeeFinancesDao.insertOrUpdate(it)
            }
        }
    }
}