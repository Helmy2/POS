package com.wael.astimal.pos.core.domain.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.dashboard
import pos.app.generated.resources.inventory
import pos.app.generated.resources.management
import pos.app.generated.resources.reports


data class TopLevelRoute(val name: StringResource, val route: Destination, val icon: ImageVector)

object TopLevelRoutes {
    val routes = listOf(
        TopLevelRoute(Res.string.dashboard, Destination.Dashboard, Icons.Default.Dashboard),
        TopLevelRoute(Res.string.inventory, Destination.Inventory, Icons.Default.Inventory),
        TopLevelRoute(Res.string.management, Destination.Management, Icons.Default.Analytics),
        TopLevelRoute(Res.string.reports, Destination.Reports, Icons.Default.Assessment),
    )
}

fun isTopLevelRoute(destination: NavDestination?): Boolean {
    return TopLevelRoutes.routes.any { destination?.hasRoute(it.route::class) == true }
}