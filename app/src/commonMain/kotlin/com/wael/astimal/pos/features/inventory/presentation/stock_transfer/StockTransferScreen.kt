package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.add_item
import pos.app.generated.resources.from_store
import pos.app.generated.resources.in_stock_with_args
import pos.app.generated.resources.product
import pos.app.generated.resources.remove_item
import pos.app.generated.resources.to_store
import pos.app.generated.resources.unit

@Composable
fun StockTransferRoute(
    modifier: Modifier = Modifier,
    viewModel: StockTransferViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StockTransferScreen(
        state = state, onEvent = viewModel::processEvent, modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferScreen(
    state: StockTransferContract.State,
    onEvent: (StockTransferContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(StockTransferContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(StockTransferContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(StockTransferContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(StockTransferContract.Event.BackClicked) },
        lastModifiedDate = state.selectedTransfer?.updatedAt,
        onDelete = { onEvent(StockTransferContract.Event.DeleteClicked) },
        onCreate = { onEvent(StockTransferContract.Event.SaveClicked) },
        onUpdate = { onEvent(StockTransferContract.Event.SaveClicked) },
        onNew = { onEvent(StockTransferContract.Event.NewTransferClicked) },
        canEdit = state.canUserEdit,
        canSave = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.transfers,
                onItemClick = { transfer ->
                    onEvent(
                        StockTransferContract.Event.TransferSelected(
                            transfer
                        )
                    )
                },
                label = {
                    val fromStoreName = it.fromStore.name.displayName(language)
                    val toStoreName = it.toStore.name.displayName(language)
                    Label("$fromStoreName -> $toStoreName")
                },
                isSelected = { transfer -> transfer.id.local == state.selectedTransfer?.id?.local },
            )
        },
        mainContent = {
            item {
                DataPicker(
                    selectedDateMillis = state.currentTransferInput.transferDate,
                    onDateSelected = { onEvent(StockTransferContract.Event.DateChanged(it)) },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }

            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.from_store),
                    items = state.dropdownData.stores,
                    currentSelection = state.currentTransferInput.fromStore?.name?.displayName(
                        language
                    ) ?: "",
                    onItemSelected = { store ->
                        onEvent(
                            StockTransferContract.Event.FromStoreChanged(
                                store
                            )
                        )
                    },
                    itemToDisplayString = { it.name.displayName(language) },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }

            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.to_store),
                    items = state.dropdownData.stores.filter { it.id.local != state.currentTransferInput.fromStore?.id?.local },
                    currentSelection = state.currentTransferInput.toStore?.name?.displayName(
                        language
                    ) ?: "",
                    onItemSelected = { store ->
                        onEvent(
                            StockTransferContract.Event.ToStoreChanged(
                                store
                            )
                        )
                    },
                    itemToDisplayString = { it.name.displayName(language) },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)

                )
            }

            items(
                state.currentTransferInput.items,
            ) { item ->
                StockTransferItemRow(
                    item = item,
                    availableProducts = state.dropdownData.products,
                    onEvent = onEvent,
                    onRemoveItem = { onEvent(StockTransferContract.Event.RemoveItem(item.editorId)) },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }

            item(
                span = StaggeredGridItemSpan.FullLine
            ) {
                Button(
                    onClick = { onEvent(StockTransferContract.Event.AddItem) },
                    enabled = state.canUserEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(Res.string.add_item)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(Res.string.add_item))
                }
            }
        },
    )
}


@Composable
fun StockTransferItemRow(
    item: StockTransferContract.EditableStockTransferItem,
    availableProducts: List<Product>,
    onEvent: (StockTransferContract.Event) -> Unit,
    onRemoveItem: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val language = LocalAppLocale.current
    val product = item.product

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    currentSelection = product?.name?.displayName(language) ?: "",
                    label = stringResource(Res.string.product),
                    items = availableProducts,
                    onItemSelected = { p ->
                        onEvent(
                            StockTransferContract.Event.ItemProductChanged(
                                item.editorId, p
                            )
                        )
                    },
                    itemToDisplayString = { it.name.displayName(language) },
                    enabled = enabled,
                )
            }
            IconButton(
                onClick = onRemoveItem,
                enabled = enabled,
                modifier = Modifier.padding(vertical = OutlinedTextFieldDefaults.MinHeight / 6)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.remove_item),
                    modifier = Modifier.size(
                        OutlinedTextFieldDefaults.MinHeight / 1.5f
                    )
                )
            }
        }

        if (product != null) {
            // Show unit selection only if a minimum unit exists
            AnimatedVisibility(visible = product.minimumProductUnit != null) {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.unit),
                    items = listOfNotNull(product.maximumProductUnit, product.minimumProductUnit),
                    selectedItemId = if (item.isSelectedUnitMax) product.maximumProductUnit.id.local else product.minimumProductUnit?.id?.local,
                    onItemSelected = { unit ->
                        onEvent(
                            StockTransferContract.Event.ItemUnitChanged(
                                item.editorId, unit.id.local == product.maximumProductUnit.id.local
                            )
                        )
                    },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id.local },
                    enabled = enabled,
                )
            }

            Text(
                text = "${
                    stringResource(
                        Res.string.in_stock_with_args,
                        if (item.isSelectedUnitMax) item.currentMaxStock else item.currentMaxStock * product.subUnitsPerMainUnit
                    )
                } ${
                    if (item.isSelectedUnitMax) product.maximumProductUnit.name.displayName(language)
                    else product.minimumProductUnit?.name?.displayName(language)
                }",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Max Unit Quantity
                LabeledTextField(
                    value = item.maxUnitQuantity,
                    onValueChange = {
                        onEvent(
                            StockTransferContract.Event.ItemMaxQuantityChanged(
                                item.editorId, it
                            )
                        )
                    },
                    label = product.maximumProductUnit.name.displayName(language),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = enabled && (product.minimumProductUnit == null || item.isSelectedUnitMax)
                )

                // Min Unit Quantity (only visible if it exists)
                AnimatedVisibility(
                    visible = product.minimumProductUnit != null, modifier = Modifier.weight(1f)
                ) {
                    LabeledTextField(
                        value = item.minUnitQuantity,
                        onValueChange = {
                            onEvent(
                                StockTransferContract.Event.ItemMinQuantityChanged(
                                    item.editorId, it
                                )
                            )
                        },
                        label = product.minimumProductUnit!!.name.displayName(language),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = enabled && !item.isSelectedUnitMax
                    )
                }
            }
        }
    }
}