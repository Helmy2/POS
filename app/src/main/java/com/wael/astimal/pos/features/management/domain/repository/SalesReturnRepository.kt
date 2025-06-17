package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import kotlinx.coroutines.flow.Flow

interface SalesReturnRepository {

    fun getReturns(query: String): Flow<List<SalesReturn>>

    fun getReturnDetailsFlow(returnLocalId: Long): Flow<SalesReturn?>

    suspend fun addReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<SalesReturn>

    suspend fun updateReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<SalesReturn>

    suspend fun deleteReturn(returnLocalId: Long): Result<Unit>
}