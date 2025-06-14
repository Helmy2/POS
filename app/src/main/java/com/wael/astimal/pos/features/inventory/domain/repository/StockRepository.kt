package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StoreStock
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getStoreStocks(query: String, selectedStoreId: Long?): Flow<List<StoreStock>>

    fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double>

    suspend fun adjustStock(
        storeId: Long,
        productId: Long,
        transactionQuantity: Double,
    )

    suspend fun addStockAdjustment(adjustment: StockAdjustment)

    fun getAdjustmentHistory(storeId: Long, productId: Long): Flow<List<StockAdjustment>>
}