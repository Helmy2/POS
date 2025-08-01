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

    @Query("SELECT * FROM partner_transactions WHERE NOT isDeletedLocally AND partnerLocalId = :partnerLocalId")
    fun getTransactionsForPartner(partnerLocalId: String): Flow<List<PartnerTransactionEntity>>

    @Query("SELECT SUM(balance) FROM partner_transactions WHERE NOT isDeletedLocally AND partnerLocalId = :clientId")
    suspend fun getPartnerBalance(clientId: String): Double?

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
    fun getTransactionsByPartnerId(id: String): Flow<List<PartnerTransactionWithDetails>>


    @Query("UPDATE partner_transactions SET isDeletedLocally = 1 WHERE localId = :id")
    suspend fun softDeleteTransactionById(id: String)

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE NOT isDeletedLocally AND localId = :id")
    suspend fun getTransactionById(id: String): PartnerTransactionWithDetails?


    @Query("SELECT * FROM partner_transactions WHERE NOT isDeletedLocally AND localId = :id LIMIT 1")
    suspend fun getTransactionBySeverId(id: String): PartnerTransactionEntity?

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE NOT isDeletedLocally AND isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<PartnerTransactionWithDetails>

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE isDeletedLocally = 1")
    suspend fun getAllDeletedTransactions(): List<PartnerTransactionWithDetails>

    @Query("DELETE FROM partner_transactions WHERE localId = :id")
    suspend fun hardDeleteTransactionById(id: String)

    @Query("DELETE FROM partner_transactions WHERE invoiceId = :id")
    suspend fun deleteTransactionsByInvoiceId(id: String)

    @Query("UPDATE partner_transactions SET isDeletedLocally = 1 WHERE invoiceId = :id")
    suspend fun softDeleteTransactionsByInvoiceId(id: String)

    @Query("UPDATE partner_transactions SET isDeletedLocally = 1")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM partner_transactions WHERE NOT isDeletedLocally AND partnerLocalId = :partnerId AND updatedAt BETWEEN :startDate AND :endDate")
    fun getTransactionsForPartnerInRange(
        partnerId: String,
        startDate: Long,
        endDate: Long,
    ): Flow<List<PartnerTransactionWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM partner_transactions
        WHERE NOT isDeletedLocally AND employeeLocalId = :employeeId AND createdAt BETWEEN :startDate AND :endDate
        """
    )
    fun getTransactionsCreatedByEmployeeInRange(
        employeeId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<PartnerTransactionWithDetails>>
}