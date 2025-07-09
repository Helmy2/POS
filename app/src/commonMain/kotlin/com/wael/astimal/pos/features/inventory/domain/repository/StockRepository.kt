package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getStoreStocks(query: String, selectedStoreId: Long?): Flow<List<StockAdjustment>>

    fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double>

    suspend fun addStockAdjustment(adjustment: StockAdjustment): Result<Unit>

    suspend fun syncWithServer(adjustments: List<StockAdjustmentEntity>): Result<Unit>

    suspend fun deleteStockAdjustment(adjustment: StockAdjustment): Result<Unit>
}