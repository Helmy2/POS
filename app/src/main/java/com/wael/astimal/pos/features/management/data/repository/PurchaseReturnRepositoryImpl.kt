package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PurchaseReturnDao
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PurchaseReturnRepositoryImpl(
    private val purchaseReturnDao: PurchaseReturnDao,
) : PurchaseReturnRepository {

    override fun getPurchaseReturns(): Flow<List<PurchaseReturn>> {
        return purchaseReturnDao.getAllPurchaseReturnsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getPurchaseReturnDetails(localId: Long): PurchaseReturn? {
         val detailsEntity = purchaseReturnDao.getPurchaseReturnWithDetails(localId)
         return detailsEntity?.toDomain()
        return null
    }


    override suspend fun addPurchaseReturn(
        purchaseReturn: PurchaseReturnEntity,
        items: List<PurchaseReturnProductEntity>
    ): Result<PurchaseReturn> {
        return try {
            val insertedId = purchaseReturnDao.insertPurchaseReturnWithItems(purchaseReturn, items)
            // To return the full domain object, we would need a getPurchaseReturnById method in the DAO
            // Returning a simplified success object for now. The UI will update from the flow.
            Result.success(purchaseReturn.toDomainPlaceholder())
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
            val entityToUpdate = purchaseReturn.copy(
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            purchaseReturnDao.updatePurchaseReturnWithItems(entityToUpdate, items)
            Result.success(entityToUpdate.toDomainPlaceholder())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePurchaseReturn(localId: Long): Result<Unit> {
        return try {
            val purchaseReturnEntity = purchaseReturnDao.getPurchaseReturnEntityByLocalId(localId)
                ?: return Result.failure(NoSuchElementException("Purchase Return not found with localId: $localId"))

            val purchaseReturnToMarkAsDeleted = purchaseReturnEntity.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            purchaseReturnDao.insertOrUpdatePurchaseReturn(purchaseReturnToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}


private fun PurchaseReturnEntity.toDomainPlaceholder(): PurchaseReturn {
    return PurchaseReturn(
        localId = this.localId,
        serverId = this.serverId,
        invoiceNumber = this.invoiceNumber,
        supplier = null,
        employee = null,
        totalPrice = this.totalPrice,
        paymentType = this.paymentType,
        returnDate = this.returnDate,
        items = emptyList(),
        isSynced = this.isSynced,
        lastModified = this.lastModified,
        isDeletedLocally = this.isDeletedLocally
    )
}
