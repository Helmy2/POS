package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.PurchaseReturnDao
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import com.wael.astimal.pos.features.reports.domain.entity.TransactionType
import com.wael.astimal.pos.features.user.data.local.EmployeeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PurchaseReturnRepositoryImpl(
    private val database: AppDatabase,
    private val purchaseReturnDao: PurchaseReturnDao,
    private val employeeDao: EmployeeDao,
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

    override suspend fun addPurchaseReturn(
        purchaseReturn: PurchaseReturnEntity,
        items: List<PurchaseReturnProductEntity>
    ): Result<PurchaseReturn> {
        return try {
            var insertedId: Long = -1
            database.withTransaction {
                val employeeId =
                    purchaseReturn.employeeLocalId ?: throw Exception("Employee ID missing.")
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                    ?: throw Exception("Store not found.")

                insertedId = purchaseReturnDao.insertPurchaseReturnWithItems(purchaseReturn, items)

                items.forEach { item ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = item.productLocalId,
                        transactionQuantity = -item.quantity
                    )
                }

                addPurchaseReturnLedgerEntries(purchaseReturn, insertedId)
            }
            val createdReturn = getPurchaseReturnDetails(insertedId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve purchase return after insert."))
            Result.success(createdReturn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePurchaseReturn(
        purchaseReturn: PurchaseReturnEntity,
        items: List<PurchaseReturnProductEntity>
    ): Result<PurchaseReturn> {
        return try {
            val returnId = purchaseReturn.localId
            database.withTransaction {
                val oldReturn = purchaseReturnDao.getPurchaseReturnWithDetails(returnId)
                    ?: throw NoSuchElementException("Original return not found")
                val employeeId =
                    purchaseReturn.employeeLocalId ?: throw Exception("Employee ID missing.")
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

                val entityToUpdate = purchaseReturn.copy(isSynced = false, lastModified = System.currentTimeMillis())
                purchaseReturnDao.updatePurchaseReturnWithItems(entityToUpdate, items)

                items.forEach { newItem ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = newItem.productLocalId,
                        transactionQuantity = -newItem.quantity
                    )
                }
                addPurchaseReturnLedgerEntries(entityToUpdate, returnId)
            }
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
            database.withTransaction {
                val returnToDelete = purchaseReturnDao.getPurchaseReturnWithDetails(localId)
                    ?: throw NoSuchElementException("Return not found")

                if (!returnToDelete.purchaseReturn.isDeletedLocally) {
                    val employeeId = returnToDelete.purchaseReturn.employeeLocalId ?: throw Exception("Employee ID missing.")
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
                        lastModified = System.currentTimeMillis()
                    )
                    purchaseReturnDao.updatePurchaseReturn(entityToMarkAsDeleted)
                }
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
        if (purchaseReturn.supplierLocalId != null) {
            partnerTransactionDao.insertTransaction(
                PartnerTransactionEntity(
                    clientId = null,
                    supplierId = purchaseReturn.supplierLocalId,
                    sourceTransactionId = returnId,
                    transactionType = TransactionType.PURCHASE_RETURN,
                    date = purchaseReturn.returnDate,
                    debit = purchaseReturn.totalAmount, // A purchase return reduces what you owe them
                    credit = 0.0
                )
            )
            if (purchaseReturn.amountPaid > 0) {
                partnerTransactionDao.insertTransaction(
                    PartnerTransactionEntity(
                        clientId = null,
                        supplierId = purchaseReturn.supplierLocalId,
                        sourceTransactionId = returnId,
                        transactionType = TransactionType.PAYMENT_RECEIVED,
                        date = purchaseReturn.returnDate,
                        debit = 0.0,
                        credit = purchaseReturn.amountPaid // Money received from a supplier is a credit
                    )
                )
            }
        }
    }
}
