package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.StockTransferDao
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StockTransferRepositoryImpl(
    private val stockTransferDao: StockTransferDao,
) : StockTransferRepository {
    
    override fun getStockTransfersWithDetails(): Flow<List<StockTransfer>> {
        return stockTransferDao.getAllStockTransfersWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getStockTransferWithDetailsFlow(localId: Long): Flow<StockTransfer?> {
        return stockTransferDao.getStockTransferWithDetailsFlow(localId).map { entityWithDetails ->
            entityWithDetails?.takeUnless { it.transfer.isDeletedLocally }?.toDomain()
        }
    }

    override suspend fun getStockTransferWithDetails(localId: Long): StockTransfer? {
        val entityWithDetails = stockTransferDao.getStockTransferWithDetails(localId)
        return entityWithDetails?.takeUnless { it.transfer.isDeletedLocally }?.toDomain()
    }


    override suspend fun addStockTransfer(
        fromStoreId: Long,
        toStoreId: Long,
        initiatedByUserId: Long,
        items: List<StockTransferItemEntity>
    ): Result<Unit> {
        return try {
            if (items.isEmpty()) {
                return Result.failure(IllegalArgumentException("Stock transfer must have at least one item."))
            }

            val newTransferEntity = StockTransferEntity(
                serverId = null,
                fromStoreId = fromStoreId,
                toStoreId = toStoreId,
                initiatedByUserId = initiatedByUserId,
                transferDate = System.currentTimeMillis(),
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = false
            )
            stockTransferDao.insertStockTransfer(newTransferEntity)

            stockTransferDao.insertStockTransferItems(items)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteStockTransfer(transferLocalId: Long): Result<Unit> {
        return try {
            val transferEntity = stockTransferDao.getStockTransferEntityByLocalId(transferLocalId)
                ?: return Result.failure(NoSuchElementException("Stock transfer not found for deletion with localId: $transferLocalId"))

            val transferToMarkAsDeleted = transferEntity.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            stockTransferDao.updateStockTransfer(transferToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}