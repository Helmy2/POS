package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseReturn(
    val invoiceNumber: String,
    val supplier: BusinessPartner,
    val employee: User,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
    val data: Long,
    val items: List<PurchaseReturnItem>,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val isSynced: Boolean,
) : Item

data class PurchaseReturnItem(
    val id: Id,
    val product: Product,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)

fun PurchaseReturn.toEntity(): Pair<PurchaseReturnEntity, List<PurchaseReturnProductEntity>> {
    return PurchaseReturnEntity(
        localId = id.local,
        serverId = id.server,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        invoiceNumber = invoiceNumber,
        businessPartnerLocalId = supplier.id.local,
        paymentType = paymentType,
        employeeLocalId = employee.id,
        totalAmount = totalAmount,
        amountRemaining = amountRemaining,
        amountPaid = amountPaid
    ) to items.map {
        PurchaseReturnProductEntity(
            localId = it.id.local,
            serverId = it.id.server,
            purchaseReturnLocalId = id.local,
            productLocalId = it.product.id.local,
            quantity = it.quantity,
            purchasePrice = it.purchasePrice,
            itemTotalPrice = it.itemTotalPrice
        )
    }
}

fun PurchaseReturn.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val queryLower = query.lowercase()
    return supplier.name.contains(queryLower) ||
            invoiceNumber.contains(queryLower, ignoreCase = true) ||
            totalAmount.toString().contains(queryLower)
}