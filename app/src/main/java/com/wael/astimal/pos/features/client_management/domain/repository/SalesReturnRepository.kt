package com.wael.astimal.pos.features.client_management.domain.repository

import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.client_management.domain.entity.SalesReturn
import kotlinx.coroutines.flow.Flow

interface SalesReturnRepository {
    fun getSalesReturns(): Flow<List<SalesReturn>>
    fun getSalesReturnDetailsFlow(returnLocalId: Long): Flow<SalesReturn?>
    suspend fun addSalesReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<Unit>

    suspend fun updateSalesReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<Unit>

    suspend fun deleteSalesReturn(returnLocalId: Long): Result<Unit>
}