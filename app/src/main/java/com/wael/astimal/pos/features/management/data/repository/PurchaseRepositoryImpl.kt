package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.formatSequence
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.PurchaseDao
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.repository.PurchaseRepository
import com.wael.astimal.pos.features.user.data.local.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PurchaseRepositoryImpl(
    private val database: AppDatabase,
    private val purchaseDao: PurchaseDao,
    private val employeeDao: UserDao,
    private val stockRepository: StockRepository,
    private val partnerTransactionDao: PartnerTransactionDao
) : PurchaseRepository {

    override fun getPurchases(): Flow<List<PurchaseOrder>> {
        return purchaseDao.getAllPurchasesWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getPurchaseDetails(localId: Long): PurchaseOrder? {
        return purchaseDao.getPurchaseWithDetails(localId)?.toDomain()
    }

    private suspend fun generateNextInvoiceNumber(): String {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prefix = "INV-PUR-$today-"
        val lastInvoice = purchaseDao.getLastInvoiceNumber("$prefix%")
        val nextSeq = if (lastInvoice == null) {
            1
        } else {
            lastInvoice.substringAfter(prefix).toIntOrNull()?.plus(1) ?: 1
        }
        return "$prefix${nextSeq.formatSequence()}"
    }

    override suspend fun addPurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseProductEntity>
    ): Result<PurchaseOrder> {
        return try {
            var insertedId: Long = -1
            database.withTransaction {
                val employeeId = purchase.employeeLocalId
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                    ?: throw Exception("Could not find an assigned store for the employee.")

                val newInvoiceNumber = generateNextInvoiceNumber()
                val purchaseWithInvoice = purchase.copy(invoiceNumber = newInvoiceNumber)

                insertedId = purchaseDao.insertPurchaseWithItems(purchaseWithInvoice, items)

                items.forEach { item ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = item.productLocalId,
                        transactionQuantity = item.quantity
                    )
                }
                addPurchaseLedgerEntries(purchaseWithInvoice, insertedId)
            }
            val createdPurchase = getPurchaseDetails(insertedId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve purchase after insert."))
            Result.success(createdPurchase)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseProductEntity>
    ): Result<PurchaseOrder> {
        return try {
            val purchaseId = purchase.localId
            database.withTransaction {
                val oldPurchase = purchaseDao.getPurchaseWithDetails(purchaseId)
                    ?: throw NoSuchElementException("Original purchase not found")
                val employeeId = purchase.employeeLocalId
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                    ?: throw Exception("Store not found.")

                // Revert old stock adjustments
                oldPurchase.itemsWithProductDetails.forEach { oldItem ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = oldItem.purchaseItem.productLocalId,
                        transactionQuantity = -oldItem.purchaseItem.quantity
                    )
                }
                // Delete old ledger entries
                partnerTransactionDao.deleteTransactionsBySource(
                    purchaseId,
                    TransactionType.PURCHASE,
                    TransactionType.PAYMENT_SENT
                )

                // Update purchase and items
                val entityToUpdate =
                    purchase.copy(isSynced = false, updatedAt = Clock.now())
                purchaseDao.updatePurchaseWithItems(entityToUpdate, items)

                // Apply new adjustments
                items.forEach { newItem ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = newItem.productLocalId,
                        transactionQuantity = newItem.quantity
                    )
                }

                // Re-add new ledger entries
                addPurchaseLedgerEntries(entityToUpdate, purchaseId)
            }
            val updatedPurchase = getPurchaseDetails(purchase.localId) ?: return Result.failure(
                IllegalStateException("Failed to retrieve purchase after update.")
            )
            Result.success(updatedPurchase)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePurchase(purchaseLocalId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val purchaseToDelete = purchaseDao.getPurchaseWithDetails(purchaseLocalId)
                    ?: throw NoSuchElementException("Purchase not found")

                if (!purchaseToDelete.purchase.isDeletedLocally) {
                    val employeeId = purchaseToDelete.purchase.employeeLocalId
                    val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                        ?: throw Exception("Store not found.")

                    purchaseToDelete.itemsWithProductDetails.forEach { item ->
                        stockRepository.adjustStock(
                            storeId = employeeStoreId,
                            productId = item.purchaseItem.productLocalId,
                            transactionQuantity = -item.purchaseItem.quantity
                        )
                    }

                    // Delete ledger entries
                    partnerTransactionDao.deleteTransactionsBySource(
                        purchaseLocalId,
                        TransactionType.PURCHASE,
                        TransactionType.PAYMENT_SENT
                    )

                    val entityToMarkAsDeleted = purchaseToDelete.purchase.copy(
                        isDeletedLocally = true,
                        isSynced = false,
                        updatedAt = Clock.now()
                    )
                    purchaseDao.updatePurchase(entityToMarkAsDeleted)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun addPurchaseLedgerEntries(purchase: PurchaseEntity, purchaseId: Long) {
        partnerTransactionDao.insertTransaction(
            PartnerTransactionEntity(
                serverId = null,
                clientId = null,
                supplierId = purchase.supplierLocalId,
                sourceTransactionId = purchaseId,
                transactionType = TransactionType.PURCHASE,
                createdAt = purchase.createdAt,
                updatedAt = purchase.updatedAt,
                debit = 0.0,
                credit = purchase.totalAmount
            )
        )
        if (purchase.amountPaid > 0) {
            partnerTransactionDao.insertTransaction(
                PartnerTransactionEntity(
                    serverId = null,
                    clientId = null,
                    supplierId = purchase.supplierLocalId,
                    sourceTransactionId = purchaseId,
                    transactionType = TransactionType.PAYMENT_SENT,
                    createdAt = purchase.createdAt,
                    updatedAt = purchase.updatedAt,
                    debit = purchase.amountPaid,
                    credit = 0.0
                )
            )
        }
    }
}