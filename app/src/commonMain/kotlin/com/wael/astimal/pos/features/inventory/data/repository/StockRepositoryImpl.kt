package com.wael.astimal.pos.features.inventory.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.data.entity.StoreProductStockEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreProductStockDao
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StoreStock
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class StockRepositoryImpl(
    private val database: AppDatabase,
    private val stockDao: StoreProductStockDao,
    private val stockAdjustmentDao: StockAdjustmentDao,
    private val productDao: ProductDao
) : StockRepository {

    override fun getStoreStocks(
        query: String,
        selectedStoreId: Long?
    ): Flow<List<StoreStock>> {
        return stockDao.getStoreStocks().map { list ->
            list.map { it.toDomain() }.filter {
                val storeCondition =
                    selectedStoreId == null || it.store.id.local == selectedStoreId
                val queryCondition =
                    query.isBlank() || it.product.name.contains(query) || it.store.name.contains(
                        query
                    )
                storeCondition && queryCondition
            }
        }
    }

    override fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double> {
        return stockDao.getStockQuantity(storeId, productId).map { it ?: 0.0 }
    }

    override suspend fun adjustStock(
        storeId: Long,
        productId: Long,
        transactionQuantity: Double
    ) {
        val currentStock =
            stockDao.getStockByStoreAndProduct(storeId, productId).map { it?.quantity ?: 0.0 }
                .first()
        val newQuantity = currentStock + transactionQuantity

        if (newQuantity < 0) {
            val product = productDao.getProductByLocalId(productId)
            throw IllegalStateException("Stock level for ${product?.enName ?: "Product"} cannot be negative. Current stock is $currentStock, attempted change is $transactionQuantity.")
        }

        stockDao.insertOrUpdateStock(
            StoreProductStockEntity(
                storeLocalId = storeId,
                productLocalId = productId,
                quantity = newQuantity
            )
        )
    }

    override suspend fun addStockAdjustment(adjustment: StockAdjustment) {
        database.withTransaction {
            Log.d("TAG", "addStockAdjustment: $adjustment")
            stockAdjustmentDao.insert(adjustment.toEntity())

            val currentStock = stockDao.getStockByStoreAndProduct(
                adjustment.store.id.local, adjustment.product.id.local
            ).map { it?.quantity ?: 0.0 }.first()
            val newQuantity = currentStock + adjustment.quantityChange

            stockDao.insertOrUpdateStock(
                StoreProductStockEntity(
                    storeLocalId = adjustment.store.id.local,
                    productLocalId = adjustment.product.id.local,
                    quantity = newQuantity
                )
            )
        }
    }
}