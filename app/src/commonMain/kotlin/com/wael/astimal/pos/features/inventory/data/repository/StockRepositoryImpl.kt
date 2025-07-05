package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class StockRepositoryImpl(
    private val stockAdjustmentDao: StockAdjustmentDao,
) : StockRepository {

    override fun getStoreStocks(
        query: String,
        selectedStoreId: Long?
    ): Flow<List<StockAdjustment>> {
        return stockAdjustmentDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double> {
        return stockAdjustmentDao.getStockQuantity(storeId, productId).map { it ?: 0.0 }
    }

    override suspend fun addStockAdjustment(adjustment: StockAdjustment) {
        stockAdjustmentDao.insert(adjustment.toEntity())
    }

    override suspend fun syncWithServer(adjustments: List<StockAdjustmentEntity>): Result<Unit> {
        return runCatching {
            adjustments.map {
                val existingEntity = stockAdjustmentDao.getAdjustmentByServerId(
                    it.serverId ?: throw Exception("Server ID not found")
                )
                it.copy(localId = existingEntity?.localId ?: 0L)
            }.also {
                stockAdjustmentDao.upsertAll(it)
            }
        }
    }
}