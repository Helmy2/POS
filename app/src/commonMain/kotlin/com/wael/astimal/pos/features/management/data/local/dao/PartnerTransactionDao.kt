package com.wael.astimal.pos.features.management.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.management.data.local.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.PartnerTransactionWithDetails
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow


@Dao
interface PartnerTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(transaction: PartnerTransactionEntity)

    @Query("SELECT * FROM partner_transactions WHERE partnerLocalId = :partnerLocalId")
    fun getTransactionsForPartner(partnerLocalId: Long): Flow<List<PartnerTransactionEntity>>

    @Query("SELECT SUM(balance) FROM partner_transactions WHERE partnerLocalId = :clientId")
    suspend fun getPartnerBalance(clientId: Long): Double?

    @Query("DELETE FROM partner_transactions WHERE partnerLocalId = :partnerId AND transactionType = :type")
    suspend fun deleteTransactionsByPartner(
        partnerId: Long,
        type: TransactionType,
    )

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE NOT isDeletedLocally")
    fun getAll(): Flow<List<PartnerTransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE partnerLocalId = :id AND NOT isDeletedLocally")
    fun getTransactionsByPartnerId(id: Long): Flow<List<PartnerTransactionWithDetails>>


    @Query("UPDATE partner_transactions SET isDeletedLocally = 1 WHERE localId = :id")
    suspend fun softDeleteTransactionById(id: Long)

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE localId = :id")
    suspend fun getTransactionById(id: Long): PartnerTransactionWithDetails?


    @Query("SELECT * FROM partner_transactions WHERE serverId = :id LIMIT 1")
    suspend fun getTransactionBySeverId(id: String): PartnerTransactionEntity?

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<PartnerTransactionWithDetails>

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE isDeletedLocally = 1")
    suspend fun getAllDeletedTransactions(): List<PartnerTransactionWithDetails>

    @Query("DELETE FROM partner_transactions WHERE serverId = :serverId")
    suspend fun hardDeleteTransactionById(serverId: String)

    @Query("DELETE FROM partner_transactions WHERE invoiceId = :orderId")
    fun deleteTransactionsByInvoiceId(orderId: String)
}