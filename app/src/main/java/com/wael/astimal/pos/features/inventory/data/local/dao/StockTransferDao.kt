package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferWithItemsAndDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockOrUpdateTransfer(transfer: StockTransferEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransferItems(items: List<StockTransferItemEntity>)

    @Query("DELETE FROM stock_transfer_items WHERE stockTransferLocalId = :transferLocalId")
    suspend fun deleteItemsForTransfer(transferLocalId: Long)

    @Transaction
    suspend fun updateTransferWithItems(transfer: StockTransferEntity, items: List<StockTransferItemEntity>) {
        insertStockOrUpdateTransfer(transfer)
        deleteItemsForTransfer(transfer.localId)
        val itemsWithCorrectId = items.map { it.copy(stockTransferLocalId = transfer.localId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertStockTransferItems(itemsWithCorrectId)
        }
    }

    @Transaction
    suspend fun insertTransferWithItems(transfer: StockTransferEntity, items: List<StockTransferItemEntity>): Long {
        val transferId = insertStockOrUpdateTransfer(transfer)
        val itemsWithCorrectId = items.map { it.copy(stockTransferLocalId = transferId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertStockTransferItems(itemsWithCorrectId)
        }
        return transferId
    }

    @Transaction
    @Query("SELECT * FROM stock_transfers WHERE localId = :localId")
    suspend fun getStockTransferWithDetails(localId: Long): StockTransferWithItemsAndDetails?

    @Transaction
    @Query("SELECT * FROM stock_transfers WHERE NOT isDeletedLocally")
    fun getAllStockTransfersWithDetailsFlow(): Flow<List<StockTransferWithItemsAndDetails>>
}