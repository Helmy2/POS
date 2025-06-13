package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseReturn(
    val localId: Long,
    val serverId: Int?,
    val invoiceNumber: String?,
    val supplier: Supplier?,
    val employee: User?,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
    val data: Long,
    val items: List<PurchaseReturnItem>,
    var isSynced: Boolean,
    var lastModified: Long,
    var isDeletedLocally: Boolean
)

data class PurchaseReturnItem(
    val localId: Long,
    val serverId: Int?,
    val purchaseReturnLocalId: Long,
    val product: Product?,
    val productUnit: ProductUnit?,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)