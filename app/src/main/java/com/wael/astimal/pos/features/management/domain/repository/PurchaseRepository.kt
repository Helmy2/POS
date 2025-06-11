package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    fun getPurchases(): Flow<List<PurchaseOrder>>
    suspend fun getPurchaseDetails(localId: Long): PurchaseOrder?
    suspend fun addPurchase(purchase: PurchaseEntity, items: List<PurchaseProductEntity>): Result<PurchaseOrder>
    suspend fun updatePurchase(purchase: PurchaseEntity, items: List<PurchaseProductEntity>): Result<PurchaseOrder>
    suspend fun deletePurchase(purchaseLocalId: Long): Result<Unit>
}
