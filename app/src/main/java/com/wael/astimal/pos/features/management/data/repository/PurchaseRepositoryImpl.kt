package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PurchaseDao
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.repository.PurchaseRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.user.data.local.EmployeeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PurchaseRepositoryImpl(
    private val database: AppDatabase,
    private val purchaseDao: PurchaseDao,
    private val employeeDao: EmployeeDao,
    private val stockRepository: StockRepository,
    private val supplierRepository: SupplierRepository,
) : PurchaseRepository {

    override fun getPurchases(): Flow<List<PurchaseOrder>> {
        return purchaseDao.getAllPurchasesWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addPurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseProductEntity>
    ): Result<PurchaseOrder> {
        return try {
            var insertedId: Long = -1
            database.withTransaction {
                val employeeId = purchase.employeeLocalId
                    ?: throw Exception("Employee ID is missing on the purchase record.")
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                    ?: throw Exception("Could not find an assigned store for the employee.")

                insertedId = purchaseDao.insertPurchaseWithItems(purchase, items)

                items.forEach { item ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = item.productLocalId,
                        transactionUnitId = item.unitLocalId,
                        transactionQuantity = item.quantity
                    )
                }

                if (purchase.supplierLocalId != null) {
                    supplierRepository.adjustSupplierIndebtedness(
                        supplierLocalId = purchase.supplierLocalId,
                        changeInDebt = purchase.totalPrice
                    )
                }
            }
            val createdPurchase = purchaseDao.getPurchaseWithDetails(insertedId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve purchase after insert."))
            Result.success(createdPurchase.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseProductEntity>
    ): Result<PurchaseOrder> {
        return try {
            if (purchase.localId == 0L) {
                return Result.failure(IllegalArgumentException("Purchase localId is missing for update operation."))
            }

            database.withTransaction {
                val employeeId = purchase.employeeLocalId
                    ?: throw Exception("Employee ID is missing on the purchase record.")
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                    ?: throw Exception("Could not find an assigned store for the employee.")

                val oldPurchaseWithDetails = purchaseDao.getPurchaseWithDetails(purchase.localId)
                if (oldPurchaseWithDetails != null) {
                    // Revert old stock and debt
                    oldPurchaseWithDetails.itemsWithProductDetails.forEach { oldItem ->
                        stockRepository.adjustStock(
                            storeId = employeeStoreId,
                            productId = oldItem.purchaseItem.productLocalId,
                            transactionUnitId = oldItem.purchaseItem.unitLocalId,
                            transactionQuantity = -oldItem.purchaseItem.quantity // Decrease stock to revert
                        )
                    }
                    if (oldPurchaseWithDetails.purchase.supplierLocalId != null) {
                        supplierRepository.adjustSupplierIndebtedness(
                            supplierLocalId = oldPurchaseWithDetails.purchase.supplierLocalId,
                            changeInDebt = -oldPurchaseWithDetails.purchase.totalPrice // Decrease debt to revert
                        )
                    }
                }

                val entityToUpdate = purchase.copy(isSynced = false, lastModified = System.currentTimeMillis())
                purchaseDao.updatePurchaseWithItems(entityToUpdate, items)

                // Apply new stock and debt
                items.forEach { newItem ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = newItem.productLocalId,
                        transactionUnitId = newItem.unitLocalId,
                        transactionQuantity = newItem.quantity // Increase stock
                    )
                }
                if (entityToUpdate.supplierLocalId != null) {
                    supplierRepository.adjustSupplierIndebtedness(
                        supplierLocalId = entityToUpdate.supplierLocalId,
                        changeInDebt = entityToUpdate.totalPrice // Increase debt
                    )
                }
            }
            val updatedPurchase = purchaseDao.getPurchaseWithDetails(purchase.localId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve purchase after update."))
            Result.success(updatedPurchase.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePurchase(purchaseLocalId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val purchaseToDelete = purchaseDao.getPurchaseWithDetails(purchaseLocalId)
                    ?: throw NoSuchElementException("Purchase not found with localId: $purchaseLocalId")

                if (!purchaseToDelete.purchase.isDeletedLocally) {
                    val employeeId = purchaseToDelete.purchase.employeeLocalId
                        ?: throw Exception("Employee ID missing on purchase record.")
                    val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                        ?: throw Exception("Could not find an assigned store for the employee.")

                    // Revert stock and debt
                    purchaseToDelete.itemsWithProductDetails.forEach { item ->
                        stockRepository.adjustStock(
                            storeId = employeeStoreId,
                            productId = item.purchaseItem.productLocalId,
                            transactionUnitId = item.purchaseItem.unitLocalId,
                            transactionQuantity = -item.purchaseItem.quantity // DECREASE stock to revert
                        )
                    }
                    if (purchaseToDelete.purchase.supplierLocalId != null) {
                        supplierRepository.adjustSupplierIndebtedness(
                            supplierLocalId = purchaseToDelete.purchase.supplierLocalId,
                            changeInDebt = -purchaseToDelete.purchase.totalPrice // DECREASE indebtedness to revert
                        )
                    }

                    val entityToMarkAsDeleted = purchaseToDelete.purchase.copy(
                        isDeletedLocally = true, isSynced = false, lastModified = System.currentTimeMillis()
                    )
                    purchaseDao.updatePurchase(entityToMarkAsDeleted)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}