package com.wael.astimal.pos.features.inventory.presentation.inventory

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InventoryRoute(
    viewModel: InventoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InventoryScreen(
        state = state,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun InventoryScreen(
    state: InventoryContract.State,
    onEvent: (InventoryContract.Event) -> Unit,
) {
    Screen(
        modifier = Modifier.padding(16.dp),
    ) {
        ItemGrid(
            list = state.items,
            onItemClick = { inventoryItem ->
                onEvent(InventoryContract.Event.ItemClicked(inventoryItem.destination))
            },
            label = { inventoryItem ->
                Label(text = stringResource(inventoryItem.label))
            },
            modifier = Modifier.fillMaxSize(),
            isSelected = { false }
        )
    }
}
