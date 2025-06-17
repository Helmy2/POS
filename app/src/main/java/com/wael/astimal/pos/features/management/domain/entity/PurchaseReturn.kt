package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseReturn(
    val invoiceNumber: String,
    val supplier: Supplier,
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
    val purchaseReturnLocalId: Long,
    val product: Product,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)