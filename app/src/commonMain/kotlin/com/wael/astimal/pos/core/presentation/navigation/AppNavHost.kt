package com.wael.astimal.pos.core.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
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
import com.wael.astimal.pos.features.user.presentation.create.CreateEmployeeRoute
import com.wael.astimal.pos.features.user.presentation.login.LoginRoute

@Composable
fun AppNavHost(
    startDestination: Destination,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        exitTransition = { fadeOut() },
        enterTransition = { fadeIn() },
    ) {
        // Main App Graph
        navigation<Destination.Main>(
            startDestination = Destination.Dashboard
        ) {
            // Top-level destinations
            composable<Destination.Dashboard> {
                DashboardRoute(
                    onNavigateToStockTransfer = {
                        navController.navigate(Destination.StockTransfer(true))
                    },
                )
            }

            // Inventory Hub
            composable<Destination.Inventory> { InventoryRoute() }

            // Management Hub
            composable<Destination.Management> { ManagementRoute() }

            // Reports Hub
            composable<Destination.Reports> { ReportsRoute() }


            // --- Inventory Sub-Screens ---
            composable<Destination.CreateEmployee> {
                CreateEmployeeRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.Categories> { CategoryRoute() }
            composable<Destination.Products> { ProductRoute() }
            composable<Destination.Stores> { StoreRoute() }
            composable<Destination.Units> { UnitRoute() }
            composable<Destination.StockManagement> { StockManagementRoute() }
            composable<Destination.StockTransfer> {

                StockTransferRoute(
                    openSearch = it.toRoute<Destination.StockTransfer>().openSearch
                )
            }


            // --- Management Sub-Screens ---
            composable<Destination.BusinessPartners> { BusinessPartnerRoute() }
            composable<Destination.SalesOrders> { SalesRoute() }
            composable<Destination.SalesReturns> { SalesReturnRoute() }
            composable<Destination.PurchaseOrders> { PurchaseRoute() }
            composable<Destination.PurchaseReturns> { PurchaseReturnRoute() }
            composable<Destination.EmployeeAccounts> { EmployeeAccountRoute() }
            composable<Destination.Vouchers> { ReceivePayVoucherRoute() }

            // --- Reports Sub-Screens ---
            composable<Destination.AccountStatement> { AccountStatementRoute() }
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
