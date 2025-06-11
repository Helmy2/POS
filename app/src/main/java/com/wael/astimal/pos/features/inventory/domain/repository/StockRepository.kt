package com.wael.astimal.pos.features.inventory.domain.repository

import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double>
    suspend fun adjustStock(storeId: Long, productId: Long, transactionUnitId: Long, transactionQuantity: Double)
    suspend fun setStock(storeId: Long, productId: Long, newQuantity: Double)
}