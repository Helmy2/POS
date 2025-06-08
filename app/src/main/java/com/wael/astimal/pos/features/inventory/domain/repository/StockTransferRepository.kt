package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import kotlinx.coroutines.flow.Flow

interface StockTransferRepository {
    fun getStockTransfersWithDetails(): Flow<List<StockTransfer>>
    fun getStockTransferWithDetailsFlow(localId: Long): Flow<StockTransfer?>
    suspend fun getStockTransferWithDetails(localId: Long): StockTransfer?
    suspend fun addStockTransfer(
        fromStoreId: Long,
        toStoreId: Long,
        initiatedByUserId: Long,
        items: List<StockTransferItemEntity>
    ): Result<Unit>
    suspend fun updateStockTransfer(
        transferLocalId: Long,
        fromStoreId: Long,
        toStoreId: Long,
        initiatedByUserId: Long,
        items: List<StockTransferItemEntity>
    ): Result<Unit>
    suspend fun deleteStockTransfer(transferLocalId: Long): Result<Unit>
}