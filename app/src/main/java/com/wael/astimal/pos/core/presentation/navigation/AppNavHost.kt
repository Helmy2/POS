package com.wael.astimal.pos.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.dashboard.presentation.DashboardRoute
import com.wael.astimal.pos.features.inventory.presentation.category.CategoryRoute
import com.wael.astimal.pos.features.inventory.presentation.inventory.InventoryRoute
import com.wael.astimal.pos.features.inventory.presentation.product.ProductRoute
import com.wael.astimal.pos.features.inventory.presentation.stock_management.StockManagementRoute
import com.wael.astimal.pos.features.inventory.presentation.stock_transfer.StockTransferRoute
import com.wael.astimal.pos.features.inventory.presentation.store.StoreRoute
import com.wael.astimal.pos.features.inventory.presentation.unit.UnitRoute
import com.wael.astimal.pos.features.management.presentation.business_partner.BusinessPartnerRoute
import com.wael.astimal.pos.features.management.presentation.employee_account.EmployeeAccountRoute
import com.wael.astimal.pos.features.management.presentation.management.ManagementRoute
import com.wael.astimal.pos.features.management.presentation.purchase.PurchaseRoute
import com.wael.astimal.pos.features.management.presentation.purchase_return.PurchaseReturnRoute
import com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers.ReceivePayVoucherRoute
import com.wael.astimal.pos.features.management.presentation.sales.SalesRoute
import com.wael.astimal.pos.features.management.presentation.sales_return.SalesReturnRoute
import com.wael.astimal.pos.features.reports.presentation.account_statement.AccountStatementRoute
import com.wael.astimal.pos.features.reports.presentation.reports.ReportsRoute
import com.wael.astimal.pos.features.user.presentation.login.LoginRoute

@Composable
fun AppNavHost(
    startDestination: Destination,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // Main App Graph
        navigation<Destination.Main>(
            startDestination = Destination.Dashboard
        ) {
            // Top-level destinations
            composable<Destination.Dashboard> { DashboardRoute() }

            // Inventory Hub
            composable<Destination.Inventory> {
                InventoryRoute(
                    onNavigate = { navController.navigate(it) }
                )
            }
            // Management Hub
            composable<Destination.Management> {
                ManagementRoute(
                    onNavigate = { navController.navigate(it) }
                )
            }
            // Reports Hub
            composable<Destination.Reports> { ReportsRoute() }


            // --- Inventory Sub-Screens ---
            composable<Destination.Categories> { CategoryRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.Products> { ProductRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.Stores> { StoreRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.Units> { UnitRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.StockManagement> { StockManagementRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.StockTransfer> { StockTransferRoute(onBack = { navController.popBackStack() }) }


            // --- Management Sub-Screens ---
            composable<Destination.BusinessPartners> { BusinessPartnerRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.SalesOrders> { SalesRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.SalesReturns> { SalesReturnRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.PurchaseOrders> { PurchaseRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.PurchaseReturns> { PurchaseReturnRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.EmployeeAccounts> { EmployeeAccountRoute(onBack = { navController.popBackStack() }) }
            composable<Destination.Vouchers> { ReceivePayVoucherRoute(onBack = { navController.popBackStack() }) }

            // --- Reports Sub-Screens ---
            composable<Destination.AccountStatement> { AccountStatementRoute(onBack = { navController.popBackStack() }) }
            // Add other report routes here in the future
        }

        // Auth Graph
        navigation<Destination.Auth>(
            startDestination = Destination.Login
        ) {
            composable<Destination.Login> {
                LoginRoute()
            }
        }
    }
}
