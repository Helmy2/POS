package com.wael.astimal.pos.features.inventory.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreProductStockEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreProductStockDao
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StoreStock
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class StockRepositoryImpl(
    private val database: AppDatabase,
    private val stockDao: StoreProductStockDao,
    private val stockAdjustmentDao: StockAdjustmentDao,
) : StockRepository {

    override fun getStoreStocks(query: String, selectedStoreId: Long?): Flow<List<StoreStock>> {
        return stockDao.getStoreStocks().map { list ->
            list.map { it.toDomain() }.filter {
                    val storeCondition =
                        selectedStoreId == null || it.store.localId == selectedStoreId
                    val queryCondition =
                        query.isBlank() || it.product.localizedName.displayName(Language.English)
                            .contains(
                                query, ignoreCase = true
                            ) || it.product.localizedName.displayName(Language.Arabic)
                            .contains(query, ignoreCase = true)
                    storeCondition && queryCondition
                }
        }
    }

    override fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double> {
        return stockDao.getStockQuantity(storeId, productId).map { it ?: 0.0 }
    }

    override suspend fun adjustStock(
        storeId: Long, productId: Long, transactionQuantity: Double
    ) {
        val currentStock =
            stockDao.getStockByStoreAndProduct(storeId, productId).map { it?.quantity ?: 0.0 }
                .first()
        val newQuantity = currentStock + transactionQuantity

        stockDao.insertOrUpdateStock(
            StoreProductStockEntity(
                storeLocalId = storeId, productLocalId = productId, quantity = newQuantity
            )
        )
    }

    override suspend fun addStockAdjustment(adjustment: StockAdjustment) {
        database.withTransaction {
            val adjustmentEntity = StockAdjustmentEntity(
                serverId = null,
                storeId = adjustment.store.localId,
                productId = adjustment.product.localId,
                userId = adjustment.user.id,
                reason = adjustment.reason,
                notes = adjustment.notes,
                quantityChange = adjustment.quantityChange,
                date = System.currentTimeMillis()
            )
            stockAdjustmentDao.insert(adjustmentEntity)

            val currentStock = stockDao.getStockByStoreAndProduct(
                adjustment.store.localId, adjustment.product.localId
            ).map { it?.quantity ?: 0.0 }.first()
            val newQuantity = currentStock + adjustment.quantityChange

            stockDao.insertOrUpdateStock(
                StoreProductStockEntity(
                    storeLocalId = adjustment.store.localId,
                    productLocalId = adjustment.product.localId,
                    quantity = newQuantity
                )
            )
        }
    }

    override fun getAdjustmentHistory(storeId: Long, productId: Long): Flow<List<StockAdjustment>> {
        return stockAdjustmentDao.getAdjustmentHistory(storeId, productId).map { list ->
            list.map { it.toDomain() }
        }
    }
}