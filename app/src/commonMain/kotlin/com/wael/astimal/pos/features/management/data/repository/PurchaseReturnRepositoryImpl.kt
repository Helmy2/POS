package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.formatSequence
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.PurchaseReturnDao
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import com.wael.astimal.pos.features.user.data.local.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class PurchaseReturnRepositoryImpl(
    private val purchaseReturnDao: PurchaseReturnDao,
    private val employeeDao: UserDao,
    private val stockRepository: StockRepository,
    private val partnerTransactionDao: PartnerTransactionDao
) : PurchaseReturnRepository {

    override fun getPurchaseReturns(): Flow<List<PurchaseReturn>> {
        return purchaseReturnDao.getAllPurchaseReturnsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getPurchaseReturnDetails(localId: Long): PurchaseReturn? {
        return purchaseReturnDao.getPurchaseReturnWithDetails(localId)?.toDomain()
    }

    private suspend fun generateNextInvoiceNumber(): String {
        val random = Random.nextInt(1, 999999)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prefix = "INV-PUR-RET-$today-$random-"
        val lastInvoice = purchaseReturnDao.getLastInvoiceNumber("$prefix%")
        val nextSeq = if (lastInvoice == null) {
            1
        } else {
            lastInvoice.substringAfter(prefix).toIntOrNull()?.plus(1) ?: 1
        }
        return "$prefix${nextSeq.formatSequence()}"
    }

    override suspend fun addPurchaseReturn(
        purchaseReturn: PurchaseReturn,
    ): Result<PurchaseReturn> {
        return try {
            val (purchaseReturn, items) = purchaseReturn.toEntity()
            val employeeId =
                purchaseReturn.employeeLocalId
            val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                ?: throw Exception("Store not found.")

            val newInvoiceNumber = generateNextInvoiceNumber()
            val returnWithInvoice = purchaseReturn.copy(invoiceNumber = newInvoiceNumber)

            val insertedId: Long =
                purchaseReturnDao.insertPurchaseReturnWithItems(returnWithInvoice, items)

            items.forEach { item ->
                stockRepository.adjustStock(
                    storeId = employeeStoreId,
                    productId = item.productLocalId,
                    transactionQuantity = -item.quantity
                )
            }

            addPurchaseReturnLedgerEntries(returnWithInvoice, insertedId)

            val createdReturn = getPurchaseReturnDetails(insertedId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve purchase return after insert."))
            Result.success(createdReturn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePurchaseReturn(
        purchaseReturn: PurchaseReturn,
    ): Result<PurchaseReturn> {
        val (purchaseReturn, items) = purchaseReturn.toEntity()
        return try {
            val returnId = purchaseReturn.localId
            val oldReturn = purchaseReturnDao.getPurchaseReturnWithDetails(returnId)
                ?: throw NoSuchElementException("Original return not found")
            val employeeId =
                purchaseReturn.employeeLocalId
            val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                ?: throw Exception("Store not found.")

            oldReturn.itemsWithProductDetails.forEach { oldItem ->
                stockRepository.adjustStock(
                    storeId = employeeStoreId,
                    productId = oldItem.purchaseReturnItem.productLocalId,
                    transactionQuantity = oldItem.purchaseReturnItem.quantity
                )
            }
            partnerTransactionDao.deleteTransactionsBySource(
                returnId,
                TransactionType.PURCHASE_RETURN,
                TransactionType.PAYMENT_RECEIVED
            )

            val entityToUpdate =
                purchaseReturn.copy(isSynced = false, updatedAt = Clock.now())
            purchaseReturnDao.updatePurchaseReturnWithItems(entityToUpdate, items)

            items.forEach { newItem ->
                stockRepository.adjustStock(
                    storeId = employeeStoreId,
                    productId = newItem.productLocalId,
                    transactionQuantity = -newItem.quantity
                )
            }
            addPurchaseReturnLedgerEntries(entityToUpdate, returnId)

            val updatedReturn = getPurchaseReturnDetails(returnId) ?: return Result.failure(
                IllegalStateException("Failed to retrieve return after update.")
            )
            Result.success(updatedReturn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePurchaseReturn(localId: Long): Result<Unit> {
        return try {
            val returnToDelete = purchaseReturnDao.getPurchaseReturnWithDetails(localId)
                ?: throw NoSuchElementException("Return not found")

            if (!returnToDelete.purchaseReturn.isDeletedLocally) {
                val employeeId = returnToDelete.purchaseReturn.employeeLocalId
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                    ?: throw Exception("Store not found.")

                returnToDelete.itemsWithProductDetails.forEach { item ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = item.purchaseReturnItem.productLocalId,
                        transactionQuantity = item.purchaseReturnItem.quantity
                    )
                }

                partnerTransactionDao.deleteTransactionsBySource(
                    localId,
                    TransactionType.PURCHASE_RETURN,
                    TransactionType.PAYMENT_RECEIVED
                )

                val entityToMarkAsDeleted = returnToDelete.purchaseReturn.copy(
                    isDeletedLocally = true,
                    isSynced = false,
                    updatedAt = Clock.now()
                )
                purchaseReturnDao.updatePurchaseReturn(entityToMarkAsDeleted)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun addPurchaseReturnLedgerEntries(
        purchaseReturn: PurchaseReturnEntity,
        returnId: Long
    ) {
        partnerTransactionDao.insertTransaction(
            PartnerTransactionEntity(
                serverId = null,
                partnerLocalId = purchaseReturn.businessPartnerLocalId,
                sourceTransactionId = returnId,
                transactionType = TransactionType.PURCHASE_RETURN,
                createdAt = purchaseReturn.createdAt,
                updatedAt = purchaseReturn.updatedAt,
                debit = purchaseReturn.totalAmount, // A purchase return reduces what you owe them
                credit = 0.0
            )
        )
        if (purchaseReturn.amountPaid > 0) {
            partnerTransactionDao.insertTransaction(
                PartnerTransactionEntity(
                    serverId = null,
                    partnerLocalId = purchaseReturn.businessPartnerLocalId,
                    sourceTransactionId = returnId,
                    transactionType = TransactionType.PAYMENT_RECEIVED,
                    createdAt = purchaseReturn.createdAt,
                    updatedAt = purchaseReturn.updatedAt,
                    debit = 0.0,
                    credit = purchaseReturn.amountPaid // Money received from a supplier is a credit
                )
            )
        }
    }
}