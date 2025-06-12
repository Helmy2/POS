package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.user.domain.entity.User

data class SalesOrder(
    val localId: Long,
    val serverId: Int?,
    val invoiceNumber: String?,
    val client: Client?,
    val employee: User?,
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
    val product: Product?,
    val productUnit: ProductUnit?,
    val quantity: Double,
    val unitSellingPrice: Double,
    val itemTotalPrice: Double,
    val itemGain: Double
)

enum class PaymentType {
    CASH, TRANSFER, WALLET, DEFERRED;

    fun stringResource(type: PaymentType = this): Int {
        return when (type) {
            CASH -> R.string.payment_type_cash
            TRANSFER -> R.string.payment_type_transfer
            WALLET -> R.string.payment_type_wallet
            DEFERRED -> R.string.payment_type_deferred
        }
    }


}

