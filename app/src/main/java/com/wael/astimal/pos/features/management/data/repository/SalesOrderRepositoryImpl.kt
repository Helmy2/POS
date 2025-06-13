package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.management.data.logic.OrderAmountLogic
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class SalesOrderRepositoryImpl(
    private val database: AppDatabase,
    private val salesOrderDao: SalesOrderDao,
    private val orderAmountLogic: OrderAmountLogic,
    private val sessionManager: SessionManager
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
            var insertedOrderLocalId: Long = -1
            database.withTransaction {
                insertedOrderLocalId = salesOrderDao.insertOrUpdateOrder(order)
                val itemsWithCorrectId = items.map { it.copy(orderLocalId = insertedOrderLocalId) }
                salesOrderDao.insertOrderItems(itemsWithCorrectId)

                orderAmountLogic.processNewOrder(order, items, insertedOrderLocalId)
            }
            val createdOrderWithDetails = salesOrderDao.getOrderWithDetailsFlow(insertedOrderLocalId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve order after insert."))
            Result.success(createdOrderWithDetails.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrder(
        order: OrderEntity,
        items: List<OrderProductEntity>
    ): Result<SalesOrder> {
        return try {
            val orderId = order.localId
            if (orderId == 0L) {
                return Result.failure(IllegalArgumentException("Order localId is missing for update operation."))
            }

            database.withTransaction {
                val currentUserId = sessionManager.getCurrentUser().first()?.id
                    ?: throw Exception("User not authenticated for update operation")

                val oldOrderEntity = salesOrderDao.getOrderEntityByLocalId(orderId)
                    ?: throw NoSuchElementException("Original order not found for update.")
                val oldItems = salesOrderDao.getItemsForOrder(orderId)

                orderAmountLogic.revertOrder(oldOrderEntity, oldItems, currentUserId)

                val entityToUpdate = order.copy(isSynced = false, lastModified = System.currentTimeMillis())
                salesOrderDao.updateOrderWithItems(entityToUpdate, items)

                orderAmountLogic.processNewOrder(entityToUpdate, items, orderId)
            }

            val updatedOrderWithDetails = salesOrderDao.getOrderWithDetailsFlow(orderId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve order after update."))
            Result.success(updatedOrderWithDetails.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteOrder(orderLocalId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val currentUserId = sessionManager.getCurrentUser().first()?.id
                    ?: throw Exception("User not authenticated for delete operation")

                val orderEntity = salesOrderDao.getOrderEntityByLocalId(orderLocalId)
                    ?: throw NoSuchElementException("Order not found with localId: $orderLocalId")

                if (!orderEntity.isDeletedLocally) {
                    val items = salesOrderDao.getItemsForOrder(orderLocalId)

                    orderAmountLogic.revertOrder(orderEntity, items, currentUserId)

                    val orderToMarkAsDeleted = orderEntity.copy(
                        isDeletedLocally = true,
                        isSynced = false,
                        lastModified = System.currentTimeMillis()
                    )
                    salesOrderDao.updateOrder(orderToMarkAsDeleted)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDailySales(startDate: Long, endDate: Long): Flow<List<DailySale>> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        return salesOrderDao.getDailySales(startDate, endDate).map { dailyDataList ->
            dailyDataList.map { dailyData ->
                DailySale(
                    date = LocalDate.parse(dailyData.saleDate, formatter),
                    totalRevenue = dailyData.totalRevenue,
                    numberOfSales = dailyData.numberOfSales
                )
            }
        }
    }
}