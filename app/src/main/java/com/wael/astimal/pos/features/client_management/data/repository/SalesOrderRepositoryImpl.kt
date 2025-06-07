package com.wael.astimal.pos.features.client_management.data.repository

import com.wael.astimal.pos.features.client_management.data.entity.OrderEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.client_management.data.entity.toDomain
import com.wael.astimal.pos.features.client_management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.client_management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.client_management.domain.repository.SalesOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class SalesOrderRepositoryImpl(
    private val salesOrderDao: SalesOrderDao,
) : SalesOrderRepository {


    override fun getOrders(query: String): Flow<List<SalesOrder>> {
        return salesOrderDao.getAllOrdersWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getOrderDetailsFlow(orderLocalId: Long): Flow<SalesOrder?> {
        return salesOrderDao.getOrderWithDetailsFlow(orderLocalId).map { entityWithDetails ->
            entityWithDetails?.takeUnless { it.order.isDeletedLocally }?.toDomain()
        }
    }

    override suspend fun addOrder(
        order: OrderEntity,
        items: List<OrderProductEntity>
    ): Result<SalesOrder> {
        return try {

            // 1. Insert the parent OrderEntity to get its auto-generated localId
            val insertedOrderLocalId = salesOrderDao.insertOrUpdateOrder(order)

            // 2. Set the correct orderLocalId for each item
            val itemsWithCorrectId = items.map { it.copy(orderLocalId = insertedOrderLocalId) }

            // 3. Insert all the items
            salesOrderDao.insertOrderItems(itemsWithCorrectId)

            // 4. Fetch the newly created order with all its details to return it
            val createdOrderWithDetails = salesOrderDao.getOrderWithDetailsFlow(insertedOrderLocalId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve order after insert."))

            Result.success(createdOrderWithDetails.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteOrder(orderLocalId: Long): Result<Unit> {
        return try {
            val orderEntity = salesOrderDao.getOrderEntityByLocalId(orderLocalId)
                ?: return Result.failure(NoSuchElementException("Order not found with localId: $orderLocalId"))

            val orderToMarkAsDeleted = orderEntity.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            salesOrderDao.insertOrUpdateOrder(orderToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
