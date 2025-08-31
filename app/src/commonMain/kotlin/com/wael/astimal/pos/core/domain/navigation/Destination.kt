package com.wael.astimal.pos.core.domain.navigation

import kotlinx.serialization.Serializable

/**
 * A type-safe and serializable representation of all navigation destinations in the app.
 * This sealed class hierarchy defines the entire navigation graph.
 */
@Serializable
sealed class Destination(
    val key: String
) {
    // Auth Graph
    @Serializable
    data object Auth : Destination("Auth")

    @Serializable
    data object Login : Destination("Login")

    // Main Graph
    @Serializable
    data object Main : Destination("Main")

    @Serializable
    data object Dashboard : Destination("Dashboard")

    // Inventory Feature Graph
    @Serializable
    data object Inventory :
        Destination("Inventory") // The androidMain landing/hub screen for inventory

    @Serializable
    data object Categories : Destination("Categories")

    @Serializable
    data object Products : Destination("Products")

    @Serializable
    data object Stores : Destination("Stores")

    @Serializable
    data object Units : Destination("Units")

    @Serializable
    data object StockManagement : Destination("StockManagement")

    @Serializable
    data class StockTransfer(val openSearch: Boolean = false) : Destination("StockTransfer")


    // Management Feature Graph
    @Serializable
    data object Management :
        Destination("Management") // The androidMain landing/hub screen for management

    @Serializable
    data class BusinessPartners(val openNew: Boolean = false) : Destination("BusinessPartners")

    @Serializable
    data class SalesOrders(val invoiceId: String? = null) : Destination("SalesOrders")

    @Serializable
    data class SalesReturns(val invoiceId: String? = null) : Destination("SalesReturns")

    @Serializable
    data class PurchaseOrders(val invoiceId: String? = null) : Destination("PurchaseOrders")

    @Serializable
    data class PurchaseReturns(val invoiceId: String? = null) : Destination("PurchaseReturns")

    @Serializable
    data object EmployeeAccounts : Destination("EmployeeAccounts")

    @Serializable
    data object Vouchers : Destination("Vouchers")

    // Reports Feature Graph
    @Serializable
    data object Reports : Destination("Reports") // The androidMain landing/hub screen for reports

    @Serializable
    data object CustomerStatement : Destination("CustomerStatement")

    @Serializable
    data object EmployeeReport : Destination("EmployeeReport")

    @Serializable
    data object EmployeeLedger : Destination("EmployeeLedger")

    @Serializable
    data object ProductMovement : Destination("ProductMovement")

    @Serializable
    data object CurrentStock : Destination("CurrentStock")

    @Serializable
    data object ClientDebit : Destination("ClientDebit")

    @Serializable
    data object StockTransferReport : Destination("StockTransferReport")

    @Serializable
    data object Employee : Destination("Employee")
}
