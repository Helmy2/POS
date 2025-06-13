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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.compoenents.TextInputField
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User
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
    val language = LocalAppLocale.current
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage, state.error) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(StockTransferScreenEvent.ClearSnackbar)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(StockTransferScreenEvent.ClearError)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        onQueryChange = { onEvent(StockTransferScreenEvent.SearchTransfers(it)) },
        onSearch = { onEvent(StockTransferScreenEvent.SearchTransfers(it)) },
        onSearchActiveChange = { onEvent(StockTransferScreenEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedTransfer?.lastModified,
        onDelete = {
            state.selectedTransfer?.let {
                onEvent(StockTransferScreenEvent.DeleteTransfer(it.localId))
            }
        },
        onCreate = { onEvent(StockTransferScreenEvent.SaveTransfer) },
        onUpdate = { onEvent(StockTransferScreenEvent.SaveTransfer) },
        onNew = { onEvent(StockTransferScreenEvent.OpenNewTransferForm) },
        searchResults = {
            ItemGrid(
                list = state.transfers,
                onItemClick = { transfer ->
                    onEvent(StockTransferScreenEvent.SelectTransferToView(transfer))
                },
                label = {
                    Label(
                        "${it.fromStore?.name?.displayName(language)} : ${
                            it.toStore?.name?.displayName(language)
                        }"
                    )
                },
                isSelected = { product -> product.localId == state.selectedTransfer?.localId },
            )
        },
        mainContent = {
            StockTransferForm(
                editableTransfer = state.currentTransferInput,
                availableStores = state.availableStores,
                availableProducts = state.availableProducts,
                availableEmployees = state.availableEmployees,
                onEvent = onEvent,
                isNewTransfer = state.isNew.not(),
            )
        },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferForm(
    editableTransfer: EditableStockTransfer,
    availableStores: List<Store>,
    availableProducts: List<Product>,
    availableEmployees: List<User>,
    onEvent: (StockTransferScreenEvent) -> Unit,
    isNewTransfer: Boolean
) {
    val localAppLocale = LocalAppLocale.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (isNewTransfer) stringResource(R.string.new_stock_transfer) else stringResource(R.string.transfer_details),
            style = MaterialTheme.typography.headlineSmall
        )
        DataPicker(
            selectedDateMillis = editableTransfer.transferDate,
            onDateSelected = { onEvent(StockTransferScreenEvent.UpdateTransferDate(it)) })

        CustomExposedDropdownMenu(
            label = stringResource(R.string.from_store),
            items = availableStores,
            selectedItemId = editableTransfer.fromStoreId,
            onItemSelected = { store -> onEvent(StockTransferScreenEvent.UpdateFromStore(store?.localId)) },
            itemToDisplayString = { it.name.displayName(localAppLocale) },
            itemToId = { it.localId },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.to_store),
            items = availableStores.filter { it.localId != editableTransfer.fromStoreId },
            selectedItemId = editableTransfer.toStoreId,
            onItemSelected = { store -> onEvent(StockTransferScreenEvent.UpdateToStore(store?.localId)) },
            itemToDisplayString = { it.name.displayName(localAppLocale) },
            itemToId = { it.localId },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = availableEmployees,
            selectedItemId = editableTransfer.selectedEmployeeId,
            onItemSelected = { onEvent(StockTransferScreenEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(localAppLocale) },
            itemToId = { it.id },
        )

        Text(stringResource(R.string.items), style = MaterialTheme.typography.titleMedium)
        editableTransfer.items.forEach { item ->
            StockTransferItemRow(
                item = item,
                availableProducts = availableProducts,
                onEvent = onEvent,
                onRemoveItem = { onEvent(StockTransferScreenEvent.RemoveItemFromTransfer(item.tempEditorId)) },
            )
        }

        Button(
            onClick = { onEvent(StockTransferScreenEvent.AddItemToTransfer) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_item))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferItemRow(
    item: EditableStockTransferItem,
    availableProducts: List<Product>,
    onEvent: (StockTransferScreenEvent) -> Unit,
    onRemoveItem: () -> Unit,
    enabled: Boolean = true
) {
    val language = LocalAppLocale.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.product),
                    items = availableProducts,
                    selectedItemId = item.product?.localId,
                    onItemSelected = { product ->
                        onEvent(
                            StockTransferScreenEvent.UpdateItemProduct(
                                item.tempEditorId, product
                            )
                        )
                    },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.localId },
                )
            }
            if (enabled) {
                IconButton(onClick = onRemoveItem) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove_item)
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomExposedDropdownMenu(
                label = stringResource(R.string.unit),
                items = listOf(
                    item.product?.minimumProductUnit, item.product?.maximumProductUnit
                ),
                selectedItemId = item.productUnit?.localId ?: item.product?.minimumProductUnit?.localId,
                onItemSelected = { unit ->
                    onEvent(
                        StockTransferScreenEvent.UpdateItemUnit(item.tempEditorId, unit)
                    )
                },
                itemToDisplayString = { it?.localizedName?.displayName(language) ?: "" },
                itemToId = { it?.localId ?: -1L },
                modifier = Modifier.weight(1f)
            )
            TextInputField(
                value = item.quantity,
                onValueChange = {
                    onEvent(
                        StockTransferScreenEvent.UpdateItemQuantity(
                            item.tempEditorId, it
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                ),
                label = stringResource(R.string.quantity),
                modifier = Modifier.weight(1f)
            )
        }
    }
}