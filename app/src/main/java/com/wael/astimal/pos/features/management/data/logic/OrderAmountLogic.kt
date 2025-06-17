package com.wael.astimal.pos.features.management.data.logic

import com.wael.astimal.pos.core.util.ORDER_COMMISSION_PERCENTAGE
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.SaleCommissionEntity
import com.wael.astimal.pos.features.management.data.local.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.entity.SourceTransactionType
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.user.data.local.UserDao

class OrderAmountLogic(
    private val stockRepository: StockRepository,
    private val clientRepository: ClientRepository,
    private val employeeFinancesDao: EmployeeFinancesDao,
    private val employeeDao: UserDao
) {

    suspend fun processNewOrder(
        order: OrderEntity, items: List<OrderProductEntity>, orderId: Long
    ) {
        handleStockUpdate(order.employeeLocalId, items, isReturn = false)
        handleCommissions(order, orderId)
    }

    suspend fun revertOrder(
        order: OrderEntity, items: List<OrderProductEntity>, currentUserId: Long
    ) {
        handleStockUpdate(order.employeeLocalId, items, isReturn = true)
        revertCommissions(order.localId, currentUserId, order.invoiceNumber)
    }

    private suspend fun handleStockUpdate(
        employeeId: Long, items: List<OrderProductEntity>, isReturn: Boolean
    ) {
        val storeId = employeeDao.getStoreIdForEmployee(employeeId)
            ?: throw Exception("Could not find a store for the employee.")
        items.forEach { item ->
            val quantityChange = if (isReturn) item.quantity else -item.quantity
            stockRepository.adjustStock(storeId, item.productLocalId, quantityChange)
        }
    }

    private suspend fun handleCommissions(order: OrderEntity, orderId: Long) {
        val client = clientRepository.getClient(order.clientLocalId)
        val responsibleEmployeeId = client?.responsibleEmployee?.id
        val sellingEmployeeId = order.employeeLocalId

        val commissionAmount = order.totalAmount * ORDER_COMMISSION_PERCENTAGE

        if (responsibleEmployeeId == sellingEmployeeId) {
            createCommission(
                employeeId = sellingEmployeeId,
                orderId = orderId,
                commissionAmount = commissionAmount * 2,
                isMain = true,
                invoiceNumber = order.invoiceNumber,
                createdByEmployeeId = sellingEmployeeId
            )
        } else {
            createCommission(
                employeeId = sellingEmployeeId,
                orderId = orderId,
                commissionAmount = commissionAmount,
                isMain = true,
                invoiceNumber = order.invoiceNumber,
                createdByEmployeeId = order.employeeLocalId
            )
            if (responsibleEmployeeId != null) {
                createCommission(
                    employeeId = responsibleEmployeeId,
                    orderId = orderId,
                    commissionAmount = commissionAmount,
                    isMain = false,
                    invoiceNumber = order.invoiceNumber,
                    createdByEmployeeId = order.employeeLocalId
                )
            }
        }
    }


    private suspend fun createCommission(
        employeeId: Long,
        orderId: Long,
        commissionAmount: Double,
        isMain: Boolean,
        invoiceNumber: String,
        createdByEmployeeId: Long
    ) {
        val commission = SaleCommissionEntity(
            employeeId = employeeId,
            sourceTransactionId = orderId,
            sourceTransactionType = SourceTransactionType.SALE,
            commissionAmount = commissionAmount,
            isMain = isMain,
            date = System.currentTimeMillis(),
            serverId = null
        )
        val commissionId = employeeFinancesDao.insertSaleCommission(commission)

        val commissionTransaction = EmployeeAccountTransactionEntity(
            serverId = null,
            employeeId = employeeId,
            createdByEmployeeId = createdByEmployeeId,
            type = EmployeeTransactionType.COMMISSION,
            amount = commissionAmount,
            relatedCommissionId = commissionId,
            notes = "Commission for order #$invoiceNumber",
            lastModificationDate = System.currentTimeMillis(),
            creationDate = System.currentTimeMillis()
        )
        employeeFinancesDao.insertEmployeeTransaction(commissionTransaction)
    }

    private suspend fun revertCommissions(
        orderId: Long,
        currentUserId: Long,
        invoiceNumber: String,
    ) {
        val oldCommissions =
            employeeFinancesDao.getAllCommissionsBySource(orderId, SourceTransactionType.SALE)
        oldCommissions.forEach { commission ->
            employeeFinancesDao.insertEmployeeTransaction(
                EmployeeAccountTransactionEntity(
                    serverId = null,
                    employeeId = commission.employeeId,
                    createdByEmployeeId = currentUserId,
                    type = EmployeeTransactionType.COMMISSION,
                    amount = -commission.commissionAmount,
                    relatedCommissionId = commission.localId,
                    notes = "Reversal for order #${invoiceNumber}",
                    lastModificationDate = System.currentTimeMillis(),
                    creationDate = commission.date
                )
            )
        }
        employeeFinancesDao.deleteAllCommissionsBySource(orderId, SourceTransactionType.SALE)
    }

}