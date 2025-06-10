package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SalesReturnRepositoryImpl(
    private val orderReturnDao: OrderReturnDao,
) : SalesReturnRepository {

    override fun getSalesReturns(): Flow<List<SalesReturn>> {
        return orderReturnDao.getAllOrderReturnsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSalesReturnDetailsFlow(returnLocalId: Long): Flow<SalesReturn?> {
        // Assuming a similar DAO method exists for single item flow
        // For now, we can filter the main list, but a direct DAO call is better.
        return getSalesReturns().map { returns ->
            returns.find { it.localId == returnLocalId }
        }
    }

    override suspend fun addSalesReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<Unit> {
        return try {
            val insertedReturnLocalId = orderReturnDao.insertOrUpdateOrderReturn(returnEntity)

            val itemsWithCorrectId =
                items.map { it.copy(orderReturnLocalId = insertedReturnLocalId) }
            orderReturnDao.insertOrderReturnItems(itemsWithCorrectId)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateSalesReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<Unit> {
        return try {
            if (returnEntity.localId == 0L) {
                return Result.failure(IllegalArgumentException("Sales Return localId is missing for update operation."))
            }

            val entityToUpdate = returnEntity.copy(
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )

            orderReturnDao.updateSalesReturnWithItems(entityToUpdate, items)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteSalesReturn(returnLocalId: Long): Result<Unit> {
        return try {
            val returnEntity = orderReturnDao.getOrderReturnEntityByLocalId(returnLocalId)
                ?: return Result.failure(NoSuchElementException("Sales Return not found with localId: $returnLocalId"))

            val returnToMarkAsDeleted = returnEntity.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            orderReturnDao.insertOrUpdateOrderReturn(returnToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}