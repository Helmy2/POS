package com.wael.astimal.pos.features.inventory.presentation.stock_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.StoreStock
import org.koin.androidx.compose.koinViewModel

@Composable
fun StockManagementRoute(
    onBack : () -> Unit,
    viewModel: StockManagementViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.error, state.snackbarMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.onEvent(StockManagementEvent.ClearError)
        }
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.onEvent(StockManagementEvent.ClearSnackbar)
        }
    }

    StockManagementScreen(onBack=onBack,state = state, onEvent = viewModel::onEvent)
}

@Composable
fun StockManagementScreen(
    state: StockManagementState,
    onEvent: (StockManagementEvent) -> Unit,
    onBack: () -> Unit
) {
    val language = LocalAppLocale.current
    if (state.showAdjustmentDialog) {
        StockAdjustmentDialog(state = state, onEvent = onEvent)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onEvent(StockManagementEvent.SearchStock(it)) },
                    label = { Text(stringResource(R.string.search_products)) },
                    modifier = Modifier.fillMaxWidth()
                )
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.filter_by_store),
                    items = state.stores,
                    selectedItemId = state.selectedStore?.localId,
                    onItemSelected = { onEvent(StockManagementEvent.FilterByStore(it)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.localId },
                )
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.stocks, key = { "${it.store.localId}-${it.product.localId}" }) { stockItem ->
                    StockItemCard(stockItem = stockItem, onEvent = onEvent)
                }
            }
        }
    }
}

@Composable
fun StockItemCard(stockItem: StoreStock, onEvent: (StockManagementEvent) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stockItem.product.localizedName.displayName(LocalAppLocale.current),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.store) + ": ${stockItem.store.name.displayName(LocalAppLocale.current)}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.current_quantity) + ": ${stockItem.quantity}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = { onEvent(StockManagementEvent.ShowAdjustmentDialog(stockItem)) }) {
                    Text(stringResource(R.string.adjust))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentDialog(state: StockManagementState, onEvent: (StockManagementEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(StockManagementEvent.DismissAdjustmentDialog) },
        title = { Text(stringResource(R.string.adjust_stock)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = state.adjustmentTarget?.product?.localizedName?.displayName(LocalAppLocale.current) ?: ""
                )
                OutlinedTextField(
                    value = state.adjustmentQuantityChange,
                    onValueChange = { onEvent(StockManagementEvent.UpdateAdjustmentQuantity(it)) },
                    label = { Text(stringResource(R.string.quantity_change_by)) },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = state.adjustmentReason.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.reason)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        StockAdjustmentReason.entries.forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(text = stringResource(reason.getStringResource())) },
                                onClick = {
                                    onEvent(StockManagementEvent.UpdateAdjustmentReason(reason))
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.adjustmentNotes,
                    onValueChange = { onEvent(StockManagementEvent.UpdateAdjustmentNotes(it)) },
                    label = { Text(stringResource(R.string.notes_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onEvent(StockManagementEvent.SaveStockAdjustment) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(StockManagementEvent.DismissAdjustmentDialog) }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
