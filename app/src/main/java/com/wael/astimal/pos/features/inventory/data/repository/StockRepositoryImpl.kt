package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.StoreProductStockEntity
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreProductStockDao
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class StockRepositoryImpl(
    private val stockDao: StoreProductStockDao,
    private val unitDao: UnitDao
) : StockRepository {

    override fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double> {
        return stockDao.getQuantityFlow(storeId, productId).map { it ?: 0.0 }
    }

    override suspend fun adjustStock(
        storeId: Long,
        productId: Long,
        transactionUnitId: Long, // The unit used in the transaction (e.g., a "Box")
        transactionQuantity: Double // The quantity of that unit (e.g., 1 box)
    ) {
        // 1. Get the conversion rate for the transaction's unit
        val unit = unitDao.getByLocalId(transactionUnitId)
            ?: throw Exception("Unit with id $transactionUnitId not found for stock adjustment.")

        // 2. Calculate the change in the BASE unit
        val changeInBaseQuantity = transactionQuantity * unit.rate

        // 3. Get current stock to see if a record exists
        val currentStock = stockDao.getQuantity(storeId, productId)

        if (currentStock == null) {
            // No record exists, create one with the new quantity
            stockDao.insertOrUpdateStock(
                StoreProductStockEntity(
                    storeLocalId = storeId,
                    productLocalId = productId,
                    quantity = changeInBaseQuantity
                )
            )
        } else {
            // Record exists, just adjust the quantity
            stockDao.adjustQuantity(storeId, productId, changeInBaseQuantity)
        }
    }

    override suspend fun setStock(storeId: Long, productId: Long, newQuantity: Double) {
        stockDao.insertOrUpdateStock(
            StoreProductStockEntity(
                storeLocalId = storeId,
                productLocalId = productId,
                quantity = newQuantity
            )
        )
    }
}