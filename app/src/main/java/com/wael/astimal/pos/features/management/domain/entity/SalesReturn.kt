package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.user.domain.entity.User


data class SalesReturn(
    val invoiceNumber: String,
    val client: BusinessPartner,
    val employee: User,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
    val data: Long,
    val items: List<SalesReturnItem>,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val isSynced: Boolean,
) : Item

data class SalesReturnItem(
    val id: Id,
    val returnLocalId: Long,
    val product: Product,
    val quantity: Double,
    val priceAtReturn: Double,
    val itemTotalValue: Double,
)