package com.wael.astimal.pos.core.domain.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.wael.astimal.pos.R


data class TopLevelRoute(@StringRes val name: Int, val route: Destination, val icon: ImageVector)

object TopLevelRoutes {
    val routes = listOf(
        TopLevelRoute(R.string.dashboard, Destination.Dashboard, Icons.Default.Dashboard),
        TopLevelRoute(R.string.inventory, Destination.Inventory, Icons.Default.Inventory),
        TopLevelRoute(R.string.management, Destination.Management, Icons.Default.Analytics),
        TopLevelRoute(R.string.reports, Destination.Reports, Icons.Default.Assessment),
    )
}

fun isTopLevelRoute(destination: NavDestination?): Boolean {
    return TopLevelRoutes.routes.any { destination?.hasRoute(it.route::class) == true }
}