package com.wael.astimal.pos.features.inventory.presentation.stock_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.core.presentation.compoenents.SearchBarWithBackButton
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import org.koin.androidx.compose.koinViewModel

@Composable
fun StockManagementRoute(viewModel: StockManagementViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StockManagementScreen(
        state = state, onEvent = viewModel::processEvent
    )
}

@Composable
fun StockManagementScreen(
    state: StockManagementContract.State,
    onEvent: (StockManagementContract.Event) -> Unit,
) {
    Screen(
        topBar = {
            SearchBarWithBackButton(
                query = state.query,
                onBack = { onEvent(StockManagementContract.Event.NavigateBack) },
                onQueryChange = { onEvent(StockManagementContract.Event.SearchQueryChanged(it)) },
                onSearch = { onEvent(StockManagementContract.Event.SearchQueryChanged(it)) },
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            if (state.canUserEdit) {
                FloatingActionButton(
                    onClick = { onEvent(StockManagementContract.Event.ShowAdjustmentDialog) },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.adjust_stock),
                    )
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(300.dp),
                    modifier = Modifier.padding(8.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        state.productBundles, key = { "${it.store.id.local}" }) { stockItem ->
                        StockItemCard(
                            productBundle = stockItem, onClick = {
                                onEvent(
                                    StockManagementContract.Event.ShowAdjustmentDialogWithStore(
                                        stockItem.store
                                    )
                                )
                            }, enabled = state.canUserEdit
                        )
                    }
                }
            }

            if (state.showAdjustmentDialog) {
                StockAdjustmentDialog(state = state, onEvent = onEvent)
            }
        }
    }
}


@Composable
fun StockItemCard(
    productBundle: StockManagementContract.ProductBundle, onClick: () -> Unit, enabled: Boolean
) {
    val language = LocalAppLocale.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = productBundle.store.name.displayName(language),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.products),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.weight(1f))
                Card(
                    enabled = enabled,
                    onClick = onClick,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.adjust_stock),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            productBundle.quantities.forEach {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                ) {
                    Text(
                        text = it.product.name.displayName(
                            language
                        ), style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${stringResource(R.string.current_quantity)}: ${it.quantity} ${
                            it.product.maximumProductUnit.name.displayName(
                                language
                            )
                        }", style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun StockAdjustmentDialog(
    state: StockManagementContract.State, onEvent: (StockManagementContract.Event) -> Unit
) {
    val context = LocalContext.current
    val language = LocalAppLocale.current
    AlertDialog(
        onDismissRequest = { onEvent(StockManagementContract.Event.DismissAdjustmentDialog) },
        title = { Text(stringResource(R.string.adjust_stock)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.store),
                    items = state.stores,
                    currentSelection = state.adjustmentStore?.name?.displayName(
                        LocalAppLocale.current
                    ) ?: "",
                    onItemSelected = {
                        onEvent(
                            StockManagementContract.Event.AdjustmentStoreChanged(it)
                        )
                    },
                    displayedItemText = { it.name.displayName(language) },
                    enabled = state.canUserEdit
                )
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.product),
                    items = state.products,
                    currentSelection = state.adjustmentProduct?.name?.displayName(
                        LocalAppLocale.current
                    ) ?: "",
                    onItemSelected = {
                        onEvent(
                            StockManagementContract.Event.AdjustmentProductChanged(it)
                        )
                    },
                    displayedItemText = { it.name.displayName(language) },
                    enabled = state.canUserEdit
                )
                LabeledTextField(
                    value = state.adjustmentQuantityChange,
                    onValueChange = {
                        onEvent(
                            StockManagementContract.Event.AdjustmentQuantityChanged(
                                it
                            )
                        )
                    },
                    label = stringResource(R.string.quantity_change_by),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canUserEdit
                )
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.reason),
                    items = StockAdjustmentReason.entries,
                    currentSelection = stringResource(state.adjustmentReason.getStringResource()),
                    onItemSelected = {
                        onEvent(
                            StockManagementContract.Event.AdjustmentReasonChanged(
                                it
                            )
                        )
                    },
                    displayedItemText = { context.getString(it.getStringResource()) },
                    enabled = state.canUserEdit
                )
                LabeledTextField(
                    value = state.adjustmentNotes,
                    onValueChange = {
                        onEvent(
                            StockManagementContract.Event.AdjustmentNotesChanged(
                                it
                            )
                        )
                    },
                    label = stringResource(R.string.notes),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canUserEdit
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(StockManagementContract.Event.SaveAdjustmentClicked) },
                enabled = state.canUserEdit
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(StockManagementContract.Event.DismissAdjustmentDialog) },
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}