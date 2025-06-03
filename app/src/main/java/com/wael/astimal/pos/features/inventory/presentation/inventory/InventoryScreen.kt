package com.wael.astimal.pos.features.inventory.presentation.inventory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.inventory.presentation.components.ItemGridRes
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun InventoryRoute(
    viewModel: InventoryViewModel = koinViewModel(),
    navController: NavHostController,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InventoryScreen(state = state, onEvent = viewModel::handleEvent){
        navController.navigateUp()
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun InventoryScreen(
    state: InventoryState,
    onEvent: (InventoryEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<InventoryDestination>()

    BackHandler {
        if (scaffoldNavigator.canNavigateBack()) {
            scope.launch {
                scaffoldNavigator.navigateBack()
            }
        } else {
            onNavigateBack()
        }
    }

    ListDetailPaneScaffold(
        directive = scaffoldNavigator.scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                ItemGridRes(
                    list = InventoryDestination.getAll(),
                    onItemClick = {
                        onEvent(InventoryEvent.UpdateSelectDestination(it))
                        scope.launch {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                        }
                    },
                    labelProvider = {
                        when (it) {
                            InventoryDestination.UnitOfMeasures -> R.string.unit_of_measures
                            InventoryDestination.Stores -> R.string.stores
                            InventoryDestination.Categories -> R.string.categories
                            InventoryDestination.Products -> R.string.products
                        }
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
                        Text("UnitOfMeasures")
                    }

                    InventoryDestination.Stores -> {
                        Text("Stores")
                    }

                    InventoryDestination.Categories -> {
                        Text("Store")
                    }

                    InventoryDestination.Products -> {
                        Text("Products")
                    }

                    else -> {}
                }
            }
        },
    )
}