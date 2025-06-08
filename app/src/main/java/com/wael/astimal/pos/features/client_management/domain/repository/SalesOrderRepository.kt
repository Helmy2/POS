package com.wael.astimal.pos.features.client_management.domain.repository

import com.wael.astimal.pos.features.client_management.data.entity.OrderEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.client_management.domain.entity.SalesOrder
import kotlinx.coroutines.flow.Flow

interface SalesOrderRepository {
    fun getOrders(query: String): Flow<List<SalesOrder>>
    fun getOrderDetailsFlow(orderLocalId: Long): Flow<SalesOrder?>
    suspend fun addOrder(order: OrderEntity, items: List<OrderProductEntity>): Result<SalesOrder>
    suspend fun updateOrder(order: OrderEntity, items: List<OrderProductEntity>): Result<SalesOrder>
    suspend fun deleteOrder(orderLocalId: Long): Result<Unit>
}
