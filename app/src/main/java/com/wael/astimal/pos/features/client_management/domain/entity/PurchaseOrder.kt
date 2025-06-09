package com.wael.astimal.pos.features.client_management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Unit
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseOrder(
    val localId: Long,
    val serverId: Int?,
    val invoiceNumber: String?,
    val supplier: Supplier?,
    val user: User?,
    val totalPrice: Double,
    val paymentType: PaymentType,
    val purchaseDate: Long,
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
    val unit: Unit?,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)
