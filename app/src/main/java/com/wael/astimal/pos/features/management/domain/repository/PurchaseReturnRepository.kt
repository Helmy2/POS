package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import kotlinx.coroutines.flow.Flow

interface PurchaseReturnRepository {
    fun getPurchaseReturns(): Flow<List<PurchaseReturn>>
    suspend fun getPurchaseReturnDetails(localId: Long): PurchaseReturn?
    suspend fun addPurchaseReturn(purchaseReturn: PurchaseReturn): Result<PurchaseReturn>
    suspend fun updatePurchaseReturn(purchaseReturn: PurchaseReturn): Result<PurchaseReturn>
    suspend fun deletePurchaseReturn(localId: Long): Result<Unit>
}
