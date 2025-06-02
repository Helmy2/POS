package com.wael.astimal.pos.core.presentation.navigation

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.wael.astimal.pos.core.domain.navigation.Destination

@Composable
fun AppNavHost(
    startDestination: Destination,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.systemBarsPadding(),
    ) {
        navigation<Destination.Main>(
            startDestination = Destination.Main.Dashboard
        ) {
            composable<Destination.Main.Dashboard> {
                Text("Dashboard")
            }
            composable<Destination.Main.Inventory> {
                Text("Inventory")
            }
            composable<Destination.Main.Settings> {
                Text("Settings")
            }
        }

        navigation<Destination.Auth>(
            startDestination = Destination.Auth.Login
        ) {
            composable<Destination.Auth.Login> {
                Text("Login")
            }
        }
    }
}

