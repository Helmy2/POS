package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.local.dao.StockTransferDao
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferItem
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StockTransferRepositoryImpl(
    private val stockTransferDao: StockTransferDao,
    private val stockRepository: StockRepository
) : StockTransferRepository {

    override fun getStockTransfersWithDetails(): Flow<List<StockTransfer>> {
        return stockTransferDao.getAllStockTransfersWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getStockTransferWithDetails(localId: Long): Result<StockTransfer> {
        return runCatching {
            val entityWithDetails = stockTransferDao.getStockTransferWithDetails(localId)
            entityWithDetails?.takeUnless { it.transfer.isDeletedLocally }?.toDomain()
                ?: throw NoSuchElementException("Stock transfer with localId $localId not found or marked as deleted.")
        }
    }

    override suspend fun addStockTransfer(
        fromStoreId: Long,
        toStoreId: Long,
        transferDate: Long?,
        initiatedByUserId: Long,
        items: List<StockTransferItem>
    ): Result<StockTransfer> {
        return runCatching {
            TODO()
//            if (items.isEmpty()) {
//                throw IllegalArgumentException("Stock transfer must have at least one item.")
//            }
//
//            val newTransferEntity = StockTransferEntity(
//                serverId = null,
//                fromStoreId = fromStoreId,
//                toStoreId = toStoreId,
//                initiatedByUserId = initiatedByUserId,
//                createdAt = transferDate ?: Clock.now(),
//                isSynced = false,
//                updatedAt = Clock.now(),
//                isDeletedLocally = false
//            )
//            val insertedId: Long = stockTransferDao.insertTransferWithItems(
//                newTransferEntity,
//                items.map { it.toEntity(0L) })
//
//            // Adjust stock for all items
//            items.forEach { item ->
//                // Decrease stock from source store
//                stockRepository.adjustStock(
//                    storeId = fromStoreId,
//                    productId = item.product.id.local,
//                    transactionQuantity = -item.quantity,
//                )
//                // Increase stock in destination store
//                stockRepository.adjustStock(
//                    storeId = toStoreId,
//                    productId = item.product.id.local,
//                    transactionQuantity = item.quantity
//                )
//            }
//
//
//            val createdTransfer = getStockTransferWithDetails(insertedId)
//
//            createdTransfer.getOrThrow()
        }
    }

    override suspend fun updateStockTransfer(
        transferLocalId: Long,
        fromStoreId: Long,
        toStoreId: Long,
        transferDate: Long?,
        initiatedByUserId: Long,
        items: List<StockTransferItem>
    ): Result<Unit> {
        return try {
            TODO()
//            val existingTransferWithDetails =
//                stockTransferDao.getStockTransferWithDetails(transferLocalId)
//                    ?: throw NoSuchElementException("Stock transfer with localId $transferLocalId not found.")
//
//            val oldTransfer = existingTransferWithDetails.transfer
//            val oldFromStoreId =
//                oldTransfer.fromStoreId ?: throw Exception("Old 'from' store ID is missing.")
//            val oldToStoreId =
//                oldTransfer.toStoreId ?: throw Exception("Old 'to' store ID is missing.")
//
//            // Revert stock changes from the old items
//            existingTransferWithDetails.itemsWithProducts.forEach { oldItem ->
//                stockRepository.adjustStock(
//                    oldItem.item.productLocalId,
//                    oldFromStoreId,
//                    oldItem.item.quantity
//                ) // Add back
//                stockRepository.adjustStock(
//                    oldItem.item.productLocalId,
//                    oldToStoreId,
//                    -oldItem.item.quantity
//                ) // Remove
//            }
//
//            // Apply stock changes for the new items
//            items.forEach { newItem ->
//                stockRepository.adjustStock(
//                    newItem.product.id.local,
//                    fromStoreId,
//                    -newItem.quantity
//                ) // Remove
//                stockRepository.adjustStock(
//                    newItem.product.id.local,
//                    toStoreId,
//                    newItem.quantity
//                ) // Add
//            }
//
//            // Update the transfer record itself
//            val updatedTransferEntity = oldTransfer.copy(
//                fromStoreId = fromStoreId,
//                toStoreId = toStoreId,
//                initiatedByUserId = initiatedByUserId,
//                isSynced = false,
//                createdAt = transferDate ?: Clock.now(),
//                updatedAt = Clock.now()
//            )
//            stockTransferDao.updateTransferWithItems(
//                updatedTransferEntity,
//                items.map { it.toEntity(transferLocalId) })
//            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteStockTransfer(transfer: StockTransfer): Result<Unit> {
        return try {
            TODO()

//            val transferToDelete =
//                stockTransferDao.getStockTransferWithDetails(transfer.id.local)
//                    ?: throw NoSuchElementException("Stock transfer not found for deletion.")
//
//            if (!transferToDelete.transfer.isDeletedLocally) {
//                val fromStoreId = transferToDelete.transfer.fromStoreId
//                    ?: throw Exception("From store ID is missing.")
//                val toStoreId = transferToDelete.transfer.toStoreId
//                    ?: throw Exception("To store ID is missing.")

                // Revert stock changes

//                transferToDelete.itemsWithProducts.forEach { item ->
//                    stockRepository.adjustStock(
//                        item.item.productLocalId,
//                        fromStoreId,
//                        item.item.quantity
//                    ) // Add back
//                    stockRepository.adjustStock(
//                        item.item.productLocalId,
//                        toStoreId,
//                        -item.item.quantity
//                    ) // Remove
//                }

                // Mark as deleted
//                val transferToMarkAsDeleted = transferToDelete.transfer.copy(
//                    isDeletedLocally = true,
//                    isSynced = false,
//                    updatedAt = Clock.now()
//                )
//                stockTransferDao.insertStockOrUpdateTransfer(transferToMarkAsDeleted)
//            }
//            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}