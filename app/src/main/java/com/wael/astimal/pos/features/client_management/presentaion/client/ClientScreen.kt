package com.wael.astimal.pos.features.client_management.presentaion.client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
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
import com.wael.astimal.pos.features.client_management.presentaion.clinet_info.ClientInfoRoute
import com.wael.astimal.pos.features.inventory.presentation.components.ItemGrid
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ClientRoute(
    navController: NavHostController,
    snackbarHostState : SnackbarHostState,
    viewModel: ClientViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ClientScreen(
        state = state,
        onEvent = viewModel::handleEvent,
        navController = navController,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun ClientScreen(
    state: ClientState,
    onEvent: (ClientEvent) -> Unit,
    navController : NavHostController,
    snackbarHostState :SnackbarHostState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<ClientDestination>()

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
                    list = ClientDestination.getAll(),
                    onItemClick = {
                        onEvent(ClientEvent.UpdateSelectDestination(it))
                        scope.launch {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                        }
                    },
                    labelProvider = {
                        when (it) {
                           ClientDestination.ClientInfo -> context.getString(R.string.client_info)
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
                    ClientDestination.ClientInfo -> {
                        ClientInfoRoute(onBack = { scope.launch { scaffoldNavigator.navigateBack() } })
                    }

                    else -> {}
                }
            }
        },
    )
}


