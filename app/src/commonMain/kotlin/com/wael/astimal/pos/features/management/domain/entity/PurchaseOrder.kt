package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseOrder(
    val invoiceNumber: String,
    val supplier: BusinessPartner,
    val user: User,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
    val data: Long,
    val items: List<PurchaseOrderItem>,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val isSynced: Boolean,
) : Item

data class PurchaseOrderItem(
    val id: Id,
    val product: Product,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)

fun PurchaseOrder.matchesQuery(query: String): Boolean {
    return invoiceNumber.contains(
        query, ignoreCase = true
    ) || supplier.name.contains(query) || user.name.contains(query) || items.any {
        it.product.name.contains(
            query
        )
    }
}

fun PurchaseOrder.toEntity(): Pair<PurchaseEntity, List<PurchaseProductEntity>> {
    return PurchaseEntity(
        localId = id.local,
        serverId = id.server,
        invoiceNumber = invoiceNumber,
        employeeLocalId = user.id.local,
        amountPaid = amountPaid,
        amountRemaining = amountRemaining,
        totalAmount = totalAmount,
        paymentType = paymentType,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        businessPartnerLocalId = supplier.id.local
    ) to items.map { item ->
        PurchaseProductEntity(
            localId = item.id.local,
            serverId = item.id.server,
            quantity = item.quantity,
            purchasePrice = item.purchasePrice,
            itemTotalPrice = item.itemTotalPrice,
            productLocalId = item.product.id.local,
            purchaseLocalId = id.local
        )
    }
}
