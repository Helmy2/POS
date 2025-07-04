package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.formatSequence
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderWithDetailsEntity
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.management.data.logic.OrderAmountLogic
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class SalesOrderRepositoryImpl(
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
        val random = Random.nextInt(1, 999999)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prefix = "INV-$today-$random-"
        val lastInvoice = salesOrderDao.getLastInvoiceNumber("$prefix%")
        val nextSeq = if (lastInvoice == null) {
            1
        } else {
            lastInvoice.substringAfter(prefix).toIntOrNull()?.plus(1) ?: 1
        }
        return "$prefix${nextSeq.formatSequence()}"
    }


    override suspend fun addOrder(
        order: SalesOrder,
    ): Result<SalesOrder> {
        return runCatching {
            val (orderEntity, items) = order.toEntity()
            addOrder(
                orderEntity, items, newInvoiceNumber = generateNextInvoiceNumber()
            ).getOrThrow()
        }
    }

    private suspend fun addOrder(
        orderEntity: OrderEntity,
        items: List<OrderProductEntity>,
        newInvoiceNumber: String? = null,
    ): Result<SalesOrder> {
        return runCatching {
            val orderWithInvoice =
                newInvoiceNumber?.let { orderEntity.copy(invoiceNumber = it) } ?: orderEntity

            val insertedOrderLocalId: Long = salesOrderDao.insertOrUpdateOrder(orderWithInvoice)
            val itemsWithCorrectId = items.map { it.copy(orderLocalId = insertedOrderLocalId) }
            salesOrderDao.insertOrderItems(itemsWithCorrectId)

            orderAmountLogic.processNewOrder(orderWithInvoice, items, insertedOrderLocalId)

            // Create ledger entry for the sale
            partnerTransactionDao.insertTransaction(
                PartnerTransactionEntity(
                    partnerLocalId = orderWithInvoice.businessPartnerLocalId,
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
                        partnerLocalId = orderWithInvoice.businessPartnerLocalId,
                        sourceTransactionId = insertedOrderLocalId,
                        transactionType = TransactionType.PAYMENT_RECEIVED,
                        createdAt = orderWithInvoice.createdAt,
                        updatedAt = orderWithInvoice.updatedAt, debit = 0.0,
                        credit = orderWithInvoice.amountPaid,
                        serverId = null,
                    )
                )
            }
            val createdOrder =
                getOrderDetailsFlow(insertedOrderLocalId).first() ?: return Result.failure(
                    IllegalStateException("Failed to retrieve order after insert.")
                )
            createdOrder
        }
    }

    override suspend fun updateOrder(
        order: SalesOrder,
    ): Result<SalesOrder> {
        return runCatching {
            val (orderEntity, items) = order.toEntity()
            updateOrder(orderEntity, items).getOrThrow()
        }
    }

    private suspend fun updateOrder(
        orderEntity: OrderEntity, items: List<OrderProductEntity>
    ): Result<SalesOrder> {
        return runCatching {
            val orderId = orderEntity.localId
            val currentUserId = userRepository.getCurrentUser()?.id
                ?: throw Exception("User not authenticated for update operation")

            val oldOrderEntity = salesOrderDao.getOrderEntityByLocalId(orderId)
                ?: throw NoSuchElementException("Original order not found for update.")
            val oldItems = salesOrderDao.getItemsForOrder(orderId)

            // Revert non-financial logic (stock, commissions)
            orderAmountLogic.revertOrder(oldOrderEntity, oldItems, currentUserId.local)
            // Delete old financial ledger entries
            partnerTransactionDao.deleteTransactionsBySource(
                orderId, TransactionType.SALE, TransactionType.PAYMENT_RECEIVED
            )

            // Update the order and its items
            val entityToUpdate = orderEntity.copy(isSynced = false, updatedAt = Clock.now())
            salesOrderDao.updateOrderWithItems(entityToUpdate, items)

            // Re-process the non-financial logic
            orderAmountLogic.processNewOrder(entityToUpdate, items, orderId)
            // Re-create the financial ledger entries
            addOrderLedgerEntries(entityToUpdate, orderId)
            val updatedOrderWithDetails =
                salesOrderDao.getOrderWithDetailsFlow(orderId).first() ?: return Result.failure(
                    IllegalStateException("Failed to retrieve order after update.")
                )
            updatedOrderWithDetails.toDomain()
        }
    }


    override suspend fun deleteOrder(orderLocalId: Long): Result<Unit> {
        return try {
            val currentUserId = userRepository.getCurrentUser()?.id
                ?: throw Exception("User not authenticated for delete operation")

            val orderEntity =
                salesOrderDao.getOrderEntityByLocalId(orderLocalId) ?: throw NoSuchElementException(
                    "Order not found with localId: $orderLocalId"
                )

            if (!orderEntity.isDeletedLocally) {
                val items = salesOrderDao.getItemsForOrder(orderLocalId)
                orderAmountLogic.revertOrder(orderEntity, items, currentUserId.local)
                partnerTransactionDao.deleteTransactionsBySource(
                    orderLocalId, TransactionType.SALE, TransactionType.PAYMENT_RECEIVED
                )

                val orderToMarkAsDeleted = orderEntity.copy(
                    isDeletedLocally = true, isSynced = false, updatedAt = Clock.now()
                )
                salesOrderDao.updateOrder(orderToMarkAsDeleted)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun addOrderLedgerEntries(order: OrderEntity, orderId: Long) {
        partnerTransactionDao.insertTransaction(
            PartnerTransactionEntity(
                partnerLocalId = order.businessPartnerLocalId,
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
                    partnerLocalId = order.businessPartnerLocalId,
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

    override suspend fun syncWithServer(orderEntities: List<Pair<OrderEntity, List<OrderProductEntity>>>): Result<Unit> {
        return runCatching {
            orderEntities.forEach { (order, items) ->
                val existingOrder = salesOrderDao.getOrderByServerId(order.serverId!!)
                val newOrder = order.copy(localId = existingOrder?.localId ?: 0L)

                if (newOrder.localId != 0L) {
                    updateOrder(newOrder, items)
                } else {
                    addOrder(newOrder, items)
                }
            }
        }
    }

    override suspend fun getLocalChanges(): Result<List<OrderWithDetailsEntity>> {
        return runCatching {
            salesOrderDao.getUnsyncedOrders()
        }
    }
}