package com.wael.astimal.pos.features.inventory.presentation.inventory

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
import com.wael.astimal.pos.features.inventory.presentation.category.CategoryRoute
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.features.inventory.presentation.product.ProductRoute
import com.wael.astimal.pos.features.inventory.presentation.stock_transfer.StockTransferRoute
import com.wael.astimal.pos.features.inventory.presentation.store.StoreRoute
import com.wael.astimal.pos.features.inventory.presentation.unit.UnitRoute
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun InventoryRoute(
    viewModel: InventoryViewModel = koinViewModel(),
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InventoryScreen(
        state = state,
        onEvent = viewModel::handleEvent,
        navController = navController,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun InventoryScreen(
    state: InventoryState,
    onEvent: (InventoryEvent) -> Unit,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<InventoryDestination>()

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
                    list = InventoryDestination.getAll(),
                    onItemClick = {
                        onEvent(InventoryEvent.UpdateSelectDestination(it))
                        scope.launch {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                        }
                    },
                    label = {
                        Text(
                            when (it) {
                                InventoryDestination.UnitOfMeasures -> context.getString(R.string.unit)
                                InventoryDestination.Stores -> context.getString(R.string.stores)
                                InventoryDestination.Categories -> context.getString(R.string.categories)
                                InventoryDestination.Products -> context.getString(R.string.products)
                                InventoryDestination.StockTransfer -> context.getString(R.string.stock_transfer)
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
                    InventoryDestination.UnitOfMeasures -> {
                        UnitRoute(onBack = { scope.launch { scaffoldNavigator.navigateBack() } })
                    }

                    InventoryDestination.Stores -> {
                        StoreRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                        )
                    }

                    InventoryDestination.Categories -> {
                        CategoryRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                        )
                    }

                    InventoryDestination.Products -> {
                        ProductRoute(
                            onBack = { scope.launch { scaffoldNavigator.navigateBack() } },
                        )
                    }

                    InventoryDestination.StockTransfer -> {
                        StockTransferRoute(
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