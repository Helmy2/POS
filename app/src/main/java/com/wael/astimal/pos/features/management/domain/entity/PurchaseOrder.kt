package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseOrder(
    val localId: Long,
    val serverId: Int?,
    val invoiceNumber: String?,
    val supplier: Supplier?,
    val user: User?,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
    val data: Long,
    val items: List<PurchaseOrderItem>,
    var isSynced: Boolean,
    var lastModified: Long,
    var isDeletedLocally: Boolean
)

data class PurchaseOrderItem(
    val localId: Long,
    val serverId: Int?,
    val purchaseLocalId: Long,
    val product: Product?,
    val productUnit: ProductUnit?,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)
