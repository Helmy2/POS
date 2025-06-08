package com.wael.astimal.pos.features.client_management.data.repository

import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.client_management.data.entity.toDomain
import com.wael.astimal.pos.features.client_management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.client_management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.client_management.domain.repository.SalesReturnRepository
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

            val itemsWithCorrectId = items.map { it.copy(orderReturnLocalId = insertedReturnLocalId) }
            orderReturnDao.insertOrderReturnItems(itemsWithCorrectId)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSalesReturn(returnLocalId: Long): Result<Unit> {
        // todo
        // Implement soft delete logic similar to OrderRepository
        // 1. Fetch the OrderReturnEntity by localId
        // 2. Copy it with isDeletedLocally = true and isSynced = false
        // 3. Call insertOrUpdateOrderReturn() with the updated entity
        println("SalesReturnRepositoryImpl: deleteSalesReturn() called. Logic to be implemented.")
        return Result.success(Unit)
    }

}