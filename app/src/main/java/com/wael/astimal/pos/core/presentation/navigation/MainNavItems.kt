package com.wael.astimal.pos.core.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.domain.navigation.TopLevelRoutes

fun NavigationSuiteScope.mainNavigationItems(
    onDestinationSelected: (destinations: Destination) -> Unit,
    navBackStackEntry: NavBackStackEntry?
) {
    TopLevelRoutes.routes.forEach { topLevelRoute ->
        val isSelected =
            navBackStackEntry?.destination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true
        item(
            icon = {
                Icon(
                    topLevelRoute.icon,
                    contentDescription = stringResource(topLevelRoute.name),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            },
            label = {
                Text(
                    text = stringResource(topLevelRoute.name),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            },
            selected = isSelected,
            onClick = {
                onDestinationSelected(topLevelRoute.route)
            }
        )
    }
}
