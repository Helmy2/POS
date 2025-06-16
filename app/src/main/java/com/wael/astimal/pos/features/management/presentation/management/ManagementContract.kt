package com.wael.astimal.pos.features.management.presentation.management

import androidx.annotation.StringRes
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.navigation.Destination

data class ManagementItem(val destination: Destination, @StringRes val label: Int)

data class ManagementState(
    val items: List<ManagementItem> = emptyList()
)

object ManagementDestinations {
    fun getAll(): List<ManagementItem> = listOf(
        ManagementItem(Destination.BusinessPartners, R.string.business_partner),
        ManagementItem(Destination.EmployeeAccounts, R.string.employee_account),
        ManagementItem(Destination.SalesOrders, R.string.sales_order),
        ManagementItem(Destination.SalesReturns, R.string.order_return),
        ManagementItem(Destination.PurchaseOrders, R.string.purchase_order),
        ManagementItem(Destination.PurchaseReturns, R.string.purchase_return),
        ManagementItem(Destination.Vouchers, R.string.receive_pay_voucher)
    )
}