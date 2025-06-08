package com.wael.astimal.pos.features.client_management.presentaion.sales_return

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.client_management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.inventory.presentation.components.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SalesReturnRoute(
    viewModel: SalesReturnViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SalesReturnScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    )
}

@Composable
fun SalesReturnScreen(
    state: SalesReturnScreenState,
    onEvent: (SalesReturnScreenEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(SalesReturnScreenEvent.ClearSnackbar)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isDetailViewOpen && state.selectedReturn == null,
        onQueryChange = { onEvent(SalesReturnScreenEvent.UpdateQuery(it)) },
        onSearch = { onEvent(SalesReturnScreenEvent.SearchReturns(it)) },
        onSearchActiveChange = { onEvent(SalesReturnScreenEvent.UpdateIsQueryActive(it)) },
        onBack = {
            if (state.isDetailViewOpen) {
                onEvent(SalesReturnScreenEvent.CloseReturnForm)
            } else {
                onBack()
            }
        },
        onCreate = { onEvent(SalesReturnScreenEvent.SaveReturn) },
        onNew = { onEvent(SalesReturnScreenEvent.OpenNewReturnForm) },
        searchResults = {
            SalesReturnList(
                returns = state.returns,
                onItemClick = { onEvent(SalesReturnScreenEvent.SelectReturnToView(it)) },
                selectedReturnId = state.selectedReturn?.localId
            )
        },
        mainContent = {
            Column {
                Text(
                    stringResource(state.error ?: R.string.something_went_wrong),
                )
                SalesReturnForm(state = state, onEvent = onEvent)
            }

        },
        onDelete = {},
        onUpdate = {onEvent(SalesReturnScreenEvent.SaveReturn)},
    )
}

@Composable
fun SalesReturnList(
    returns: List<SalesReturn>, onItemClick: (SalesReturn) -> Unit, selectedReturnId: Long?
) {
    val language = LocalAppLocale.current
    LazyColumn {
        items(returns, key = { it.localId }) { item ->
            ListItem(
                headlineContent = { Text("Return from: ${item.clientName?.displayName(language) ?: "N/A"}") },
                supportingContent = { Text("Value: ${item.totalReturnedValue}") },
                modifier = Modifier
                    .clickable { onItemClick(item) }
                    .background(if (item.localId == selectedReturnId) MaterialTheme.colorScheme.inversePrimary else Color.Transparent))
        }
    }
}

@Composable
fun SalesReturnForm(
    state: SalesReturnScreenState, onEvent: (SalesReturnScreenEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val returnInput = state.newReturnInput
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomExposedDropdownMenu(
            label = stringResource(R.string.client),
            items = state.availableClients,
            selectedItemId = returnInput.selectedClient?.localId,
            onItemSelected = { onEvent(SalesReturnScreenEvent.SelectClient(it)) },
            itemToDisplayString = { it.clientName.displayName(currentLanguage) },
            itemToId = { it.localId })

        Text("Returned Items", style = MaterialTheme.typography.titleMedium)
        returnInput.items.forEach { item ->
            ReturnItemRow(
                item = item, state = state, onEvent = onEvent
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        }
        Button(
            onClick = { onEvent(SalesReturnScreenEvent.AddItemToReturn) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_item))
        }

        OutlinedTextField(
            value = returnInput.amountRefunded,
            onValueChange = { onEvent(SalesReturnScreenEvent.UpdateAmountRefunded(it)) },
            label = { Text(stringResource(R.string.amount_refunded)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        SalesReturnTotalsSection(
            totalReturnValue = returnInput.totalReturnValue,
            previousDebt = returnInput.selectedClient?.debt ?: 0.0,
            newDebt = returnInput.newDebt
        )

        Button(
            onClick = { onEvent(SalesReturnScreenEvent.SaveReturn) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.save_order))
        }
    }
}

@Composable
fun ReturnItemRow(
    item: EditableReturnItem,
    state: SalesReturnScreenState,
    onEvent: (SalesReturnScreenEvent) -> Unit
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
                            SalesReturnScreenEvent.UpdateItemProduct(
                                item.tempEditorId, product
                            )
                        )
                    },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.localId })
            }
            IconButton(onClick = { onEvent(SalesReturnScreenEvent.RemoveItemFromReturn(item.tempEditorId)) }) {
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
                    SalesReturnScreenEvent.UpdateItemUnit(
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
                        SalesReturnScreenEvent.UpdateItemQuantity(
                            item.tempEditorId, it
                        )
                    )
                },
                label = { Text(stringResource(R.string.qty)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = item.priceAtReturn,
                onValueChange = {
                    onEvent(
                        SalesReturnScreenEvent.UpdateItemPrice(
                            item.tempEditorId, it
                        )
                    )
                },
                label = { Text(stringResource(R.string.price)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = "%.2f".format(item.lineTotal),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.total)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SalesReturnTotalsSection(
    totalReturnValue: Double, previousDebt: Double, newDebt: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Return Value:", style = MaterialTheme.typography.bodyLarge)
                Text("%.2f".format(totalReturnValue), style = MaterialTheme.typography.bodyLarge)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Previous Debt:", style = MaterialTheme.typography.bodyLarge)
                Text("%.2f".format(previousDebt), style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("New Debt:", style = MaterialTheme.typography.titleMedium)
                Text("%.2f".format(newDebt), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
