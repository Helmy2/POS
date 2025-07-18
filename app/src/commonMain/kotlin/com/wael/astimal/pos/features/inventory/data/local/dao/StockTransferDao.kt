package com.wael.astimal.pos.features.inventory.data.local.dao

import StockTransferStatus
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferWithItemsAndDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockOrUpdateTransfer(transfer: StockTransferEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransferItems(items: List<StockTransferItemEntity>)

    @Query("DELETE FROM stock_transfer_items WHERE stockTransferLocalId = :transferLocalId")
    suspend fun hardDeleteItemsForTransfer(transferLocalId: String)

    @Query("UPDATE stock_transfer_items SET isDeletedLocally = 1 WHERE stockTransferLocalId = :transferLocalId")
    suspend fun softDeleteItemsForTransfer(transferLocalId: String)

    @Query("DELETE FROM stock_transfers WHERE localId = :transferLocalId")
    suspend fun hardDeleteTransfer(transferLocalId: String)

    @Query("UPDATE stock_transfers SET isDeletedLocally = 1 WHERE localId = :transferLocalId")
    suspend fun softDeleteTransfer(transferLocalId: String)

    @Transaction
    suspend fun updateTransferWithItems(
        transfer: StockTransferEntity,
        items: List<StockTransferItemEntity>
    ) {
        insertStockOrUpdateTransfer(transfer)
        softDeleteItemsForTransfer(transfer.localId)
        val itemsWithCorrectId = items.map { it.copy(stockTransferLocalId = transfer.localId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertStockTransferItems(itemsWithCorrectId)
        }
    }

    @Transaction
    suspend fun insertTransferWithItems(
        transfer: StockTransferEntity,
        items: List<StockTransferItemEntity>
    ) {
        insertStockOrUpdateTransfer(transfer)
        val itemsWithCorrectId = items.map { it.copy(stockTransferLocalId = transfer.localId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertStockTransferItems(itemsWithCorrectId)
        }
    }

    @Transaction
    @Query("SELECT * FROM stock_transfers WHERE NOT isDeletedLocally AND localId = :localId")
    suspend fun getStockTransferWithDetails(localId: Long): StockTransferWithItemsAndDetails?

    @Transaction
    @Query("SELECT * FROM stock_transfers WHERE NOT isDeletedLocally")
    fun getAllStockTransfersWithDetailsFlow(): Flow<List<StockTransferWithItemsAndDetails>>

    @Transaction
    @Query("SELECT * FROM stock_transfers WHERE NOT isDeletedLocally AND receivingUserId = :currentUserId AND status = :status")
    fun getPendingTransfersForApproval(
        currentUserId: Long,
        status: StockTransferStatus
    ): Flow<List<StockTransferWithItemsAndDetails>>

    @Transaction
    @Query("SELECT * FROM stock_transfers WHERE NOT isDeletedLocally AND isSynced = 0")
    suspend fun getUnsyncedInvoices(): List<StockTransferWithItemsAndDetails>

    @Transaction
    @Query("SELECT * FROM stock_transfers WHERE isDeletedLocally = 1")
    suspend fun getDeletedInvoice(): List<StockTransferWithItemsAndDetails>

    @Transaction
    @Query("SELECT * FROM stock_transfer_items WHERE isDeletedLocally = 0 AND isSynced = 0")
    suspend fun getUnsyncedInvoicesItems(): List<StockTransferItemEntity>

    @Transaction
    @Query("SELECT * FROM stock_transfer_items WHERE isDeletedLocally = 1")
    suspend fun getDeletedInvoiceItems(): List<StockTransferItemEntity>

    @Query("UPDATE stock_transfer_items SET isDeletedLocally = 1 WHERE localId = :id")
    suspend fun hardDeleteInvoiceItems(id: String)

    @Query("UPDATE stock_transfers SET status = :status WHERE localId = :transferId")
    fun setTransferApprovalStatus(transferId: String, status: StockTransferStatus)

    @Transaction
    @Query("SELECT * FROM stock_transfers WHERE localId = :transferId")
    fun getStockTransfer(transferId: String): StockTransferWithItemsAndDetails
}