package com.wael.astimal.pos.features.management.presentation.management

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.user.domain.PermissionManager
import pos.app.generated.resources.Res
import pos.app.generated.resources.business_partner
import pos.app.generated.resources.employee_account
import pos.app.generated.resources.purchase
import pos.app.generated.resources.purchase_return
import pos.app.generated.resources.receive_pay_voucher
import pos.app.generated.resources.sales
import pos.app.generated.resources.sales_return

class ManagementReducer : Reducer<ManagementContract.State, ManagementContract.Event, Nothing> {
    override fun reduce(
        previousState: ManagementContract.State, event: ManagementContract.Event
    ): Pair<ManagementContract.State, Nothing?> {
        return when (event) {
            is ManagementContract.Event.LoadManagementItems -> {
                val managementItems = buildList {
                    if (PermissionManager.canView(Destination.EmployeeAccounts)) add(
                        ManagementContract.ManagementItem(
                            Destination.EmployeeAccounts, Res.string.employee_account
                        )
                    )
                    if (PermissionManager.canView(Destination.BusinessPartners())) add(
                        ManagementContract.ManagementItem(
                            Destination.BusinessPartners(), Res.string.business_partner
                        )
                    )
                    if (PermissionManager.canView(Destination.Vouchers)) add(
                        ManagementContract.ManagementItem(
                            Destination.Vouchers, Res.string.receive_pay_voucher
                        )
                    )
                    if (PermissionManager.canView(Destination.SalesOrders(null))) add(
                        ManagementContract.ManagementItem(
                            Destination.SalesOrders(null), Res.string.sales
                        )
                    )
                    if (PermissionManager.canView(Destination.PurchaseOrders(null))) add(
                        ManagementContract.ManagementItem(
                            Destination.PurchaseOrders(null), Res.string.purchase
                        )
                    )
                    if (PermissionManager.canView(Destination.SalesReturns(null))) add(
                        ManagementContract.ManagementItem(
                            Destination.SalesReturns(null), Res.string.sales_return
                        )
                    )
                    if (PermissionManager.canView(Destination.PurchaseReturns(null))) add(
                        ManagementContract.ManagementItem(
                            Destination.PurchaseReturns(null), Res.string.purchase_return
                        )
                    )
                }
                previousState.copy(items = managementItems) to null
            }

            is ManagementContract.Event.ItemClicked -> previousState to null
        }
    }
}
