package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.presentation.components.ItemGrid
import com.wael.astimal.pos.features.inventory.presentation.components.LabeledTextField
import com.wael.astimal.pos.features.inventory.presentation.components.SearchScreen
import com.wael.astimal.pos.features.inventory.presentation.product.CustomExposedDropdownMenu
import com.wael.astimal.pos.features.inventory.presentation.product.ProductEvent
import org.koin.androidx.compose.koinViewModel

@Composable
fun StockTransferRoute(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    viewModel: StockTransferViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StockTransferScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StockTransferScreen(
    state: StockTransferScreenState,
    onEvent: (StockTransferScreenEvent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(StockTransferScreenEvent.ClearSnackbar)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isDetailViewOpen && state.selectedTransfer == null,
        onQueryChange = { onEvent(StockTransferScreenEvent.SearchTransfers(it)) },
        onSearch = { onEvent(StockTransferScreenEvent.SearchTransfers(it)) },
        onSearchActiveChange = { onEvent(StockTransferScreenEvent.UpdateIsQueryActive(it)) },        onBack = {
            if (state.isDetailViewOpen) {
                onEvent(StockTransferScreenEvent.CloseTransferForm)
            } else {
                onBack()
            }
        },
        onDelete = {
            state.selectedTransfer?.let {
                onEvent(
                    StockTransferScreenEvent.DeleteTransfer(
                        it.localId
                    )
                )
            }
        },
        onCreate = { onEvent(StockTransferScreenEvent.SaveTransfer) }, // Save handles create
        onUpdate = { onEvent(StockTransferScreenEvent.SaveTransfer) }, // Save handles update (status for now)
        onNew = { onEvent(StockTransferScreenEvent.OpenNewTransferForm) },
        searchResults = {
            ItemGrid(
                list = state.transfers,
                onItemClick = { transfer ->
                    onEvent(StockTransferScreenEvent.AcceptTransfer(transfer.localId))
                    onEvent(StockTransferScreenEvent.SelectTransferToView(transfer))
                },
                labelProvider = { "${it.localId}: ${it.transferDate}" },
                isSelected = { product -> product.localId == state.selectedTransfer?.localId },
            )
        },
        mainContent = {
            StockTransferForm(
                editableTransfer = state.currentTransferInput,
                availableStores = state.availableStores,
                availableProducts = state.availableProducts,
                availableUnits = state.availableUnits,
                onEvent = onEvent,
                isExistingTransfer = state.selectedTransfer != null
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferForm(
    editableTransfer: EditableStockTransfer,
    availableStores: List<Store>,
    availableProducts: List<Product>,
    availableUnits: List<UnitEntity>,
    onEvent: (StockTransferScreenEvent) -> Unit,
    isExistingTransfer: Boolean // To disable editing certain fields for existing transfers
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (isExistingTransfer) "Transfer Details" else "New Stock Transfer",
            style = MaterialTheme.typography.headlineSmall
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.from_store),
            items = availableStores,
            selectedItemId = editableTransfer.fromStoreId,
            onItemSelected = { store -> onEvent(StockTransferScreenEvent.UpdateFromStore(store?.localId)) },
            itemToDisplayString = { "${it.enName}: ${it.arName}" },
            itemToId = { it.localId },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.to_store),
            items = availableStores.filter { it.localId != editableTransfer.fromStoreId },
            selectedItemId = editableTransfer.toStoreId,
            onItemSelected = { store -> onEvent(StockTransferScreenEvent.UpdateToStore(store?.localId)) },
            itemToDisplayString = { "${it.enName}: ${it.arName}" },
            itemToId = { it.localId },
        )

        Text("Items", style = MaterialTheme.typography.titleMedium)
        editableTransfer.items.forEach { item ->
            StockTransferItemRow(
                item = item,
                availableProducts = availableProducts,
                availableUnits = availableUnits,
                onEvent = onEvent,
                onRemoveItem = { onEvent(StockTransferScreenEvent.RemoveItemFromTransfer(item.tempEditorId)) },
                enabled = !isExistingTransfer
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        if (!isExistingTransfer) {
            Button(
                onClick = { onEvent(StockTransferScreenEvent.AddItemToTransfer) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add Item")
            }
        }

        if (isExistingTransfer && editableTransfer.isAccepted == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onEvent(StockTransferScreenEvent.AcceptTransfer(editableTransfer.localId)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept Transfer")
                }
                Button(
                    onClick = { onEvent(StockTransferScreenEvent.RejectTransfer(editableTransfer.localId)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reject Transfer")
                }
            }
        } else if (isExistingTransfer) {
            Text("Status: ${editableTransfer.isAccepted?.let { if (it) "Accepted" else "Rejected" } ?: "Pending"}")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferItemRow(
    item: EditableStockTransferItem,
    availableProducts: List<Product>,
    availableUnits: List<UnitEntity>,
    onEvent: (StockTransferScreenEvent) -> Unit,
    onRemoveItem: () -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.product),
                    items = availableProducts,
                    selectedItemId = item.productLocalId,
                    onItemSelected = { product ->
                        onEvent(
                            StockTransferScreenEvent.UpdateItemProduct(
                                item.tempEditorId,
                                product?.localId
                            )
                        )
                    },
                    itemToDisplayString = { "${it.enName}: ${it.arName}" },
                    itemToId = { it.localId },
                )
            }
            if (enabled) {
                IconButton(onClick = onRemoveItem) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Item")
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.unit),
                    items = availableUnits,
                    selectedItemId = item.unitLocalId,
                    onItemSelected = { unit ->
                        onEvent(
                            StockTransferScreenEvent.UpdateItemUnit(
                                item.tempEditorId,
                                unit?.localId
                            )
                        )
                    },
                    itemToDisplayString = { "${it.enName}: ${it.arName}" },
                    itemToId = { it.localId },
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = item.quantity,
                    onValueChange = {
                        onEvent(
                            StockTransferScreenEvent.UpdateItemQuantity(
                                item.tempEditorId,
                                it
                            )
                        )
                    },
                    minLines = 1,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    label = {
                        Text(stringResource(R.string.quantity))
                    },
                )
            }
        }
    }
}