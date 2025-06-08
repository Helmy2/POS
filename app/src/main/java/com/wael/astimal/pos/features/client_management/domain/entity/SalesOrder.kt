package com.wael.astimal.pos.features.client_management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Unit
import com.wael.astimal.pos.features.user.domain.entity.User

data class SalesOrder(
    val localId: Long,
    val serverId: Int?,
    val invoiceNumber: String?,
    val client: Client?,
    val employee: User?,
    val mainEmployee: User?,
    val previousClientDebt: Double?,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalPrice: Double,
    val totalGain: Double,
    val paymentType: PaymentType,
    val orderDate: Long,
    val items: List<SalesOrderItem>,
    var isSynced: Boolean,
    var lastModified: Long,
    var isDeletedLocally: Boolean
)

data class SalesOrderItem(
    val localId: Long,
    val serverId: Int?,
    val orderLocalId: Long,
    val product:Product?,
    val unit: Unit?,
    val quantity: Double,
    val unitSellingPrice: Double,
    val itemTotalPrice: Double,
    val itemGain: Double
)

enum class PaymentType { CASH, TRANSFER, WALLET, DEFERRED }

