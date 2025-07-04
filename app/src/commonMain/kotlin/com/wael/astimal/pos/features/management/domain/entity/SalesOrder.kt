package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.user.domain.entity.User

data class SalesOrder(
    val invoiceNumber: String,
    val client: BusinessPartner,
    val employee: User,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
    val items: List<SalesOrderItem>,
    val orderDate: Long,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
    override val isSynced: Boolean = false,
) : Item

data class SalesOrderItem(
    val id: Id,
    val product: Product,
    val quantity: Double,
    val unitSellingPrice: Double,
    val itemTotalPrice: Double,
)

fun SalesOrder.toEntity(): Pair<OrderEntity, List<OrderProductEntity>> {
    return OrderEntity(
        localId = id.local,
        serverId = id.server,
        invoiceNumber = invoiceNumber,
        businessPartnerLocalId = client.id.local,
        employeeLocalId = employee.id,
        amountPaid = amountPaid,
        amountRemaining = amountRemaining,
        totalAmount = totalAmount,
        paymentType = paymentType,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        orderDate = orderDate
    ) to items.map { item ->
        OrderProductEntity(
            localId = item.id.local,
            serverId = item.id.server,
            orderLocalId = id.local,
            productLocalId = item.product.id.local,
            quantity = item.quantity,
            unitSellingPrice = item.unitSellingPrice,
            itemTotalPrice = item.itemTotalPrice
        )
    }
}

fun SalesOrder.matchesQuery(query: String): Boolean {
    val lowerQuery = query.lowercase()
    return invoiceNumber.lowercase().contains(lowerQuery) ||
            client.name.contains(lowerQuery) ||
            employee.name.contains(lowerQuery) ||
            items.any { it.product.name.contains(lowerQuery) }
}

