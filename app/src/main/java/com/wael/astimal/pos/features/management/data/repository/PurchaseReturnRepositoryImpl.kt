package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PurchaseReturnDao
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.user.data.local.EmployeeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PurchaseReturnRepositoryImpl(
    private val database: AppDatabase,
    private val purchaseReturnDao: PurchaseReturnDao,
    private val employeeDao: EmployeeDao,
    private val stockRepository: StockRepository,
    private val supplierRepository: SupplierRepository,
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
                val employeeId = purchaseReturn.employeeLocalId
                    ?: throw Exception("Employee ID is missing on the return record.")
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                    ?: throw Exception("Could not find an assigned store for the employee.")

                insertedId = purchaseReturnDao.insertPurchaseReturnWithItems(purchaseReturn, items)

                items.forEach { item ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = item.productLocalId,
                        transactionUnitId = item.unitLocalId,
                        transactionQuantity = -item.quantity // DECREASE stock for a purchase return
                    )
                }

                if (purchaseReturn.supplierLocalId != null) {
                    supplierRepository.adjustSupplierIndebtedness(
                        supplierLocalId = purchaseReturn.supplierLocalId,
                        changeInDebt = -purchaseReturn.amountRemaining // DECREASE indebtedness to supplier
                    )
                }
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
            if (purchaseReturn.localId == 0L) {
                return Result.failure(IllegalArgumentException("Purchase Return localId is missing for update."))
            }

            database.withTransaction {
                val employeeId = purchaseReturn.employeeLocalId ?: throw Exception("Employee ID is missing.")
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId) ?: throw Exception("Employee's store not found.")

                val oldReturn = purchaseReturnDao.getPurchaseReturnWithDetails(purchaseReturn.localId)
                if (oldReturn != null) {
                    // Revert old adjustments
                    oldReturn.itemsWithProductDetails.forEach { oldItem ->
                        stockRepository.adjustStock(
                            storeId = employeeStoreId,
                            productId = oldItem.purchaseReturnItem.productLocalId,
                            transactionUnitId = oldItem.purchaseReturnItem.unitLocalId,
                            transactionQuantity = oldItem.purchaseReturnItem.quantity // Add stock back
                        )
                    }
                    if (oldReturn.purchaseReturn.supplierLocalId != null) {
                        supplierRepository.adjustSupplierIndebtedness(oldReturn.purchaseReturn.supplierLocalId, oldReturn.purchaseReturn.amountRemaining)
                    }
                }

                val entityToUpdate = purchaseReturn.copy(isSynced = false, lastModified = System.currentTimeMillis())
                purchaseReturnDao.updatePurchaseReturnWithItems(entityToUpdate, items)

                // Apply new adjustments
                items.forEach { newItem ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = newItem.productLocalId,
                        transactionUnitId = newItem.unitLocalId,
                        transactionQuantity = -newItem.quantity // Decrease stock
                    )
                }
                if (entityToUpdate.supplierLocalId != null) {
                    supplierRepository.adjustSupplierIndebtedness(entityToUpdate.supplierLocalId, -entityToUpdate.amountRemaining)
                }
            }
            val updatedReturn = getPurchaseReturnDetails(purchaseReturn.localId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve purchase return after update."))
            Result.success(updatedReturn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePurchaseReturn(localId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val returnToDelete = purchaseReturnDao.getPurchaseReturnWithDetails(localId)
                    ?: throw NoSuchElementException("Purchase Return not found with localId: $localId")

                if (!returnToDelete.purchaseReturn.isDeletedLocally) {
                    val employeeId = returnToDelete.purchaseReturn.employeeLocalId ?: throw Exception("Employee ID missing.")
                    val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId) ?: throw Exception("Employee's store not found.")

                    // Revert stock and debt adjustments
                    returnToDelete.itemsWithProductDetails.forEach { item ->
                        stockRepository.adjustStock(
                            storeId = employeeStoreId,
                            productId = item.purchaseReturnItem.productLocalId,
                            transactionUnitId = item.purchaseReturnItem.unitLocalId,
                            transactionQuantity = item.purchaseReturnItem.quantity
                        )
                    }
                    if (returnToDelete.purchaseReturn.supplierLocalId != null) {
                        supplierRepository.adjustSupplierIndebtedness(
                            supplierLocalId = returnToDelete.purchaseReturn.supplierLocalId,
                            changeInDebt = returnToDelete.purchaseReturn.amountRemaining
                        )
                    }

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
}