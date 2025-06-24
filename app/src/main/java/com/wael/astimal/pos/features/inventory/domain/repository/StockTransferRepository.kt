package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferItem
import kotlinx.coroutines.flow.Flow

interface StockTransferRepository {
    fun getStockTransfersWithDetails(): Flow<List<StockTransfer>>
    suspend fun getStockTransferWithDetails(localId: Long): Result<StockTransfer?>
    suspend fun addStockTransfer(
        fromStoreId: Long,
        toStoreId: Long,
        transferDate: Long?,
        initiatedByUserId: Long,
        items: List<StockTransferItem>
    ): Result<StockTransfer>

    suspend fun updateStockTransfer(
        transferLocalId: Long,
        fromStoreId: Long,
        toStoreId: Long,
        transferDate: Long?,
        initiatedByUserId: Long,
        items: List<StockTransferItem>
    ): Result<Unit>

    suspend fun deleteStockTransfer(transfer: StockTransfer): Result<Unit>
}