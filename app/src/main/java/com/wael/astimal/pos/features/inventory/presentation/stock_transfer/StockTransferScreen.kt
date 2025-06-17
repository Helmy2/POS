package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun StockTransferRoute(
    onBack: () -> Unit,
    viewModel: StockTransferViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StockTransferScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        eventFlow = viewModel.eventFlow,
    )
}

@Composable
fun StockTransferScreen(
    state: StockTransferScreenState,
    onEvent: (StockTransferScreenEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        eventFlow = eventFlow,
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        canEdit = state.canEdit,
        onQueryChange = { onEvent(StockTransferScreenEvent.SearchTransfers(it)) },
        onSearch = { onEvent(StockTransferScreenEvent.SearchTransfers(it)) },
        onSearchActiveChange = { onEvent(StockTransferScreenEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedTransfer?.updatedAt,
        onDelete = {
            state.selectedTransfer?.let {
                onEvent(StockTransferScreenEvent.DeleteTransfer(it.id.local))
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
                        "${it.fromStore.name.displayName(language)} -> ${
                            it.toStore.name.displayName(language)
                        }"
                    )
                },
                isSelected = { product -> product.id.local == state.selectedTransfer?.id?.local },
            )
        },
        mainContent = {
            StockTransferForm(
                editableTransfer = state.currentTransferInput,
                availableStores = state.availableStores,
                availableProducts = state.availableProducts,
                availableEmployees = state.availableEmployees,
                onEvent = onEvent,
                canChangeFromStore = state.canEdit,
                canEditEmployee = state.canEdit,
                canEditTheRest = state.canEdit
            )
        },
    )
}


@Composable
fun StockTransferForm(
    editableTransfer: EditableStockTransfer,
    availableStores: List<Store>,
    availableProducts: List<Product>,
    availableEmployees: List<User>,
    onEvent: (StockTransferScreenEvent) -> Unit,
    canEditEmployee: Boolean,
    canEditTheRest: Boolean,
    canChangeFromStore: Boolean
) {
    val localAppLocale = LocalAppLocale.current
    FlowRow(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        DataPicker(
            selectedDateMillis = editableTransfer.transferDate,
            onDateSelected = { onEvent(StockTransferScreenEvent.UpdateTransferDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.from_store),
            items = availableStores,
            selectedItemId = editableTransfer.fromStore?.id?.local,
            onItemSelected = { store -> onEvent(StockTransferScreenEvent.UpdateFromStore(store)) },
            itemToDisplayString = { it.name.displayName(localAppLocale) },
            itemToId = { it.id.local },
            enabled = canChangeFromStore,
            canClearSelection = false,
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.to_store),
            items = availableStores.filter { it.id.local != editableTransfer.fromStore?.id?.local },
            selectedItemId = editableTransfer.toStore?.id?.local,
            onItemSelected = { store -> onEvent(StockTransferScreenEvent.UpdateToStore(store)) },
            itemToDisplayString = { it.name.displayName(localAppLocale) },
            itemToId = { it.id.local },
            enabled = canEditTheRest,
            canClearSelection = false,
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = availableEmployees,
            selectedItemId = editableTransfer.selectedEmployeeId,
            onItemSelected = { onEvent(StockTransferScreenEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(localAppLocale) },
            itemToId = { it.id },
            enabled = canEditEmployee,
            canClearSelection = false,
        )

        editableTransfer.items.forEach { item ->
            StockTransferItemRow(
                item = item,
                availableProducts = availableProducts,
                onEvent = onEvent,
                onRemoveItem = { onEvent(StockTransferScreenEvent.RemoveItemFromTransfer(item.tempEditorId)) },
                enabled = canEditTheRest,
            )
        }

        Button(
            onClick = { onEvent(StockTransferScreenEvent.AddItemToTransfer) },
            enabled = canEditTheRest
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_item))
        }
    }
}


@Composable
fun StockTransferItemRow(
    item: EditableStockTransferItem,
    availableProducts: List<Product>,
    onEvent: (StockTransferScreenEvent) -> Unit,
    onRemoveItem: () -> Unit,
    enabled: Boolean = true,
) {
    val language = LocalAppLocale.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.product),
                    items = availableProducts,
                    selectedItemId = item.product?.id?.local,
                    onItemSelected = { product ->
                        onEvent(
                            StockTransferScreenEvent.UpdateItemProduct(
                                item.tempEditorId, product
                            )
                        )
                    },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.id.local },
                    enabled = enabled,
                    canClearSelection = false,
                )
            }
            IconButton(onClick = onRemoveItem, enabled = enabled) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.remove_item)
                )
            }
        }

        Text(
            text = "${stringResource(R.string.in_stock)}: ${item.currentStock}",
            style = MaterialTheme.typography.bodySmall
        )

        AnimatedVisibility(item.product?.minimumProductUnit != null) {
            Column {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.unit),
                    items = listOfNotNull(
                        item.product?.minimumProductUnit, item.product?.maximumProductUnit
                    ),
                    selectedItemId = if (item.isSelectedUnitIsMax) {
                        item.product?.maximumProductUnit?.id?.local
                    } else {
                        item.product?.minimumProductUnit?.id?.local
                    },
                    onItemSelected = { unit ->
                        onEvent(
                            StockTransferScreenEvent.UpdateItemUnit(
                                item.tempEditorId,
                                unit?.id?.local == item.product?.maximumProductUnit?.id?.local
                            )
                        )
                    },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.id.local },
                    enabled = enabled,
                    canClearSelection = false,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Label(
                        item.product?.minimumProductUnit?.localizedName?.displayName(language)
                            ?: "", modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    TextInputField(
                        value = item.minUnitQuantity,
                        onValueChange = {
                            onEvent(
                                StockTransferScreenEvent.UpdateItemMinUnitQuantity(
                                    item.tempEditorId,
                                    it
                                )
                            )
                        },
                        label = stringResource(R.string.qty),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        enabled = !item.isSelectedUnitIsMax && enabled
                    )
                }
            }
        }
        AnimatedVisibility(item.product?.maximumProductUnit != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Label(
                    item.product?.maximumProductUnit?.localizedName?.displayName(language) ?: ""
                )
                TextInputField(
                    value = item.maxUnitQuantity,
                    onValueChange = {
                        onEvent(
                            StockTransferScreenEvent.UpdateItemMaxUnitQuantity(
                                item.tempEditorId,
                                it
                            )
                        )
                    },
                    label = stringResource(R.string.qty),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    enabled = item.isSelectedUnitIsMax && enabled
                )
            }
        }
    }
}
