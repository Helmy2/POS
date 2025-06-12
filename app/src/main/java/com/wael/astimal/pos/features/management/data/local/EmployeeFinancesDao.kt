package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.SaleCommissionEntity
import com.wael.astimal.pos.features.management.domain.entity.SourceTransactionType
import kotlinx.coroutines.flow.Flow


@Dao
interface EmployeeFinancesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleCommission(commission: SaleCommissionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployeeTransaction(transaction: EmployeeAccountTransactionEntity): Long

    @Query("SELECT * FROM employee_account_transactions WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getTransactionsForEmployee(employeeId: Long): Flow<List<EmployeeAccountTransactionEntity>>

    @Query("SELECT SUM(amount) FROM employee_account_transactions WHERE employeeId = :employeeId")
    fun getEmployeeBalance(employeeId: Long): Flow<Double?>

    @Query("SELECT * FROM employee_sale_commissions WHERE sourceTransactionId = :orderId AND sourceTransactionType = :type")
    suspend fun getAllCommissionsBySource(orderId: Long, type: SourceTransactionType): List<SaleCommissionEntity>

    @Query("DELETE FROM employee_sale_commissions WHERE sourceTransactionId = :orderId AND sourceTransactionType = :type")
    suspend fun deleteAllCommissionsBySource(orderId: Long, type: SourceTransactionType)
}