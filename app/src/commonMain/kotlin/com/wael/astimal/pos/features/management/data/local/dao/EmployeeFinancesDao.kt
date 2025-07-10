package com.wael.astimal.pos.features.management.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.management.data.local.entity.EmployeeTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.EmployeeTransactionWithDetailsEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface EmployeeFinancesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(transaction: EmployeeTransactionEntity): Long

    @Transaction
    @Query("SELECT * FROM employee_account_transactions WHERE NOT isDeletedLocally")
    fun getAllTransactions(): Flow<List<EmployeeTransactionWithDetailsEntity>>

    @Query("UPDATE employee_account_transactions SET isDeletedLocally = 1 WHERE localId = :localId")
    suspend fun softDeleteEmployeeTransaction(localId: Long)

    @Transaction
    @Query("SELECT * FROM employee_account_transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<EmployeeTransactionWithDetailsEntity>

    @Transaction
    @Query("SELECT * FROM employee_account_transactions WHERE isDeletedLocally = 1")
    suspend fun getAllDeletedTransactions(): List<EmployeeTransactionWithDetailsEntity>

    @Query("DELETE FROM employee_account_transactions WHERE serverId = :serverId")
    suspend fun hardDeleteTransactionById(serverId: String)

    @Query("SELECT * FROM employee_account_transactions WHERE serverId = :id LIMIT 1")
    suspend fun getTransactionBySeverId(id: String): EmployeeTransactionEntity?
}