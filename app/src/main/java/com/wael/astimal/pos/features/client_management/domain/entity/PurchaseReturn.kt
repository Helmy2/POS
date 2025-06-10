package com.wael.astimal.pos.features.client_management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Unit
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseReturn(
    val localId: Long,
    val serverId: Int?,
    val invoiceNumber: String?,
    val supplier: Supplier?,
    val employee: User?,
    val totalPrice: Double,
    val paymentType: PaymentType,
    val returnDate: Long,
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
    val unit: Unit?,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)