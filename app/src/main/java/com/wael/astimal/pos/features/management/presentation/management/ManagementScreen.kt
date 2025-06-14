package com.wael.astimal.pos.features.management.presentation.management

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.features.management.presentation.client_info.ClientInfoRoute
import com.wael.astimal.pos.features.management.presentation.employee_account.EmployeeAccountRoute
import com.wael.astimal.pos.features.management.presentation.employee_payment.EmployeePaymentRoute
import com.wael.astimal.pos.features.management.presentation.purchase.PurchaseRoute
import com.wael.astimal.pos.features.management.presentation.purchase_return.PurchaseReturnRoute
import com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers.ReceivePayVoucherRoute
import com.wael.astimal.pos.features.management.presentation.sales.SalesRoute
import com.wael.astimal.pos.features.management.presentation.sales_return.SalesReturnRoute
import com.wael.astimal.pos.features.management.presentation.supplier_info.SupplierInfoRoute
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManagementRoute(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: ManagementViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ManagementScreen(
        state = state,
        onEvent = viewModel::handleEvent,
        navController = navController,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun ManagementScreen(
    state: ManagementState,
    onEvent: (ManagementEvent) -> Unit,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<ManagementDestination>()

    BackHandler {
        if (scaffoldNavigator.canNavigateBack()) {
            scope.launch {
                scaffoldNavigator.navigateBack()
            }
        } else {
            navController.navigateUp()
        }
    }

    ListDetailPaneScaffold(
        directive = scaffoldNavigator.scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                ItemGrid(
                    list = ManagementDestination.getAll(),
                    onItemClick = {
                        onEvent(ManagementEvent.UpdateSelectDestination(it))
                        scope.launch {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                        }
                    },
                    label = {
                        Text(
                            when (it) {
                                ManagementDestination.ClientInfo -> context.getString(R.string.client_info)
                                ManagementDestination.SalesOrder -> context.getString(R.string.sales_order)
                                ManagementDestination.OrderReturn -> context.getString(R.string.order_return)
                                ManagementDestination.SupplierInfo -> context.getString(R.string.supplier_info)
                                ManagementDestination.PurchaseOrder -> context.getString(R.string.purchase_order)
                                ManagementDestination.PurchaseReturn -> context.getString(R.string.purchase_return)
                                ManagementDestination.EmployeeAccount -> context.getString(R.string.employee_account)
                                ManagementDestination.ReceivePayVoucher -> context.getString(R.string.receive_pay_voucher)
                                ManagementDestination.EmployeePayment -> context.getString(R.string.employee_payment)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )

                    },
                    isSelected = { it == state.selectedDestination },
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        detailPane = {
            AnimatedPane {
                when (scaffoldNavigator.currentDestination?.contentKey) {
                    ManagementDestination.ClientInfo -> {
                        ClientInfoRoute(onBack = { scope.launch { scaffoldNavigator.navigateBack() } })
                    }

                    ManagementDestination.SalesOrder -> {
                        SalesRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    ManagementDestination.OrderReturn -> {
                        SalesReturnRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    ManagementDestination.SupplierInfo -> {
                        SupplierInfoRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                        )
                    }

                    ManagementDestination.PurchaseOrder -> {
                        PurchaseRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    ManagementDestination.PurchaseReturn -> {
                        PurchaseReturnRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    ManagementDestination.EmployeeAccount -> {
                        EmployeeAccountRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    ManagementDestination.ReceivePayVoucher -> {
                        ReceivePayVoucherRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    ManagementDestination.EmployeePayment -> {
                        EmployeePaymentRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                            snackbarHostState = snackbarHostState,
                        )
                    }


                    else -> {}
                }
            }
        },
    )
}


