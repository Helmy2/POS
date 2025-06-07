package com.wael.astimal.pos.features.client_management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString

data class SalesOrder(
    val localId: Long,
    val serverId: Int?,
    val invoiceNumber: String?,
    val clientLocalId: Long,
    val clientName: LocalizedString,
    val employeeLocalId: Long,
    val employeeName: LocalizedString,
    val mainEmployeeLocalId: Long?,
    val mainEmployeeName: LocalizedString?,
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
    val productLocalId: Long,
    val productName: LocalizedString,
    val unitLocalId: Long,
    val unitName: LocalizedString,
    val quantity: Double,
    val unitSellingPrice: Double,
    val itemTotalPrice: Double,
    val itemGain: Double
)

enum class PaymentType { CASH, TRANSFER, WALLET, DEFERRED }

