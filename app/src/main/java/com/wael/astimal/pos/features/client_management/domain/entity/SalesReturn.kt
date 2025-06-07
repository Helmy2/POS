package com.wael.astimal.pos.features.client_management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString


data class SalesReturn(
    val localId: Long,
    val serverId: Int?,
    val invoiceNumber: String?,
    val clientLocalId: Long?,
    val clientName: LocalizedString?,
    val supplierLocalId: Long?,
    val supplierName: String?,
    val employeeLocalId: Long?,
    val employeeName: LocalizedString?,
    val previousDebt: Double?,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalReturnedValue: Double,
    val totalGainLoss: Double,
    val paymentType: PaymentType,
    val returnDate: Long,
    val items: List<SalesReturnItem>,
    var isSynced: Boolean,
    var lastModified: Long,
    var isDeletedLocally: Boolean
)

data class SalesReturnItem(
    val localId: Long,
    val serverId: Int?,
    val returnLocalId: Long,
    val productLocalId: Long,
    val productName: LocalizedString,
    val unitLocalId: Long,
    val unitName: LocalizedString,
    val quantity: Double,
    val priceAtReturn: Double,
    val itemTotalValue: Double,
    val itemGainLoss: Double
)