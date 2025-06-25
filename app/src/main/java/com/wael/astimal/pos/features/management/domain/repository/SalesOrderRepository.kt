package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import kotlinx.coroutines.flow.Flow

interface SalesOrderRepository {
    fun getOrders(query: String = ""): Flow<List<SalesOrder>>
    fun getOrderDetailsFlow(orderLocalId: Long): Flow<SalesOrder?>
    suspend fun addOrder(order: SalesOrder): Result<SalesOrder>
    suspend fun updateOrder(order: SalesOrder): Result<SalesOrder>
    suspend fun deleteOrder(orderLocalId: Long): Result<Unit>
    fun getDailySales(startDate: Long, endDate: Long): Flow<List<DailySale>>
}