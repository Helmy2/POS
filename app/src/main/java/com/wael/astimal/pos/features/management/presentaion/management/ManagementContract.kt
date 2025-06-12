package com.wael.astimal.pos.features.management.presentaion.management

sealed interface ManagementEvent {
    data class UpdateSelectDestination(val destination: ManagementDestination) : ManagementEvent
}

data class ManagementState(
    val selectedDestination: ManagementDestination? = null,
)

enum class ManagementDestination {
    ClientInfo, SalesOrder, OrderReturn, SupplierInfo, PurchaseOrder, PurchaseReturn, EmployeeAccount;

    companion object {
        fun getAll(): List<ManagementDestination> = listOf(
            ClientInfo,
            SalesOrder,
            OrderReturn,
            SupplierInfo,
            PurchaseOrder,
            PurchaseReturn,
            EmployeeAccount
        )
    }
}