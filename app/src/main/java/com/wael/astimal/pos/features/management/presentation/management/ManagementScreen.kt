package com.wael.astimal.pos.features.management.presentation.management

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
fun ManagementRoute(
    onNavigate: (Destination) -> Unit,
    viewModel: ManagementViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ManagementScreen(
        state = state,
        onNavigate = onNavigate,
    )
}


@Composable
fun ManagementScreen(
    state: ManagementState,
    onNavigate: (Destination) -> Unit,
) {
    Scaffold { paddingValues ->
        ItemGrid(
            list = state.items,
            onItemClick = { managementItem ->
                onNavigate(managementItem.destination)
            },
            label = { managementItem ->
                Text(
                    text = stringResource(id = managementItem.label),
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


