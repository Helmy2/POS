package com.wael.astimal.pos.core.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.inventory.presentation.inventory.InventoryRoute
import com.wael.astimal.pos.features.user.presentation.login.LoginRoute
import com.wael.astimal.pos.features.user.presentation.setting.SettingsRoute

@Composable
fun AppNavHost(
    startDestination: Destination,
    navController: NavHostController,
    snackbarState: SnackbarHostState,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        navigation<Destination.Main>(
            startDestination = Destination.Main.Dashboard
        ) {
            composable<Destination.Main.Dashboard> {
                Text("Dashboard")
            }
            composable<Destination.Main.Inventory> {
                InventoryRoute(navController = navController)
            }
            composable<Destination.Main.Settings> {
                SettingsRoute(navController = navController)
            }
        }

        navigation<Destination.Auth>(
            startDestination = Destination.Auth.Login
        ) {
            composable<Destination.Auth.Login> {
                LoginRoute(navController = navController, snackbarState = snackbarState)
            }
        }
    }
}

