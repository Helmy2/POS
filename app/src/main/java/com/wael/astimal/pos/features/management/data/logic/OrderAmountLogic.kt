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
import com.wael.astimal.pos.features.user.data.local.EmployeeDao


class OrderAmountLogic(
    private val stockRepository: StockRepository,
    private val clientRepository: ClientRepository,
    private val employeeFinancesDao: EmployeeFinancesDao,
    private val employeeDao: EmployeeDao
) {

    suspend fun processNewOrder(
        order: OrderEntity,
        items: List<OrderProductEntity>,
        orderId: Long
    ) {
        val employeeStoreId = employeeDao.getStoreIdForEmployee(order.employeeLocalId)
            ?: throw Exception("Could not find an assigned store for the employee.")

        items.forEach { item ->
            stockRepository.adjustStock(
                storeId = employeeStoreId,
                productId = item.productLocalId,
                transactionQuantity = -item.quantity
            )
        }

        val debtChange = order.totalAmount - order.amountPaid
        clientRepository.adjustClientDebt(order.clientLocalId, debtChange)

        handleCommissions(order, orderId)
    }

    suspend fun revertOrder(
        order: OrderEntity,
        items: List<OrderProductEntity>,
        currentUserId: Long
    ) {
        val employeeStoreId = employeeDao.getStoreIdForEmployee(order.employeeLocalId)
            ?: throw Exception("Could not find an assigned store for the employee.")

        items.forEach { item ->
            stockRepository.adjustStock(
                storeId = employeeStoreId,
                productId = item.productLocalId,
                transactionQuantity = item.quantity
            )
        }


        val debtChange = order.totalAmount - order.amountPaid
        clientRepository.adjustClientDebt(order.clientLocalId, -debtChange)

        val oldCommissions =
            employeeFinancesDao.getAllCommissionsBySource(order.localId, SourceTransactionType.SALE)
        oldCommissions.forEach { commission ->
            employeeFinancesDao.insertEmployeeTransaction(
                EmployeeAccountTransactionEntity(
                    serverId = null,
                    employeeId = commission.employeeId,
                    createdByEmployeeId = currentUserId,
                    type = EmployeeTransactionType.COMMISSION,
                    amount = -commission.commissionAmount,
                    relatedCommissionId = commission.localId,
                    notes = "Reversal for order #${order.localId}",
                    date = System.currentTimeMillis()
                )
            )
        }
        employeeFinancesDao.deleteAllCommissionsBySource(order.localId, SourceTransactionType.SALE)
    }

    private suspend fun handleCommissions(order: OrderEntity, orderId: Long) {
        val client = clientRepository.getClient(order.clientLocalId)
        val responsibleEmployeeId = client?.responsibleEmployee?.id
        val sellingEmployeeId = order.employeeLocalId

        val commissionAmount = order.totalAmount * ORDER_COMMISSION_PERCENTAGE

        createCommission(
            employeeId = sellingEmployeeId,
            orderId = orderId,
            commissionAmount = commissionAmount,
            isMain = true,
            createdByEmployeeId = sellingEmployeeId
        )

        if (responsibleEmployeeId != null && responsibleEmployeeId != sellingEmployeeId) {
            createCommission(
                employeeId = responsibleEmployeeId,
                orderId = orderId,
                commissionAmount = commissionAmount,
                isMain = false,
                createdByEmployeeId = sellingEmployeeId
            )
        }
    }

    private suspend fun createCommission(
        employeeId: Long,
        orderId: Long,
        commissionAmount: Double,
        isMain: Boolean,
        createdByEmployeeId: Long
    ) {
        val commissionEntity = SaleCommissionEntity(
            serverId = null,
            employeeId = employeeId,
            sourceTransactionId = orderId,
            sourceTransactionType = SourceTransactionType.SALE,
            commissionAmount = commissionAmount,
            isMain = isMain,
            date = System.currentTimeMillis()
        )
        val commissionId = employeeFinancesDao.insertSaleCommission(commissionEntity)

        val commissionTransaction = EmployeeAccountTransactionEntity(
            serverId = null,
            employeeId = employeeId,
            createdByEmployeeId = createdByEmployeeId,
            type = EmployeeTransactionType.COMMISSION,
            amount = commissionAmount,
            relatedCommissionId = commissionId,
            notes = "Commission for order #$orderId",
            date = System.currentTimeMillis()
        )
        employeeFinancesDao.insertEmployeeTransaction(commissionTransaction)
    }
}