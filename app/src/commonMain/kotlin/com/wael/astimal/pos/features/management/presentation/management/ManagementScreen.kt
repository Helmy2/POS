package com.wael.astimal.pos.features.management.presentation.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.features.inventory.presentation.inventory.InventoryReducer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.inventory
import pos.app.generated.resources.management
import pos.app.generated.resources.settings

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    state: ManagementContract.State,
    onEvent: (ManagementContract.Event) -> Unit,
) {
    Screen(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.management)) }, actions = {
                IconButton(onClick = { onEvent(ManagementContract.Event.ItemClicked(Destination.Settings)) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(Res.string.settings)
                    )
                }
            })
        }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(state.items) {
                Card(
                    onClick = {
                        onEvent(ManagementContract.Event.ItemClicked(it.destination))
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(120.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface)
                    )

                    Text(
                        text = stringResource(it.label),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
