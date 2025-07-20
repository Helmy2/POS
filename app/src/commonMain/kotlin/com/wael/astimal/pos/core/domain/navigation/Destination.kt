package com.wael.astimal.pos.core.domain.navigation

import kotlinx.serialization.Serializable

/**
 * A type-safe and serializable representation of all navigation destinations in the app.
 * This sealed class hierarchy defines the entire navigation graph.
 */
@Serializable
sealed class Destination {
    // Auth Graph
    @Serializable
    data object Auth : Destination()

    @Serializable
    data object Login : Destination()

    // Main Graph
    @Serializable
    data object Main : Destination()

    @Serializable
    data object Dashboard : Destination()

    // Inventory Feature Graph
    @Serializable
    data object Inventory : Destination() // The androidMain landing/hub screen for inventory

    @Serializable
    data object Categories : Destination()

    @Serializable
    data object Products : Destination()

    @Serializable
    data object Stores : Destination()

    @Serializable
    data object Units : Destination()

    @Serializable
    data object StockManagement : Destination()

    @Serializable
    data class StockTransfer(val openSearch: Boolean = false) : Destination()


    // Management Feature Graph
    @Serializable
    data object Management : Destination() // The androidMain landing/hub screen for management

    @Serializable
    data object BusinessPartners : Destination()

    @Serializable
    data class SalesOrders(val invoiceId: String?) : Destination()

    @Serializable
    data class SalesReturns(val invoiceId: String?) : Destination()

    @Serializable
    data class PurchaseOrders(val invoiceId: String?) : Destination()

    @Serializable
    data class PurchaseReturns(val invoiceId: String?) : Destination()

    @Serializable
    data object EmployeeAccounts : Destination()

    @Serializable
    data object Vouchers : Destination()

    // Reports Feature Graph
    @Serializable
    data object Reports : Destination()

    @Serializable
    data object AccountStatement : Destination()

    @Serializable
    data object CustomerStatement : Destination()

    @Serializable
    data object EmployeeReport : Destination()

    @Serializable
    data object CreateEmployee : Destination()
}
