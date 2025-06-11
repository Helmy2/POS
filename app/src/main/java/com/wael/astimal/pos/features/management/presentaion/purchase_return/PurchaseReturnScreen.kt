package com.wael.astimal.pos.features.management.presentaion.purchase_return

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.presentation.components.CustomExposedDropdownMenu
import com.wael.astimal.pos.features.inventory.presentation.components.ItemGrid
import com.wael.astimal.pos.features.management.presentaion.purchase.PurchaseScreenEvent
import org.koin.androidx.compose.koinViewModel

@Composable
fun PurchaseReturnRoute(
    viewModel: PurchaseReturnViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PurchaseReturnScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    )
}

@Composable
fun PurchaseReturnScreen(
    state: PurchaseReturnScreenState,
    onEvent: (PurchaseReturnScreenEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage, state.error) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseReturnScreenEvent.ClearSnackbar)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseReturnScreenEvent.ClearError)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        onQueryChange = { onEvent(PurchaseReturnScreenEvent.UpdateQuery(it)) },
        onSearch = { onEvent(PurchaseReturnScreenEvent.SearchReturns(it)) },
        onSearchActiveChange = { onEvent(PurchaseReturnScreenEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        onCreate = { onEvent(PurchaseReturnScreenEvent.SaveReturn) },
        onNew = { onEvent(PurchaseReturnScreenEvent.OpenNewReturnForm) },
        searchResults = {
            ItemGrid(
                list = state.returns,
                onItemClick = { onEvent(PurchaseReturnScreenEvent.SelectReturnToView(it)) },
                label = {
                    Text(
                        "Return to ${it.supplier?.name?.displayName(LocalAppLocale.current)}",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                isSelected = { item -> item.localId == state.selectedReturn?.localId },
            )
        },
        mainContent = {
                PurchaseReturnForm(state = state, onEvent = onEvent)
        },
        onDelete = {
            onEvent(PurchaseReturnScreenEvent.DeleteReturn)
        },
        onUpdate = {
            onEvent(PurchaseReturnScreenEvent.SaveReturn)
        }
    )
}

@Composable
fun PurchaseReturnForm(
    state: PurchaseReturnScreenState,
    onEvent: (PurchaseReturnScreenEvent) -> Unit
) {
    val localAppLocale = LocalAppLocale.current
    val returnInput = state.newReturnInput
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomExposedDropdownMenu(
            label = stringResource(R.string.supplier),
            items = state.availableSuppliers,
            selectedItemId = returnInput.selectedSupplier?.id,
            onItemSelected = { onEvent(PurchaseReturnScreenEvent.SelectSupplier(it)) },
            itemToDisplayString = { it.name.displayName(localAppLocale) },
            itemToId = { it.id }
        )

        CustomExposedDropdownMenu(
            label = "Main Employee",
            items = state.availableEmployees,
            selectedItemId = returnInput.selectedEmployeeId,
            onItemSelected = { onEvent(PurchaseReturnScreenEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(localAppLocale) },
            itemToId = { it.id },
        )

        Text("Items to Return", style = MaterialTheme.typography.titleMedium)
        returnInput.items.forEach { item ->
            PurchaseReturnItemRow(item = item, state = state, onEvent = onEvent)
        }
        Button(
            onClick = { onEvent(PurchaseReturnScreenEvent.AddItemToReturn) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_item))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Return Value:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "%.2f".format(returnInput.totalReturnedValue),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseReturnItemRow(
    item: EditablePurchaseReturnItem,
    state: PurchaseReturnScreenState,
    onEvent: (PurchaseReturnScreenEvent) -> Unit
) {
    val language = LocalAppLocale.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.product),
                    items = state.availableProducts,
                    selectedItemId = item.product?.localId,
                    onItemSelected = { product ->
                        onEvent(
                            PurchaseReturnScreenEvent.UpdateItemProduct(
                                item.tempEditorId,
                                product
                            )
                        )
                    },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.localId }
                )
            }
            IconButton(onClick = { onEvent(PurchaseReturnScreenEvent.RemoveItemFromReturn(item.tempEditorId)) }) {
                Icon(Icons.Default.Delete, "Remove Item")
            }
        }
        CustomExposedDropdownMenu(
            label = stringResource(R.string.unit),
            items = listOf(
                item.product?.minimumUnit, item.product?.maximumUnit
            ),
            selectedItemId = item.selectedUnit?.localId,
            onItemSelected = { unit ->
                onEvent(
                    PurchaseReturnScreenEvent.UpdateItemUnit(
                        item.tempEditorId, unit
                    )
                )
            },
            itemToDisplayString = { it?.localizedName?.displayName(language) ?: "" },
            itemToId = { it?.localId ?: -1L },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = item.quantity,
                onValueChange = {
                    onEvent(
                        PurchaseReturnScreenEvent.UpdateItemQuantity(
                            item.tempEditorId,
                            it
                        )
                    )
                },
                label = { Text(stringResource(R.string.qty)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = item.purchasePrice,
                onValueChange = {
                    onEvent(
                        PurchaseReturnScreenEvent.UpdateItemPrice(
                            item.tempEditorId,
                            it
                        )
                    )
                },
                label = { Text(stringResource(R.string.price)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = "%.2f".format(item.lineTotal),
                onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.total)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}