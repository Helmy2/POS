package com.wael.astimal.pos.features.client_management.presentaion.client

sealed interface ClientEvent {
    data class UpdateSelectDestination(val destination: ClientDestination) : ClientEvent
}

data class ClientState(
    val selectedDestination: ClientDestination? = null,
)

enum class ClientDestination {
    ClientInfo, SalesOrder, OrderReturn, SupplierInfo, PurchaseOrder,PurchaseReturn;

    companion object {
        fun getAll(): List<ClientDestination> =
            listOf(ClientInfo,SalesOrder,OrderReturn,SupplierInfo,PurchaseOrder,PurchaseReturn)
    }
}