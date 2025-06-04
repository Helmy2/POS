package com.wael.astimal.pos.core.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.navigation.AppNavHost
import com.wael.astimal.pos.core.presentation.navigation.mainNavigationItems
import com.wael.astimal.pos.core.util.Connectivity
import org.koin.compose.koinInject


@Composable
fun MainScaffold(
    startDestination: Destination = Destination.Main
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val connectivity: Connectivity = koinInject()
    val state by connectivity.statusUpdates.collectAsStateWithLifecycle(
        Connectivity.Status.Connected(
            connectionType = Connectivity.ConnectionType.Unknown
        )
    )

    var isReconnected by remember { mutableStateOf(false) }
    val noInternetMessage = stringResource(R.string.no_internet)
    val backOnlineMessage = stringResource(R.string.back_online)

    LaunchedEffect(state) {
        if (state.isDisconnected) {
            snackbarHostState.showSnackbar(noInternetMessage)
            isReconnected = true
        }

        if (isReconnected && state.isConnected) {
            snackbarHostState.showSnackbar(backOnlineMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        NavigationSuiteScaffold(
            modifier = Modifier.padding(paddingValues),
            navigationSuiteItems = {
                mainNavigationItems(
                    onDestinationSelected = {
                        navController.apply {
                            navigate(it) {
                                popUpTo(graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    navBackStackEntry = navBackStackEntry
                )
            },
        ) {
            AppNavHost(
                startDestination = startDestination,
                navController = navController,
                snackbarState = snackbarHostState,
            )
        }
    }
}