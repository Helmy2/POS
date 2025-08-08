package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.core.util.deleteRecordAndLog
import com.wael.astimal.pos.features.management.data.local.dao.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.data.local.entity.EmployeeTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.EmployeeTransactionRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class EmployeeTransactionRepositoryImpl(
    private val employeeFinancesDao: EmployeeFinancesDao,
    private val supabaseClient: SupabaseClient
) : EmployeeTransactionRepository {
    override fun getAllTransaction(): Flow<List<EmployeeTransaction>> {
        return employeeFinancesDao.getAllTransactions()
            .map {
                it.map { it -> it.toDomain() }
                    .filter { it ->
                        it.type == EmployeeTransactionType.SALARY ||
                                it.type == EmployeeTransactionType.DEDUCTION ||
                                it.type == EmployeeTransactionType.ADVANCE ||
                                it.type == EmployeeTransactionType.BONUS
                    }
            }

    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveManualPayment(transaction: EmployeeTransaction): Result<Unit> {
        return runCatching {
            val toInsert = if (transaction.id == "") {
                transaction.toEntity().copy(localId = Uuid.random().toString())
            } else {
                transaction.toEntity()
            }
            employeeFinancesDao.insertOrUpdate(toInsert)
        }
    }

    override suspend fun deleteManualPayment(transactionId: String): Result<Unit> {
        return try {
            return supabaseClient.deleteRecordAndLog(
                targetTableName = "employee_transactions",
                targetRecordId = transactionId
            )
        } catch (e: Exception) {
            e.printStackTrace()
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

    override suspend fun syncWithServer(entities: List<EmployeeTransactionEntity>): Result<Unit> {
        return runCatching {
            entities.forEach {
                employeeFinancesDao.insertOrUpdate(it)
            }
        }
    }

    override suspend fun deleteAll(ids: List<String>): Result<Unit> {
        return try {
            ids.forEach { employeeFinancesDao.hardDelete(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}