package com.wael.astimal.pos.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.dashboard.presentation.DashboardRoute
import com.wael.astimal.pos.features.inventory.presentation.inventory.InventoryRoute
import com.wael.astimal.pos.features.management.presentation.management.ManagementRoute
import com.wael.astimal.pos.features.user.presentation.login.LoginRoute
import com.wael.astimal.pos.features.user.presentation.setting.SettingsRoute

@Composable
fun AppNavHost(
    startDestination: Destination,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        navigation<Destination.Main>(
            startDestination = Destination.Main.Dashboard
        ) {
            composable<Destination.Main.Dashboard> {
                DashboardRoute()
            }
            composable<Destination.Main.Inventory> {
                InventoryRoute(navController = navController)
            }
            composable<Destination.Main.Settings> {
                SettingsRoute(navController = navController)
            }
            composable<Destination.Main.Management> {
                ManagementRoute(navController = navController)
            }
        }

        navigation<Destination.Auth>(
            startDestination = Destination.Auth.Login
        ) {
            composable<Destination.Auth.Login> {
                LoginRoute(navController = navController)
            }
        }
    }
}

