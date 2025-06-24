package com.wael.astimal.pos.features.management.presentation.management

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManagementRoute(
    viewModel: ManagementViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ManagementScreen(
        state = state,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun ManagementScreen(
    state: ManagementContract.State,
    onEvent: (ManagementContract.Event) -> Unit,
) {
    Screen(
        modifier = Modifier.padding(16.dp),
    ) {
        ItemGrid(
            list = state.items,
            onItemClick = { managementItem ->
                onEvent(ManagementContract.Event.ItemClicked(managementItem.destination))
            },
            label = { managementItem ->
                Label(stringResource(id = managementItem.label))
            },
            modifier = Modifier.fillMaxSize(), isSelected = { false },
        )
    }
}
