package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferWithItemsAndDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransfer(transfer: StockTransferEntity): Long

    @Update
    suspend fun updateStockTransfer(transfer: StockTransferEntity)

    @Query("SELECT * FROM stock_transfers WHERE localId = :localId")
    suspend fun getStockTransferEntityByLocalId(localId: Long): StockTransferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransferItems(items: List<StockTransferItemEntity>)

    @Query("DELETE FROM stock_transfer_items WHERE stockTransferLocalId = :transferLocalId")
    suspend fun deleteItemsForTransfer(transferLocalId: Long)

    @androidx.room.Transaction
    suspend fun updateTransferWithItems(transfer: StockTransferEntity, items: List<StockTransferItemEntity>) {
        updateStockTransfer(transfer)
        deleteItemsForTransfer(transfer.localId)
        val itemsWithCorrectId = items.map { it.copy(stockTransferLocalId = transfer.localId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertStockTransferItems(itemsWithCorrectId)
        }
    }

    @androidx.room.Transaction
    suspend fun insertTransferWithItems(transfer: StockTransferEntity, items: List<StockTransferItemEntity>): Long {
        val transferId = insertStockTransfer(transfer)
        val itemsWithCorrectId = items.map { it.copy(stockTransferLocalId = transferId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertStockTransferItems(itemsWithCorrectId)
        }
        return transferId
    }

    @androidx.room.Transaction
    @Query("SELECT * FROM stock_transfers WHERE localId = :localId")
    fun getStockTransferWithDetailsFlow(localId: Long): Flow<StockTransferWithItemsAndDetails?>

    @androidx.room.Transaction
    @Query("SELECT * FROM stock_transfers WHERE localId = :localId")
    suspend fun getStockTransferWithDetails(localId: Long): StockTransferWithItemsAndDetails?

    @androidx.room.Transaction
    @Query("SELECT * FROM stock_transfers WHERE NOT isDeletedLocally ORDER BY transferDate DESC")
    fun getAllStockTransfersWithDetailsFlow(): Flow<List<StockTransferWithItemsAndDetails>>

    @Query("DELETE FROM stock_transfers WHERE localId IN (:localIds)")
    suspend fun deleteStockTransfersByLocalIds(localIds: List<Long>)
}