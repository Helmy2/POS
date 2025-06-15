package com.wael.astimal.pos.features.management.presentation.management

sealed interface ManagementEvent {
    data class UpdateSelectDestination(val destination: ManagementDestination) : ManagementEvent
}

data class ManagementState(
    val selectedDestination: ManagementDestination? = null,
)

enum class ManagementDestination {
    BusinessPartner, SalesOrder, OrderReturn, PurchaseOrder, PurchaseReturn, EmployeeAccount, ReceivePayVoucher;

    companion object {
        fun getAll(): List<ManagementDestination> = listOf(
            BusinessPartner,
            SalesOrder,
            OrderReturn,
            PurchaseOrder,
            PurchaseReturn,
            EmployeeAccount,
            ReceivePayVoucher,
        )
    }
}