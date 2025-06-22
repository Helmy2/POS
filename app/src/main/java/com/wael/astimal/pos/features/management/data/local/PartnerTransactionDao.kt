package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow


@Dao
interface PartnerTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PartnerTransactionEntity)

    @Query("SELECT * FROM partner_transactions WHERE clientId = :clientId")
    fun getTransactionsForClient(clientId: Long): Flow<List<PartnerTransactionEntity>>

    @Query("SELECT * FROM partner_transactions WHERE supplierId = :supplierId")
    fun getTransactionsForSupplier(supplierId: Long): Flow<List<PartnerTransactionEntity>>

    @Query("SELECT * FROM partner_transactions WHERE clientId = :clientId OR supplierId = :supplierId")
    fun getTransactionsForPartner(
        clientId: Long,
        supplierId: Long
    ): Flow<List<PartnerTransactionEntity>>

    @Query("SELECT SUM(debit) - SUM(credit) FROM partner_transactions WHERE clientId = :clientId")
    suspend fun getClientBalance(clientId: Long): Double?

    @Query("SELECT SUM(credit) - SUM(debit) FROM partner_transactions WHERE supplierId = :supplierId")
    suspend fun getSupplierBalance(supplierId: Long): Double?

    @Query("DELETE FROM partner_transactions WHERE sourceTransactionId = :sourceId AND transactionType = :type")
    suspend fun deleteTransactionsBySource(sourceId: Long, type: TransactionType)

    @Query("DELETE FROM partner_transactions WHERE sourceTransactionId = :sourceId AND (transactionType = :type1 OR transactionType = :type2)")
    suspend fun deleteTransactionsBySource(
        sourceId: Long,
        type1: TransactionType,
        type2: TransactionType
    )
}