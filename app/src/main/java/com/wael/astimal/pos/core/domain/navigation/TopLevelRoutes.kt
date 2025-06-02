package com.wael.astimal.pos.core.domain.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.wael.astimal.pos.R


data class TopLevelRoute(@StringRes val name:  Int, val route: Destination, val icon: ImageVector)

object TopLevelRoutes {
    val routes = listOf(
        TopLevelRoute(R.string.dashboard, Destination.Main.Dashboard, Icons.Default.Dashboard),
        TopLevelRoute(R.string.inventory, Destination.Main.Inventory, Icons.Default.Inventory),
        TopLevelRoute(R.string.settings, Destination.Main.Settings, Icons.Default.Settings),
    )
}