package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.displayName
import com.wael.astimal.pos.core.presentation.compoenents.AppButton
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferStatus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.add_item
import pos.app.generated.resources.approved
import pos.app.generated.resources.from_store
import pos.app.generated.resources.in_stock_with_args
import pos.app.generated.resources.pending
import pos.app.generated.resources.product
import pos.app.generated.resources.rejected
import pos.app.generated.resources.remove_item
import pos.app.generated.resources.to_store
import pos.app.generated.resources.unit
import pos.app.generated.resources.you_have_pending_transfer

@Composable
fun StockTransferRoute(
    modifier: Modifier = Modifier,
    viewModel: StockTransferViewModel = koinViewModel(),
    openSearch: Boolean,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()


    LaunchedEffect(key1 = openSearch) {
        if (openSearch) {
            viewModel.processEvent(StockTransferReducer.Event.SearchActiveChanged(true))
        }
    }

    StockTransferScreen(
        onBack = onBack,
        state = state, onEvent = viewModel::processEvent, modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StockTransferScreen(
    state: StockTransferReducer.State,
    onEvent: (StockTransferReducer.Event) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(StockTransferReducer.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(StockTransferReducer.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(StockTransferReducer.Event.SearchActiveChanged(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedTransfer?.updatedAt,
        onDelete = { onEvent(StockTransferReducer.Event.DeleteClicked) },
        onCreate = { onEvent(StockTransferReducer.Event.SaveClicked) },
        onUpdate = { onEvent(StockTransferReducer.Event.SaveClicked) },
        onNew = { onEvent(StockTransferReducer.Event.NewTransferClicked) },
        enableFab = state.enabledFab,
        canUpdate = state.canUpdate,
        canCreate = state.canCreate,
        canDelete = state.canDelete,
        searchResults = {
            ItemGrid(
                list = state.transfers,
                onItemClick = { transfer ->
                    onEvent(
                        StockTransferReducer.Event.TransferSelected(
                            transfer
                        )
                    )
                },
                label = {
                    val fromStoreName = it.fromStore.name.displayName(language)
                    val toStoreName = it.toStore.name.displayName(language)
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Label("$fromStoreName -> $toStoreName")
                        if (it.status == StockTransferStatus.PENDING && it.receivingUser == state.currentUser) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pending,
                                    contentDescription = stringResource(Res.string.pending),
                                )
                                Text(
                                    text = stringResource(Res.string.pending),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                isSelected = { transfer -> transfer.id == state.selectedTransfer?.id },
            )
        },
        mainContent = {
            if (state.havePendingTransfer) {
                Text(
                    text = stringResource(Res.string.you_have_pending_transfer),
                    modifier = Modifier.padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            onEvent(StockTransferReducer.Event.SearchActiveChanged(true))
                        },
                    color = MaterialTheme.colorScheme.error
                )
            }
            state.selectedTransfer?.status?.getStringResourceId()?.let {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(it))
                    Row(
                        modifier = Modifier
                            .width(320.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppButton(
                            onClick = { onEvent(StockTransferReducer.Event.ApprovedClicked) },
                            modifier = Modifier
                                .weight(1f),
                            enabled = state.canUpdateStatus
                        ) {
                            Text(stringResource(Res.string.approved))
                        }
                        AppButton(
                            onClick = { onEvent(StockTransferReducer.Event.RejectedClicked) },
                            modifier = Modifier
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            enabled = state.canUpdateStatus
                        ) {
                            Text(stringResource(Res.string.rejected))
                        }
                    }
                }
            }
            DataPicker(
                selectedDateMillis = state.currentTransferInput.transferDate,
                onDateSelected = { onEvent(StockTransferReducer.Event.DateChanged(it)) },
                enabled = state.canEdit,
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.from_store),
                options = state.dropdownData.formStores.map { it.name.displayName(language) },
                initialText = state.currentTransferInput.fromStore?.name.displayName(language),
                onItemSelected = {
                    onEvent(StockTransferReducer.Event.FromStoreChanged(it?.let {
                        state.dropdownData.formStores.getOrNull(it)
                    }))
                },
                enabled = state.canEdit,
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.to_store),
                options = state.dropdownData.toStores.map { it.name.displayName(language) },
                initialText = state.currentTransferInput.toStore?.name.displayName(language),
                onItemSelected = {
                    onEvent(
                        StockTransferReducer.Event.ToStoreChanged(it?.let {
                            state.dropdownData.toStores.getOrNull(
                                it
                            )
                        })
                    )
                },
                enabled = state.canEdit,
            )

            state.currentTransferInput.items.forEach { item ->
                StockTransferItem(
                    item = item,
                    availableProducts = state.dropdownData.products,
                    onEvent = onEvent,
                    onRemoveItem = { onEvent(StockTransferReducer.Event.RemoveItem(item.editorId)) },
                    enabled = state.canEdit,
                )
            }

            AppButton(
                onClick = { onEvent(StockTransferReducer.Event.AddItem) },
                enabled = state.canEdit,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_item)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(Res.string.add_item))
            }
        },
    )
}


@Composable
fun StockTransferItem(
    item: StockTransferReducer.EditableStockTransferItem,
    availableProducts: List<Product>,
    onEvent: (StockTransferReducer.Event) -> Unit,
    onRemoveItem: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val language = LocalAppLocale.current
    val product = item.product

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExposedDropdownMenu(
            initialText = product?.name.displayName(language),
            label = stringResource(Res.string.product),
            options = availableProducts.map { it.name.displayName(language) },
            onItemSelected = {
                onEvent(
                    StockTransferReducer.Event.ItemProductChanged(
                        item.editorId, it?.let { availableProducts.getOrNull(it) }
                    )
                )
            },
            enabled = enabled,
        )

        if (product != null) {
            AnimatedVisibility(visible = product.subProductUnit != null) {
                ExposedDropdownMenu(
                    label = stringResource(Res.string.unit),
                    options = listOfNotNull(
                        product.mainProductUnit,
                        product.subProductUnit
                    ).map { it.name.displayName(language) },
                    initialText = if (item.isSelectedUnitMax) product.mainProductUnit.name.displayName(
                        language
                    ) else product.subProductUnit?.name.displayName(language),
                    onItemSelected = {
                        onEvent(
                            StockTransferReducer.Event.ItemUnitChanged(item.editorId, it == 0)
                        )
                    },
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
                    if (item.isSelectedUnitMax) product.mainProductUnit.name.displayName(language)
                    else product.subProductUnit?.name?.displayName(language)
                }",
                style = MaterialTheme.typography.bodySmall,
            )


            LabeledTextField(
                value = item.maxUnitQuantity,
                onValueChange = {
                    onEvent(
                        StockTransferReducer.Event.ItemMaxQuantityChanged(
                            item.editorId, it
                        )
                    )
                },
                label = product.mainProductUnit.name.displayName(language),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = enabled && (product.subProductUnit == null || item.isSelectedUnitMax)
            )

            AnimatedVisibility(
                visible = product.subProductUnit != null,
            ) {
                LabeledTextField(
                    value = item.minUnitQuantity,
                    onValueChange = {
                        onEvent(
                            StockTransferReducer.Event.ItemMinQuantityChanged(
                                item.editorId, it
                            )
                        )
                    },
                    label = product.subProductUnit!!.name.displayName(language),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = enabled && !item.isSelectedUnitMax
                )
            }
        }
        IconButton(
            onClick = onRemoveItem,
            enabled = enabled,
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(Res.string.remove_item),
                modifier = Modifier.size(
                    OutlinedTextFieldDefaults.MinHeight / 1.2f
                )
            )
        }
    }
}