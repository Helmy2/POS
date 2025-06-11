package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.user.data.local.EmployeeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SalesOrderRepositoryImpl(
    private val database: AppDatabase,
    private val salesOrderDao: SalesOrderDao,
    private val employeeDao: EmployeeDao,
    private val stockRepository: StockRepository,
    private val clientRepository: ClientRepository
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
                val employeeStoreId = employeeDao.getStoreIdForEmployee(order.employeeLocalId)
                    ?: throw Exception("Could not find an assigned store for the employee.")

                insertedOrderLocalId = salesOrderDao.insertOrUpdateOrder(order)
                val itemsWithCorrectId = items.map { it.copy(orderLocalId = insertedOrderLocalId) }
                salesOrderDao.insertOrderItems(itemsWithCorrectId)

                items.forEach { item ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = item.productLocalId,
                        transactionUnitId = item.unitLocalId,
                        transactionQuantity = -item.quantity
                    )
                }

                // --- UPDATED LOGIC ---
                // Only adjust debt if the payment type is DEFERRED (credit)
                if (order.paymentType == PaymentType.DEFERRED) {
                    val debtChange = order.totalPrice - order.amountPaid
                    clientRepository.adjustClientDebt(order.clientLocalId, debtChange)
                }
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
            if (order.localId == 0L) {
                return Result.failure(IllegalArgumentException("Order localId is missing for update operation."))
            }

            database.withTransaction {
                val employeeStoreId = employeeDao.getStoreIdForEmployee(order.employeeLocalId)
                    ?: throw Exception("Could not find an assigned store for the employee.")

                val oldOrderEntity = salesOrderDao.getOrderEntityByLocalId(order.localId)
                    ?: throw NoSuchElementException("Original order not found for update.")

                // Revert old stock
                val oldItems = salesOrderDao.getItemsForOrder(order.localId)
                oldItems.forEach { oldItem ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = oldItem.productLocalId,
                        transactionUnitId = oldItem.unitLocalId,
                        transactionQuantity = oldItem.quantity
                    )
                }

                // --- UPDATED LOGIC ---
                // Revert old debt ONLY if the old order was DEFERRED
                if (oldOrderEntity.paymentType == PaymentType.DEFERRED) {
                    val oldDebtChange = oldOrderEntity.totalPrice - oldOrderEntity.amountPaid
                    clientRepository.adjustClientDebt(oldOrderEntity.clientLocalId, -oldDebtChange)
                }

                val entityToUpdate = order.copy(isSynced = false, lastModified = System.currentTimeMillis())
                salesOrderDao.updateOrderWithItems(entityToUpdate, items)

                // Apply new stock adjustments
                items.forEach { newItem ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = newItem.productLocalId,
                        transactionUnitId = newItem.unitLocalId,
                        transactionQuantity = -newItem.quantity
                    )
                }

                // --- UPDATED LOGIC ---
                // Apply new debt adjustment ONLY if the new order is DEFERRED
                if (entityToUpdate.paymentType == PaymentType.DEFERRED) {
                    val newDebtChange = entityToUpdate.totalPrice - entityToUpdate.amountPaid
                    clientRepository.adjustClientDebt(entityToUpdate.clientLocalId, newDebtChange)
                }
            }

            val updatedOrderWithDetails = salesOrderDao.getOrderWithDetailsFlow(order.localId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve order after update."))

            Result.success(updatedOrderWithDetails.toDomain())
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteOrder(orderLocalId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val orderEntity = salesOrderDao.getOrderEntityByLocalId(orderLocalId)
                    ?: throw NoSuchElementException("Order not found with localId: $orderLocalId")

                if (!orderEntity.isDeletedLocally) {
                    val employeeStoreId = employeeDao.getStoreIdForEmployee(orderEntity.employeeLocalId)
                        ?: throw Exception("Could not find an assigned store for the employee.")

                    // Revert stock
                    val itemsToRevert = salesOrderDao.getItemsForOrder(orderLocalId)
                    itemsToRevert.forEach { item ->
                        stockRepository.adjustStock(
                            storeId = employeeStoreId,
                            productId = item.productLocalId,
                            transactionUnitId = item.unitLocalId,
                            transactionQuantity = item.quantity
                        )
                    }

                    // --- UPDATED LOGIC ---
                    // Revert debt ONLY if the order was DEFERRED
                    if (orderEntity.paymentType == PaymentType.DEFERRED) {
                        val debtChange = orderEntity.totalPrice - orderEntity.amountPaid
                        clientRepository.adjustClientDebt(orderEntity.clientLocalId, -debtChange)
                    }

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
}