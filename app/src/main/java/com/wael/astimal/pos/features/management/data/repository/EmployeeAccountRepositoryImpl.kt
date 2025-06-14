package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccount
import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccountTransaction
import com.wael.astimal.pos.features.management.domain.repository.EmployeeAccountRepository
import com.wael.astimal.pos.features.user.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.local.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine


class EmployeeAccountRepositoryImpl(
    private val employeeFinancesDao: EmployeeFinancesDao,
    private val userDao: UserDao
) : EmployeeAccountRepository {
    override fun getEmployeeAccount(employeeId: Long): Flow<EmployeeAccount?> {
        val employeeFlow = userDao.getUserFLowById(employeeId)
        val transactionsFlow = employeeFinancesDao.getTransactionsForEmployee(employeeId)
        val balanceFlow = employeeFinancesDao.getEmployeeBalance(employeeId)

        return combine(
            employeeFlow,
            transactionsFlow,
            balanceFlow
        ) { employeeEntity, transactionEntities, balance ->
            employeeEntity?.let {
                EmployeeAccount(
                    employee = it.toDomain(),
                    balance = balance ?: 0.0,
                    transactions = transactionEntities.map { transaction -> transaction.toDomain() }
                )
            }
        }
    }

    override suspend fun addManualPayment(transaction: EmployeeAccountTransaction): Result<Unit> {
        return try {
            val entity = EmployeeAccountTransactionEntity(
                localId = 0,
                serverId = null,
                employeeId = transaction.employeeId,
                createdByEmployeeId = transaction.createdByEmployeeId,
                type = transaction.type,
                amount = transaction.amount,
                relatedCommissionId = null,
                notes = transaction.notes,
                date = transaction.date,
                isSynced = false
            )
            employeeFinancesDao.insertEmployeeTransaction(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}