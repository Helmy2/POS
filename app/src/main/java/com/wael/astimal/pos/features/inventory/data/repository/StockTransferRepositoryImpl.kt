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
            val stockTransferId = stockTransferDao.insertStockTransfer(newTransferEntity)

            stockTransferDao.insertStockTransferItems(items.map { it.copy(stockTransferLocalId = stockTransferId) })

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStockTransfer(
        transferLocalId: Long,
        fromStoreId: Long,
        toStoreId: Long,
        initiatedByUserId: Long,
        items: List<StockTransferItemEntity>
    ): Result<Unit> {
        return try {
            val existingTransfer = stockTransferDao.getStockTransferEntityByLocalId(transferLocalId)
                ?: return Result.failure(NoSuchElementException("Stock transfer with localId $transferLocalId not found."))

            val updatedTransferEntity = existingTransfer.copy(
                fromStoreId = fromStoreId,
                toStoreId = toStoreId,
                initiatedByUserId = initiatedByUserId,
                // Mark as unsynced because it has been modified locally
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )

            // The list of items passed in are the *new* complete list for the transfer.
            // We'll update the header and replace all old items with the new ones in a single transaction.
            stockTransferDao.updateTransferWithItems(updatedTransferEntity, items)

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