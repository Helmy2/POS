package com.wael.astimal.pos.features.management.presentation.management

import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination

class ManagementReducer : Reducer<ManagementContract.State, ManagementContract.Event, Nothing> {
    override fun reduce(
        previousState: ManagementContract.State,
        event: ManagementContract.Event
    ): Pair<ManagementContract.State, Nothing?> {
        return when (event) {
            is ManagementContract.Event.LoadManagementItems -> {
                val managementItems = listOf(
                    ManagementContract.ManagementItem(
                        Destination.BusinessPartners,
                        R.string.business_partner
                    ),
                    ManagementContract.ManagementItem(
                        Destination.EmployeeAccounts,
                        R.string.employee_account
                    ),
                    ManagementContract.ManagementItem(
                        Destination.SalesOrders,
                        R.string.sales
                    ),
                    ManagementContract.ManagementItem(
                        Destination.SalesReturns,
                        R.string.sales_return
                    ),
                    ManagementContract.ManagementItem(
                        Destination.PurchaseOrders,
                        R.string.purchase
                    ),
                    ManagementContract.ManagementItem(
                        Destination.PurchaseReturns,
                        R.string.purchase_return
                    ),
                    ManagementContract.ManagementItem(
                        Destination.Vouchers,
                        R.string.receive_pay_voucher
                    )
                )
                previousState.copy(items = managementItems) to null
            }

            is ManagementContract.Event.ItemClicked -> previousState to null
        }
    }
}
