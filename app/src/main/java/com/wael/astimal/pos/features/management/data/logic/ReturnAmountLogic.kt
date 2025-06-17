package com.wael.astimal.pos.features.management.data.logic

import com.wael.astimal.pos.core.util.ORDER_COMMISSION_PERCENTAGE
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.SaleCommissionEntity
import com.wael.astimal.pos.features.management.data.local.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.entity.SourceTransactionType
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.user.data.local.UserDao

class ReturnAmountLogic(
    private val stockRepository: StockRepository,
    private val clientRepository: ClientRepository,
    private val employeeFinancesDao: EmployeeFinancesDao,
    private val employeeDao: UserDao
) {
    suspend fun processNewReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>,
        returnId: Long
    ) {
        handleStockUpdate(returnEntity, items, isReverting = false)
        handleCommissions(returnEntity, returnId)
    }

    suspend fun revertReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>,
        currentUserId: Long
    ) {
        handleStockUpdate(returnEntity, items, isReverting = true)
        revertCommissions(returnEntity.localId, currentUserId, returnEntity.invoiceNumber)
    }

    private suspend fun handleStockUpdate(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>,
        isReverting: Boolean
    ) {
        val employeeId =
            returnEntity.employeeLocalId ?: throw Exception("Employee not found for return")
        val storeId = employeeDao.getStoreIdForEmployee(employeeId)
            ?: throw Exception("Could not find a store for the employee.")

        items.forEach { item ->
            val quantityChange = if (isReverting) -item.quantity else item.quantity
            stockRepository.adjustStock(storeId, item.productLocalId, quantityChange)
        }
    }

    private suspend fun handleCommissions(returnEntity: OrderReturnEntity, returnId: Long) {
        val client = clientRepository.getClient(returnEntity.clientLocalId)
        val responsibleEmployeeId = client?.responsibleEmployee?.id
        val returningEmployeeId = returnEntity.employeeLocalId
            ?: throw IllegalStateException("Returning employee ID is null")

        val commissionAmount = returnEntity.totalAmount * ORDER_COMMISSION_PERCENTAGE

        if (responsibleEmployeeId == returningEmployeeId) {
            createCommission(
                employeeId = returningEmployeeId,
                returnId = returnId,
                commissionAmount = -commissionAmount * 2, // Negative for return
                isMain = true,
                invoiceNumber = returnEntity.invoiceNumber,
                createdByEmployeeId = returningEmployeeId
            )
        } else {
            createCommission(
                employeeId = returningEmployeeId,
                returnId = returnId,
                commissionAmount = -commissionAmount, // Negative for return
                isMain = true,
                invoiceNumber = returnEntity.invoiceNumber,
                createdByEmployeeId = returningEmployeeId
            )
            if (responsibleEmployeeId != null) {
                createCommission(
                    employeeId = responsibleEmployeeId,
                    returnId = returnId,
                    commissionAmount = -commissionAmount, // Negative for return
                    isMain = false,
                    invoiceNumber = returnEntity.invoiceNumber,
                    createdByEmployeeId = returningEmployeeId
                )
            }
        }
    }

    private suspend fun createCommission(
        employeeId: Long,
        returnId: Long,
        commissionAmount: Double,
        isMain: Boolean,
        invoiceNumber: String,
        createdByEmployeeId: Long
    ) {
        val commission = SaleCommissionEntity(
            employeeId = employeeId,
            sourceTransactionId = returnId,
            sourceTransactionType = SourceTransactionType.SALE_RETURN,
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
            amount = commissionAmount, // Already negative
            relatedCommissionId = commissionId,
            notes = "Commission for return #$invoiceNumber",
            lastModificationDate = System.currentTimeMillis(),
            creationDate = System.currentTimeMillis()
        )
        employeeFinancesDao.insertEmployeeTransaction(commissionTransaction)
    }

    private suspend fun revertCommissions(
        returnId: Long,
        currentUserId: Long,
        invoiceNumber: String
    ) {
        val oldCommissions = employeeFinancesDao.getAllCommissionsBySource(
            returnId,
            SourceTransactionType.SALE_RETURN
        )
        oldCommissions.forEach { commission ->
            employeeFinancesDao.insertEmployeeTransaction(
                EmployeeAccountTransactionEntity(
                    serverId = null,
                    employeeId = commission.employeeId,
                    createdByEmployeeId = currentUserId,
                    type = EmployeeTransactionType.COMMISSION,
                    amount = -commission.commissionAmount, // Revert the negative amount
                    relatedCommissionId = commission.localId,
                    notes = "Reversal for return #${invoiceNumber}",
                    lastModificationDate = System.currentTimeMillis(),
                    creationDate = commission.date
                )
            )
        }
        employeeFinancesDao.deleteAllCommissionsBySource(
            returnId,
            SourceTransactionType.SALE_RETURN
        )
    }
}