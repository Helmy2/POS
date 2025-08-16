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
import com.wael.astimal.pos.features.reports.presentation.client_debit.ClientDebitRoute
import com.wael.astimal.pos.features.reports.presentation.current_stock.CurrentStockRoute
import com.wael.astimal.pos.features.reports.presentation.customer_statement.CustomerStatementRoute
import com.wael.astimal.pos.features.reports.presentation.employee_ledger.EmployeeLedgerRoute
import com.wael.astimal.pos.features.reports.presentation.employee_report.EmployeeReportRoute
import com.wael.astimal.pos.features.reports.presentation.product_movement.ProductMovementRoute
import com.wael.astimal.pos.features.reports.presentation.reports.ReportsRoute
import com.wael.astimal.pos.features.reports.presentation.stock_transfer.StockTransferReportRoute
import com.wael.astimal.pos.features.user.presentation.employee.EmployeeRoute
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
            composable<Destination.Employee> {
                EmployeeRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.Categories> {
                CategoryRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.Products> {
                ProductRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.Stores> {
                StoreRoute(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable<Destination.Units> {
                UnitRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.StockManagement> {
                StockManagementRoute(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable<Destination.StockTransfer> {

                StockTransferRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                    openSearch = it.toRoute<Destination.StockTransfer>().openSearch
                )
            }


            // --- Management Sub-Screens ---
            composable<Destination.BusinessPartners> {
                val isOpenNew = it.toRoute<Destination.BusinessPartners>().openNew
                BusinessPartnerRoute(isOpenNew = isOpenNew)
            }
            composable<Destination.SalesOrders> {
                val invoiceId = it.toRoute<Destination.SalesOrders>().invoiceId
                SalesRoute(
                    onNavigateToCreateBusinessPartner = {
                        navController.navigate(Destination.BusinessPartners(true))
                    }, invoiceId = invoiceId
                )
            }
            composable<Destination.SalesReturns> {
                val invoiceId = it.toRoute<Destination.SalesReturns>().invoiceId
                SalesReturnRoute(invoiceId = invoiceId)
            }
            composable<Destination.PurchaseOrders> {
                val invoiceId = it.toRoute<Destination.PurchaseOrders>().invoiceId

                PurchaseRoute(invoiceId = invoiceId)
            }
            composable<Destination.PurchaseReturns> {
                val invoiceId = it.toRoute<Destination.PurchaseReturns>().invoiceId
                PurchaseReturnRoute(invoiceId = invoiceId)
            }
            composable<Destination.EmployeeAccounts> { EmployeeAccountRoute() }
            composable<Destination.Vouchers> { ReceivePayVoucherRoute() }

            // --- Reports Sub-Screens ---
            composable<Destination.CustomerStatement> {
                CustomerStatementRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.EmployeeReport> {
                EmployeeReportRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.EmployeeLedger> {
                EmployeeLedgerRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            composable<Destination.ProductMovement> {
                ProductMovementRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            composable<Destination.CurrentStock> {
                CurrentStockRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.ClientDebit> {
                ClientDebitRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Destination.StockTransferReport> {
                StockTransferReportRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
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
