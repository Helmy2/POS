package com.wael.astimal.pos.features.management.presentation.management

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import pos.app.generated.resources.Res
import pos.app.generated.resources.business_partner
import pos.app.generated.resources.receive_pay_voucher

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
                        Res.string.business_partner
                    ),
//                    ManagementContract.ManagementItem(
//                        Destination.EmployeeAccounts,
//                        Res.string.employee_account
//                    ),
//                    ManagementContract.ManagementItem(
//                        Destination.SalesOrders,
//                        Res.string.sales
//                    ),
//                    ManagementContract.ManagementItem(
//                        Destination.SalesReturns,
//                        Res.string.sales_return
//                    ),
//                    ManagementContract.ManagementItem(
//                        Destination.PurchaseOrders,
//                        Res.string.purchase
//                    ),
//                    ManagementContract.ManagementItem(
//                        Destination.PurchaseReturns,
//                        Res.string.purchase_return
//                    ),
                    ManagementContract.ManagementItem(
                        Destination.Vouchers,
                        Res.string.receive_pay_voucher
                    )
                )
                previousState.copy(items = managementItems) to null
            }

            is ManagementContract.Event.ItemClicked -> previousState to null
        }
    }
}
