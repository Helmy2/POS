package com.wael.astimal.pos.features.inventory.presentation.inventory

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import org.koin.androidx.compose.koinViewModel

@Composable
fun InventoryRoute(
    onNavigate: (Destination) -> Unit,
    viewModel: InventoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InventoryScreen(
        state = state,
        onNavigate = onNavigate,
    )
}

@Composable
fun InventoryScreen(
    state: InventoryState,
    onNavigate: (Destination) -> Unit,
) {
    Scaffold { paddingValues ->
        ItemGrid(
            list = state.items,
            onItemClick = { inventoryItem ->
                onNavigate(inventoryItem.destination)
            },
            label = { inventoryItem ->
                Text(
                    text = stringResource(id = inventoryItem.label),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            },
            modifier = Modifier
                .padding(8.dp)
                .consumeWindowInsets(paddingValues),
            isSelected = { false }
        )
    }
}