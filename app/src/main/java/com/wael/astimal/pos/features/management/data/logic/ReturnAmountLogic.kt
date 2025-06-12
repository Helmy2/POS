package com.wael.astimal.pos.features.management.data.logic

import com.wael.astimal.pos.core.util.RETURN_COMMISSION_PERCENTAGE
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.SaleCommissionEntity
import com.wael.astimal.pos.features.management.data.local.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SourceTransactionType
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.user.data.local.EmployeeDao


class ReturnAmountLogic(
    private val stockRepository: StockRepository,
    private val clientRepository: ClientRepository,
    private val employeeFinancesDao: EmployeeFinancesDao,
    private val employeeDao: EmployeeDao
) {

    suspend fun processNewReturn(returnEntity: OrderReturnEntity, items: List<OrderProductEntity>, returnId: Long) {
        val employeeStoreId = employeeDao.getStoreIdForEmployee(returnEntity.employeeLocalId)
            ?: throw Exception("Could not find an assigned store for the employee.")

        items.forEach { item ->
            stockRepository.adjustStock(
                storeId = employeeStoreId,
                productId = item.productLocalId,
                transactionUnitId = item.unitLocalId,
                transactionQuantity = item.quantity
            )
        }

        if (returnEntity.paymentType == PaymentType.DEFERRED) {
            val debtChange = returnEntity.totalAmount - returnEntity.amountPaid
            clientRepository.adjustClientDebt(returnEntity.clientLocalId, -debtChange)
        }

        handleCommissions(returnEntity, returnId)
    }

    suspend fun revertReturn(returnEntity: OrderReturnEntity, items: List<OrderProductEntity>, currentUserId: Long) {
        val employeeStoreId = employeeDao.getStoreIdForEmployee(returnEntity.employeeLocalId)
            ?: throw Exception("Could not find an assigned store for the employee.")

        items.forEach { item ->
            stockRepository.adjustStock(
                storeId = employeeStoreId,
                productId = item.productLocalId,
                transactionUnitId = item.unitLocalId,
                transactionQuantity = -item.quantity
            )
        }

        if (returnEntity.paymentType == PaymentType.DEFERRED) {
            val debtChange = returnEntity.totalAmount - returnEntity.amountPaid
            clientRepository.adjustClientDebt(returnEntity.clientLocalId, debtChange)
        }

        val oldCommissions = employeeFinancesDao.getAllCommissionsBySource(returnEntity.localId, SourceTransactionType.SALE_RETURN)
        oldCommissions.forEach { commission ->
            employeeFinancesDao.insertEmployeeTransaction(
                EmployeeAccountTransactionEntity(
                    serverId = null,
                    employeeId = commission.employeeId,
                    createdByEmployeeId = currentUserId,
                    type = EmployeeTransactionType.COMMISSION,
                    amount = -commission.commissionAmount,
                    relatedCommissionId = commission.localId,
                    notes = "Reversal for deleted return #${returnEntity.localId}",
                    date = System.currentTimeMillis()
                )
            )
        }
        employeeFinancesDao.deleteAllCommissionsBySource(returnEntity.localId, SourceTransactionType.SALE_RETURN)
    }

    private suspend fun handleCommissions(returnEntity: OrderReturnEntity, returnId: Long) {
        val client = clientRepository.getClient(returnEntity.clientLocalId)
        val responsibleEmployeeId = client?.responsibleEmployee?.id
        val returningEmployeeId = returnEntity.employeeLocalId

        val commissionAmount = returnEntity.totalAmount * RETURN_COMMISSION_PERCENTAGE

        createCommission(
            employeeId = returningEmployeeId!!,
            returnId = returnId,
            commissionAmount = -commissionAmount,
            isMain = true,
            createdByEmployeeId = returningEmployeeId
        )

        if (responsibleEmployeeId != null && responsibleEmployeeId != returningEmployeeId) {
            createCommission(
                employeeId = responsibleEmployeeId,
                returnId = returnId,
                commissionAmount = -commissionAmount,
                isMain = false,
                createdByEmployeeId = returningEmployeeId
            )
        }
    }

    private suspend fun createCommission(employeeId: Long, returnId: Long, commissionAmount: Double, isMain: Boolean, createdByEmployeeId: Long) {
        val commissionEntity = SaleCommissionEntity(
            serverId = null,
            employeeId = employeeId,
            sourceTransactionId = returnId,
            sourceTransactionType = SourceTransactionType.SALE_RETURN,
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
            notes = "Commission reversal for return #$returnId",
            date = System.currentTimeMillis()
        )
        employeeFinancesDao.insertEmployeeTransaction(commissionTransaction)
    }
}
