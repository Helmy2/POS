package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    fun getPurchases(): Flow<List<PurchaseOrder>>
    suspend fun getPurchaseDetails(localId: Long): PurchaseOrder?
    suspend fun addPurchase(purchase: PurchaseOrder): Result<PurchaseOrder>
    suspend fun updatePurchase(purchase: PurchaseOrder): Result<PurchaseOrder>
    suspend fun deletePurchase(purchaseLocalId: Long): Result<Unit>
}
