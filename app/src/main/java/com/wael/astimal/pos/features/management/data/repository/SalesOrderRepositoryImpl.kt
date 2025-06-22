package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.util.formatSequence
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.management.data.logic.OrderAmountLogic
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SalesOrderRepositoryImpl(
    private val database: AppDatabase,
    private val salesOrderDao: SalesOrderDao,
    private val orderAmountLogic: OrderAmountLogic,
    private val partnerTransactionDao: PartnerTransactionDao,
    private val userRepository: UserRepository
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

    private suspend fun generateNextInvoiceNumber(): String {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prefix = "INV-$today-"
        val lastInvoice = salesOrderDao.getLastInvoiceNumber("$prefix%")
        val nextSeq = if (lastInvoice == null) {
            1
        } else {
            lastInvoice.substringAfter(prefix).toIntOrNull()?.plus(1) ?: 1
        }
        return "$prefix${nextSeq.formatSequence()}"
    }


    override suspend fun addOrder(
        order: OrderEntity,
        items: List<OrderProductEntity>
    ): Result<SalesOrder> {
        return try {
            var insertedOrderLocalId: Long = -1
            database.withTransaction {
                val newInvoiceNumber = generateNextInvoiceNumber()
                val orderWithInvoice = order.copy(invoiceNumber = newInvoiceNumber)
                insertedOrderLocalId = salesOrderDao.insertOrUpdateOrder(orderWithInvoice)
                val itemsWithCorrectId = items.map { it.copy(orderLocalId = insertedOrderLocalId) }
                salesOrderDao.insertOrderItems(itemsWithCorrectId)

                orderAmountLogic.processNewOrder(orderWithInvoice, items, insertedOrderLocalId)

                // Create ledger entry for the sale
                partnerTransactionDao.insertTransaction(
                    PartnerTransactionEntity(
                        clientId = orderWithInvoice.clientLocalId,
                        supplierId = null,
                        sourceTransactionId = insertedOrderLocalId,
                        transactionType = TransactionType.SALE,
                        createdAt = orderWithInvoice.createdAt,
                        updatedAt = orderWithInvoice.updatedAt,
                        debit = orderWithInvoice.totalAmount,
                        credit = 0.0,
                        serverId = null,
                    )
                )
                if (orderWithInvoice.amountPaid > 0) {
                    partnerTransactionDao.insertTransaction(
                        PartnerTransactionEntity(
                            clientId = orderWithInvoice.clientLocalId,
                            supplierId = null,
                            sourceTransactionId = insertedOrderLocalId,
                            transactionType = TransactionType.PAYMENT_RECEIVED,
                            createdAt = orderWithInvoice.createdAt,
                            updatedAt = orderWithInvoice.updatedAt, debit = 0.0,
                            credit = orderWithInvoice.amountPaid,
                            serverId = null,
                        )
                    )
                }
            }
            val createdOrder = getOrderDetailsFlow(insertedOrderLocalId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve order after insert."))
            Result.success(createdOrder)
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
            database.withTransaction {
                val currentUserId = userRepository.getCurrentUser()?.id
                    ?: throw Exception("User not authenticated for update operation")

                val oldOrderEntity = salesOrderDao.getOrderEntityByLocalId(orderId)
                    ?: throw NoSuchElementException("Original order not found for update.")
                val oldItems = salesOrderDao.getItemsForOrder(orderId)

                // Revert non-financial logic (stock, commissions)
                orderAmountLogic.revertOrder(oldOrderEntity, oldItems, currentUserId)
                // Delete old financial ledger entries
                partnerTransactionDao.deleteTransactionsBySource(
                    orderId,
                    TransactionType.SALE,
                    TransactionType.PAYMENT_RECEIVED
                )

                // Update the order and its items
                val entityToUpdate =
                    order.copy(isSynced = false, updatedAt = System.currentTimeMillis())
                salesOrderDao.updateOrderWithItems(entityToUpdate, items)

                // Re-process the non-financial logic
                orderAmountLogic.processNewOrder(entityToUpdate, items, orderId)
                // Re-create the financial ledger entries
                addOrderLedgerEntries(entityToUpdate, orderId)
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
                val currentUserId = userRepository.getCurrentUser()?.id
                    ?: throw Exception("User not authenticated for delete operation")

                val orderEntity = salesOrderDao.getOrderEntityByLocalId(orderLocalId)
                    ?: throw NoSuchElementException("Order not found with localId: $orderLocalId")

                if (!orderEntity.isDeletedLocally) {
                    val items = salesOrderDao.getItemsForOrder(orderLocalId)
                    orderAmountLogic.revertOrder(orderEntity, items, currentUserId)
                    partnerTransactionDao.deleteTransactionsBySource(
                        orderLocalId,
                        TransactionType.SALE,
                        TransactionType.PAYMENT_RECEIVED
                    )

                    val orderToMarkAsDeleted = orderEntity.copy(
                        isDeletedLocally = true,
                        isSynced = false,
                        updatedAt = System.currentTimeMillis()
                    )
                    salesOrderDao.updateOrder(orderToMarkAsDeleted)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun addOrderLedgerEntries(order: OrderEntity, orderId: Long) {
        partnerTransactionDao.insertTransaction(
            PartnerTransactionEntity(
                clientId = order.clientLocalId,
                supplierId = null,
                sourceTransactionId = orderId,
                transactionType = TransactionType.SALE,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                debit = order.totalAmount,
                credit = 0.0,
                serverId = null,
            )
        )
        if (order.amountPaid > 0) {
            partnerTransactionDao.insertTransaction(
                PartnerTransactionEntity(
                    clientId = order.clientLocalId,
                    supplierId = null,
                    sourceTransactionId = orderId,
                    transactionType = TransactionType.PAYMENT_RECEIVED,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt,
                    debit = 0.0,
                    credit = order.amountPaid,
                    serverId = null,
                )
            )
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