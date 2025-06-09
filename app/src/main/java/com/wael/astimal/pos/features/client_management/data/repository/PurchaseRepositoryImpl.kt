package com.wael.astimal.pos.features.client_management.data.repository

import com.wael.astimal.pos.features.client_management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.client_management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.client_management.data.entity.toDomain
import com.wael.astimal.pos.features.client_management.data.entity.toDomainPlaceholder
import com.wael.astimal.pos.features.client_management.data.local.PurchaseDao
import com.wael.astimal.pos.features.client_management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.client_management.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class PurchaseRepositoryImpl(
    private val purchaseDao: PurchaseDao,
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
            if (items.isEmpty()) {
                return Result.failure(IllegalArgumentException("A purchase must have at least one item."))
            }
            val insertedId = purchaseDao.insertPurchaseWithItems(purchase, items)

            // To return the full domain object, we would need a getPurchaseById method in the DAO
            // For now, we can acknowledge success. The UI will update from the main flow.
            // Or, construct a temporary domain object if absolutely needed.
            Result.success(purchase.toDomainPlaceholder()) // Use a simplified mapper for now
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
            if (items.isEmpty()) {
                return Result.failure(IllegalArgumentException("An updated purchase must have at least one item."))
            }

            val entityToUpdate = purchase.copy(
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )

            purchaseDao.updatePurchaseWithItems(entityToUpdate, items)

            Result.success(entityToUpdate.toDomainPlaceholder()) // Return a simplified domain object
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePurchase(purchaseLocalId: Long): Result<Unit> {
        return runCatching {
            val purchaseEntity = purchaseDao.getPurchaseEntityByLocalId(purchaseLocalId)
                ?: return Result.failure(NoSuchElementException("Purchase not found with localId: $purchaseLocalId"))

            val purchaseToMarkAsDeleted = purchaseEntity.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            purchaseDao.insertOrUpdatePurchase(purchaseToMarkAsDeleted)
            Result.success(Unit)
        }
    }
}

