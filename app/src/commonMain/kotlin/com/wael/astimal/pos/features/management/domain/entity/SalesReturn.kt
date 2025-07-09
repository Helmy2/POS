package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.data.local.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.local.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.user.domain.entity.User


data class SalesReturn(
    val invoiceNumber: String,
    val client: BusinessPartner,
    val employee: User,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
    val items: List<SalesReturnItem>,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
    override val isSynced: Boolean = false,
) : Item

data class SalesReturnItem(
    val id: Id,
    val product: Product,
    val quantity: Double,
    val priceAtReturn: Double,
    val itemTotalValue: Double,
)

fun SalesReturn.toEntity(): Pair<OrderReturnEntity, List<OrderReturnProductEntity>> {
    return OrderReturnEntity(
        localId = id.local,
        serverId = id.server,
        invoiceNumber = invoiceNumber,
        businessPartnerLocalId = client.id.local,
        employeeLocalId = employee.id.local,
        amountPaid = amountPaid,
        amountRemaining = amountRemaining,
        totalAmount = totalAmount,
        paymentType = paymentType,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    ) to items.map {
        OrderReturnProductEntity(
            localId = it.id.local,
            serverId = it.id.server,
            orderReturnLocalId = id.local,
            productLocalId = it.product.id.local,
            quantity = it.quantity,
            priceAtReturn = it.priceAtReturn,
            itemTotalValue = it.itemTotalValue
        )
    }
}

fun SalesReturn.matchesQuery(query: String): Boolean {
    return invoiceNumber.contains(query, ignoreCase = true) ||
            client.name.contains(query) ||
            employee.name.contains(query) ||
            items.any { it.product.name.contains(query) }
}