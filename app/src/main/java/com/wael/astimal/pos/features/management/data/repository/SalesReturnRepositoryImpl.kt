package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.data.logic.ReturnAmountLogic
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class SalesReturnRepositoryImpl(
    private val database: AppDatabase,
    private val orderReturnDao: OrderReturnDao,
    private val returnAmountLogic: ReturnAmountLogic,
    private val sessionManager: SessionManager
) : SalesReturnRepository {

    override fun getReturns(query: String): Flow<List<SalesReturn>> {
        return orderReturnDao.getAllReturnsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getReturnDetailsFlow(returnLocalId: Long): Flow<SalesReturn?> {
        return orderReturnDao.getReturnWithDetailsFlow(returnLocalId).map { entityWithDetails ->
            entityWithDetails?.takeUnless { it.orderReturn.isDeletedLocally }?.toDomain()
        }
    }

    override suspend fun addReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderProductEntity>
    ): Result<SalesReturn> {
        return try {
            var insertedReturnLocalId: Long = -1
            database.withTransaction {
                insertedReturnLocalId = orderReturnDao.insertOrUpdateReturn(returnEntity)
                val itemsWithCorrectId = items.map { it.copy(orderLocalId = insertedReturnLocalId) }
                orderReturnDao.insertReturnItems(itemsWithCorrectId)
                returnAmountLogic.processNewReturn(returnEntity, items, insertedReturnLocalId)
            }
            val createdReturnWithDetails = orderReturnDao.getReturnWithDetailsFlow(insertedReturnLocalId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve return after insert."))
            Result.success(createdReturnWithDetails.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderProductEntity>
    ): Result<SalesReturn> {
        return try {
            val returnId = returnEntity.localId
            if (returnId == 0L) {
                return Result.failure(IllegalArgumentException("Return localId is missing for update operation."))
            }
            database.withTransaction {
                val currentUserId = sessionManager.getCurrentUser().first()?.id
                    ?: throw Exception("User not authenticated for update operation")

                val oldReturnEntity = orderReturnDao.getReturnEntityByLocalId(returnId)
                    ?: throw NoSuchElementException("Original return not found for update.")
                val oldItems = orderReturnDao.getItemsForReturn(returnId)

                returnAmountLogic.revertReturn(oldReturnEntity, oldItems, currentUserId)

                val entityToUpdate = returnEntity.copy(isSynced = false, lastModified = System.currentTimeMillis())
                orderReturnDao.updateReturnWithItems(entityToUpdate, items)

                returnAmountLogic.processNewReturn(entityToUpdate, items, returnId)
            }
            val updatedReturnWithDetails = orderReturnDao.getReturnWithDetailsFlow(returnId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve return after update."))
            Result.success(updatedReturnWithDetails.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReturn(returnLocalId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val currentUserId = sessionManager.getCurrentUser().first()?.id
                    ?: throw Exception("User not authenticated for delete operation")

                val returnEntity = orderReturnDao.getReturnEntityByLocalId(returnLocalId)
                    ?: throw NoSuchElementException("Return not found with localId: $returnLocalId")

                if (!returnEntity.isDeletedLocally) {
                    val items = orderReturnDao.getItemsForReturn(returnLocalId)
                    returnAmountLogic.revertReturn(returnEntity, items, currentUserId)

                    val returnToMarkAsDeleted = returnEntity.copy(
                        isDeletedLocally = true,
                        isSynced = false,
                        lastModified = System.currentTimeMillis()
                    )
                    orderReturnDao.updateReturn(returnToMarkAsDeleted)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}