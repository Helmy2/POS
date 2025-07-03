package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * A data class to represent a single action within the Multi-Action FAB.
 *
 * @param icon The icon to display for the action.
 * @param label The text label for the action.
 * @param onClick The lambda to be executed when the action is clicked.
 */
data class FabAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

/**
 * A "Speed Dial" Floating Action Button that, when clicked, reveals a list of secondary actions.
 *
 * @param modifier The modifier to be applied to the component.
 * @param actions A list of [FabAction]s to be displayed when the FAB is expanded.
 * @param enabled Whether the androidMain FAB can be interacted with.
 */
@Composable
fun MultiActionFab(
    modifier: Modifier = Modifier,
    actions: List<FabAction>,
    enabled: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Secondary, expanding actions
        AnimatedVisibility(
            visible = isExpanded,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                actions.forEach { action ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = action.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                action.onClick()
                                isExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(imageVector = action.icon, contentDescription = action.label)
                        }
                    }
                }
            }
        }

        if (actions.size == 1) {
            FloatingActionButton(
                onClick = {
                    if (enabled) actions.first().onClick()
                },
                containerColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = actions.first().icon,
                    contentDescription = actions.first().label
                )
            }
        } else {
            FloatingActionButton(
                onClick = { if (enabled) isExpanded = !isExpanded },
                containerColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Actions",
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        }
    }
}
