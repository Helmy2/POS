package com.wael.astimal.pos.core.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.domain.navigation.TopLevelRoutes
import com.wael.astimal.pos.core.domain.navigation.isTopLevelRoute
import com.wael.astimal.pos.core.presentation.snackbar.ObserveEffect
import com.wael.astimal.pos.core.presentation.snackbar.SnackbarController
import com.wael.astimal.pos.features.user.presentation.setting.SettingsRoute
import kotlinx.coroutines.launch


@Composable
private fun rememberDelayedDestination(navController: NavController): State<NavBackStackEntry?> {
    val immediateDestination by navController.currentBackStackEntryAsState()
    val delayedDestination = remember { mutableStateOf(immediateDestination) }

    LaunchedEffect(immediateDestination) {
        delayedDestination.value = immediateDestination
    }
    return delayedDestination
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    startDestination: Destination = Destination.Main
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveEffect(SnackbarController.events, snackbarHostState) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            if (it.message.isNotEmpty()) {
                val result = snackbarHostState.showSnackbar(
                    it.message,
                    it.action?.name,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    it.action?.action?.invoke()
                }
            }
        }
    }

    // Use our new delayed state holder instead of the immediate one
    val navBackStackEntry by rememberDelayedDestination(navController)
    val currentDestination = navBackStackEntry?.destination

    // All UI decisions are now based on the DELAYED destination
    val isOnTopLevelRoute = isTopLevelRoute(currentDestination)

    val currentTopLevelRoute = TopLevelRoutes.routes.find {
        currentDestination?.hasRoute(it.route::class) == true
    }
    val topBarTitle = currentTopLevelRoute?.let { stringResource(id = it.name) } ?: ""
    var showSetting by rememberSaveable {
        mutableStateOf(false)
    }
    Scaffold(
        topBar = {
            if (isOnTopLevelRoute) {
                TopAppBar(title = { Text(topBarTitle) }, actions = {
                    IconButton(onClick = { showSetting = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = com.wael.astimal.pos.R.string.settings)
                        )
                    }
                })
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box {
            NavigationSuiteScaffold(
                layoutType = if (isOnTopLevelRoute) {
                    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
                        currentWindowAdaptiveInfo()
                    )
                } else {
                    NavigationSuiteType.None
                },
                modifier = Modifier.padding(paddingValues),
                navigationSuiteItems = {
                    mainNavigationItems(
                        onDestinationSelected = { destination ->
                            navController.navigate(destination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        navBackStackEntry = navBackStackEntry,
                    )
                },
            ) {
                AppNavHost(
                    startDestination = startDestination,
                    navController = navController,
                )
            }

            AnimatedVisibility(showSetting) {
                Dialog(onDismissRequest = { showSetting = false }) {
                    Card {
                        SettingsRoute(navController = navController)
                    }
                }
            }
        }
    }
}