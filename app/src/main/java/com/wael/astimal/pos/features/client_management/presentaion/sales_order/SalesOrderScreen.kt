package com.wael.astimal.pos.features.client_management.presentaion.sales_order

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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.client_management.domain.entity.PaymentType
import com.wael.astimal.pos.features.inventory.presentation.components.CustomExposedDropdownMenu
import com.wael.astimal.pos.features.inventory.presentation.components.ItemGrid
import com.wael.astimal.pos.features.inventory.presentation.components.SearchScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SalesOrderRoute(
    viewModel: SalesOrderViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SalesOrderScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesOrderScreen(
    state: OrderScreenState,
    onEvent: (OrderScreenEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(OrderScreenEvent.ClearSnackbar)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isDetailViewOpen && state.selectedOrder == null,
        onQueryChange = { onEvent(OrderScreenEvent.UpdateQuery(it)) },
        onSearch = { onEvent(OrderScreenEvent.SearchOrders(it)) },
        onSearchActiveChange = { onEvent(OrderScreenEvent.UpdateIsQueryActive(it)) },
        onBack = {
            if (state.isDetailViewOpen) {
                onEvent(OrderScreenEvent.CloseOrderForm)
            } else {
                onBack()
            }
        },
        onDelete = {
            state.selectedOrder?.let {
                onEvent(
                    OrderScreenEvent.DeleteOrder(it.localId)
                )
            }
        },
        onCreate = { onEvent(OrderScreenEvent.SaveOrder) },
        onUpdate = { onEvent(OrderScreenEvent.SaveOrder) },
        onNew = { onEvent(OrderScreenEvent.OpenNewOrderForm) },
        searchResults = {
            ItemGrid(
                list = state.orders,
                onItemClick = {
                    onEvent(OrderScreenEvent.SelectOrderToView(it))
                },
                labelProvider = { "${it.invoiceNumber}: ${it.clientName}" },
                isSelected = { product -> product.localId == state.selectedOrder?.localId },
            )
        },
        mainContent = {
            OrderForm(
                state = state, onEvent = onEvent
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderForm(
    modifier: Modifier = Modifier, state: OrderScreenState, onEvent: (OrderScreenEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val orderInput = state.currentOrderInput
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        CustomExposedDropdownMenu(
            label = stringResource(R.string.client),
            items = state.availableClients,
            selectedItemId = orderInput.selectedClient?.localId,
            onItemSelected = { onEvent(OrderScreenEvent.SelectClient(it)) },
            itemToDisplayString = { it.clientName.displayName(currentLanguage) },
            itemToId = { it.localId })
        CustomExposedDropdownMenu(
            label = "Main Employee",
            items = state.availableEmployees, // TODO: string res
            selectedItemId = orderInput.selectedMainEmployeeId,
            onItemSelected = { onEvent(OrderScreenEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
        )

        // --- Items Section ---
        Text(stringResource(R.string.items), style = MaterialTheme.typography.titleMedium)
        orderInput.items.forEach { item ->
            OrderItemRow(
                item = item, state = state, onEvent = onEvent
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
        }
        Button(
            onClick = { onEvent(OrderScreenEvent.AddItemToOrder) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_item))
        }

        CustomExposedDropdownMenu(
            label = stringResource(R.string.payment_type),
            items = PaymentType.entries,
            selectedItemId = orderInput.paymentType.ordinal.toLong(),
            onItemSelected = {
                onEvent(
                    OrderScreenEvent.UpdatePaymentType(
                        it ?: PaymentType.CASH
                    )
                )
            },
            itemToDisplayString = { it.name },
            itemToId = { it.ordinal.toLong() })

        OutlinedTextField(
            value = orderInput.amountPaid,
            onValueChange = { onEvent(OrderScreenEvent.UpdateAmountPaid(it)) },
            label = { Text(stringResource(R.string.amount_paid)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OrderTotalsSection(orderInput)

        Button(
            onClick = { onEvent(OrderScreenEvent.SaveOrder) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.save_order))
        }
    }
}

@Composable
fun OrderItemRow(
    item: EditableOrderItem, state: OrderScreenState, onEvent: (OrderScreenEvent) -> Unit
) {
    val language = LocalAppLocale.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.product),
                    items = state.availableProducts,
                    selectedItemId = item.product?.localId,
                    onItemSelected = {
                        onEvent(
                            OrderScreenEvent.UpdateItemProduct(
                                item.tempEditorId, it
                            )
                        )
                    },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.localId })
            }
            IconButton(onClick = { onEvent(OrderScreenEvent.RemoveItemFromOrder(item.tempEditorId)) }) { // TODO: string res
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
                    OrderScreenEvent.UpdateItemUnit(
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
                        OrderScreenEvent.UpdateItemQuantity(
                            item.tempEditorId, it
                        )
                    )
                },
                label = { Text(stringResource(R.string.qty)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = item.sellingPrice,
                onValueChange = {
                    onEvent(
                        OrderScreenEvent.UpdateItemPrice(
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
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.total)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun OrderTotalsSection(orderInput: EditableOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.subtotal), style = MaterialTheme.typography.bodyLarge)
                Text("%.2f".format(orderInput.subtotal), style = MaterialTheme.typography.bodyLarge)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.previous_debt), style = MaterialTheme.typography.bodyLarge)
                Text(
                    "%.2f".format(orderInput.selectedClient?.debt ?: 0.0),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.total_amount), style = MaterialTheme.typography.titleMedium)
                Text(
                    "%.2f".format(orderInput.totalAmount),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.paid), style = MaterialTheme.typography.titleMedium)
                Text(
                    "%.2f".format(orderInput.amountPaid.toDoubleOrNull() ?: 0.0),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.remaining), style = MaterialTheme.typography.titleLarge)
                Text(
                    "%.2f".format(orderInput.amountRemaining),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
