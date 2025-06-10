package com.wael.astimal.pos.features.client_management.domain.repository

import com.wael.astimal.pos.features.client_management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.client_management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.client_management.domain.entity.PurchaseReturn
import kotlinx.coroutines.flow.Flow

interface PurchaseReturnRepository {
    fun getPurchaseReturns(): Flow<List<PurchaseReturn>>
    suspend fun getPurchaseReturnDetails(localId: Long): PurchaseReturn?
    suspend fun addPurchaseReturn(purchaseReturn: PurchaseReturnEntity, items: List<PurchaseReturnProductEntity>): Result<PurchaseReturn>
    suspend fun updatePurchaseReturn(purchaseReturn: PurchaseReturnEntity, items: List<PurchaseReturnProductEntity>): Result<PurchaseReturn>
    suspend fun deletePurchaseReturn(localId: Long): Result<Unit>
}
