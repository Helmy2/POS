package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getStoreStocks(query: String, selectedStoreId: String?): Flow<List<StockAdjustment>>

    fun getStockQuantity(storeId: String, productId: String): Flow<Double>

    suspend fun addStockAdjustment(adjustment: StockAdjustment): Result<Unit>

    suspend fun syncWithServer(adjustments: List<StockAdjustmentEntity>): Result<Unit>

    suspend fun deleteStockAdjustment(adjustment: StockAdjustment): Result<Unit>
    suspend fun getAllUnSynced(): Result<List<StockAdjustment>>
    suspend fun getStockQuantity(productId: String): Double
    suspend fun getStockInCurrentStore(productId: String): Double
    suspend fun deleteAll(ids: List<String>): Result<Unit>
}