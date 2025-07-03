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
    data object StockTransfer : Destination()


    // Management Feature Graph
    @Serializable
    data object Management : Destination() // The androidMain landing/hub screen for management

    @Serializable
    data object BusinessPartners : Destination()

    @Serializable
    data object SalesOrders : Destination()

    @Serializable
    data object SalesReturns : Destination()

    @Serializable
    data object PurchaseOrders : Destination()

    @Serializable
    data object PurchaseReturns : Destination()

    @Serializable
    data object EmployeeAccounts : Destination()

    @Serializable
    data object Vouchers : Destination()

    // Reports Feature Graph
    @Serializable
    data object Reports : Destination() // The androidMain landing/hub screen for reports

    @Serializable
    data object AccountStatement : Destination()
}
