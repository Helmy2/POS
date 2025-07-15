package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class StockRepositoryImpl(
    private val stockAdjustmentDao: StockAdjustmentDao,
) : StockRepository {

    override fun getStoreStocks(
        query: String, selectedStoreId: Long?
    ): Flow<List<StockAdjustment>> {
        return stockAdjustmentDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double> {
        return stockAdjustmentDao.getStockQuantity(storeId, productId).map { it ?: 0.0 }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addStockAdjustment(adjustment: StockAdjustment): Result<Unit> {
        return try {
            val adjustmentToInsert = if (adjustment.id.server == null) adjustment.toEntity()
                .copy(serverId = Uuid.random().toString())
            else adjustment.toEntity()

            stockAdjustmentDao.insert(adjustmentToInsert)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteStockAdjustment(adjustment: StockAdjustment): Result<Unit> {
        return runCatching {
            stockAdjustmentDao.softDeleteByLocalId(adjustment.id.local)
        }
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

    override suspend fun getAllUnSynced(): Result<List<StockAdjustment>> {
        return try {
            Result.success(stockAdjustmentDao.getAllUnSynced().map { it.toDomain() })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAllDeleted(): Result<List<StockAdjustment>> {
        return try {
            Result.success(stockAdjustmentDao.getAllDeleted().map { it.toDomain() })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun hardDeleteByServerId(id: String): Result<Unit> {
        return try {
            stockAdjustmentDao.deleteByServerId(id)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}