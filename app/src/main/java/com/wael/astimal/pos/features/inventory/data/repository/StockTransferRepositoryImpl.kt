package com.wael.astimal.pos.features.inventory.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.StockTransferDao
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StockTransferRepositoryImpl(
    private val database: AppDatabase,
    private val stockTransferDao: StockTransferDao,
    private val stockRepository: StockRepository
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
        transferDate: Long?,
        initiatedByUserId: Long,
        items: List<StockTransferItemEntity>
    ): Result<StockTransfer> {
        return try {
            if (items.isEmpty()) {
                return Result.failure(IllegalArgumentException("Stock transfer must have at least one item."))
            }

            val newTransferEntity = StockTransferEntity(
                serverId = null,
                fromStoreId = fromStoreId,
                toStoreId = toStoreId,
                initiatedByUserId = initiatedByUserId,
                transferDate = transferDate ?: System.currentTimeMillis(),
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = false
            )

            var insertedId: Long = -1
            database.withTransaction {
                insertedId = stockTransferDao.insertTransferWithItems(newTransferEntity, items)

                // Adjust stock for all items
                items.forEach { item ->
                    // Decrease stock from source store
                    stockRepository.adjustStock(
                        storeId = fromStoreId,
                        productId = item.productLocalId,
                        transactionUnitId = item.unitLocalId,
                        transactionQuantity = -item.quantity,
                    )
                    // Increase stock in destination store
                    stockRepository.adjustStock(
                        storeId = toStoreId,
                        productId = item.productLocalId,
                        transactionUnitId = item.unitLocalId,
                        transactionQuantity = item.quantity
                    )
                }
            }

            val createdTransfer = getStockTransferWithDetails(insertedId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve transfer after creation."))

            Result.success(createdTransfer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStockTransfer(
        transferLocalId: Long,
        fromStoreId: Long,
        toStoreId: Long,
        transferDate: Long?,
        initiatedByUserId: Long,
        items: List<StockTransferItemEntity>
    ): Result<Unit> {
        return try {
            database.withTransaction {
                val existingTransferWithDetails = stockTransferDao.getStockTransferWithDetails(transferLocalId)
                    ?: throw NoSuchElementException("Stock transfer with localId $transferLocalId not found.")

                val oldTransfer = existingTransferWithDetails.transfer
                val oldFromStoreId = oldTransfer.fromStoreId ?: throw Exception("Old 'from' store ID is missing.")
                val oldToStoreId = oldTransfer.toStoreId ?: throw Exception("Old 'to' store ID is missing.")

                // Revert stock changes from the old items
                existingTransferWithDetails.itemsWithProducts.forEach { oldItem ->
                    stockRepository.adjustStock(oldItem.item.productLocalId, oldFromStoreId, oldItem.item.unitLocalId, oldItem.item.quantity) // Add back
                    stockRepository.adjustStock(oldItem.item.productLocalId, oldToStoreId, oldItem.item.unitLocalId, -oldItem.item.quantity) // Remove
                }

                // Apply stock changes for the new items
                items.forEach { newItem ->
                    stockRepository.adjustStock(newItem.productLocalId, fromStoreId, newItem.unitLocalId, -newItem.quantity) // Remove
                    stockRepository.adjustStock(newItem.productLocalId, toStoreId, newItem.unitLocalId, newItem.quantity) // Add
                }

                // Update the transfer record itself
                val updatedTransferEntity = oldTransfer.copy(
                    fromStoreId = fromStoreId,
                    toStoreId = toStoreId,
                    initiatedByUserId = initiatedByUserId,
                    isSynced = false,
                    transferDate = transferDate ?: System.currentTimeMillis(),
                    lastModified = System.currentTimeMillis()
                )
                stockTransferDao.updateTransferWithItems(updatedTransferEntity, items)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteStockTransfer(transferLocalId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val transferToDelete = stockTransferDao.getStockTransferWithDetails(transferLocalId)
                    ?: throw NoSuchElementException("Stock transfer not found for deletion.")

                if (!transferToDelete.transfer.isDeletedLocally) {
                    val fromStoreId = transferToDelete.transfer.fromStoreId ?: throw Exception("From store ID is missing.")
                    val toStoreId = transferToDelete.transfer.toStoreId ?: throw Exception("To store ID is missing.")

                    // Revert stock changes
                    transferToDelete.itemsWithProducts.forEach { item ->
                        stockRepository.adjustStock(item.item.productLocalId, fromStoreId, item.item.unitLocalId, item.item.quantity) // Add back
                        stockRepository.adjustStock(item.item.productLocalId, toStoreId, item.item.unitLocalId, -item.item.quantity) // Remove
                    }

                    // Mark as deleted
                    val transferToMarkAsDeleted = transferToDelete.transfer.copy(
                        isDeletedLocally = true,
                        isSynced = false,
                        lastModified = System.currentTimeMillis()
                    )
                    stockTransferDao.updateStockTransfer(transferToMarkAsDeleted)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}