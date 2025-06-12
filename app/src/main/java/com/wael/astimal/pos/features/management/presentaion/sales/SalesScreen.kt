package com.wael.astimal.pos.features.management.presentaion.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.OrderInputFields
import com.wael.astimal.pos.core.presentation.compoenents.OrderTotalsSection
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SalesRoute(
    viewModel: SalesViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SalesScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    )
}

@Composable
fun SalesScreen(
    state: OrderState,
    onEvent: (OrderEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    val language = LocalAppLocale.current
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage, state.error) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(OrderEvent.ClearSnackbar)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(OrderEvent.ClearError)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        onQueryChange = { onEvent(OrderEvent.UpdateQuery(it)) },
        onSearch = { onEvent(OrderEvent.SearchOrders(it)) },
        onSearchActiveChange = { onEvent(OrderEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedOrder?.lastModified,
        onDelete = {
            state.selectedOrder?.let {
                onEvent(OrderEvent.DeleteOrder(it.localId))
            }
        },
        onCreate = { onEvent(OrderEvent.SaveOrder) },
        onUpdate = { onEvent(OrderEvent.SaveOrder) },
        onNew = { onEvent(OrderEvent.OpenNewOrderForm) },
        searchResults = {
            ItemGrid(
                list = state.orders,
                onItemClick = {
                    onEvent(OrderEvent.SelectOrderToView(it))
                },
                label = {
                    Label(
                        "${it.invoiceNumber}: ${it.client?.name?.displayName(language)}",
                    )
                },
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

@Composable
fun OrderForm(
    modifier: Modifier = Modifier, state: OrderState, onEvent: (OrderEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val orderInput = state.currentOrderInput
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        DataPicker(
            selectedDateMillis = orderInput.date,
            onDateSelected = { onEvent(OrderEvent.UpdateTransferDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.client),
            items = state.availableClients,
            selectedItemId = orderInput.selectedClient?.id,
            onItemSelected = { onEvent(OrderEvent.SelectClient(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.id })

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.availableEmployees,
            selectedItemId = orderInput.selectedEmployeeId,
            onItemSelected = { onEvent(OrderEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
        )

        OrderInputFields(
            itemList = orderInput.items,
            selectedPaymentType = orderInput.paymentType,
            amountPaid = orderInput.amountPaid,
            onUpdateAmountPaid = { onEvent(OrderEvent.UpdateAmountPaid(it)) },
            onAddNewItemToOrder = { onEvent(OrderEvent.AddItemToOrder) },
            availableProducts = state.availableProducts,
            onSelectPaymentType = { onEvent(OrderEvent.UpdatePaymentType(it)) },
            onItemSelected = { tempEditorId, product ->
                onEvent(OrderEvent.UpdateItemProduct(tempEditorId, product))
            },
            onRemoveItemFromOrder = { tempEditorId ->
                onEvent(OrderEvent.RemoveItemFromOrder(tempEditorId))
            },
            onUpdateItemQuantity = { tempEditorId, quantity ->
                onEvent(OrderEvent.UpdateItemQuantity(tempEditorId, quantity))
            },
            onUpdateItemUnit = { tempEditorId, unit ->
                onEvent(OrderEvent.UpdateItemUnit(tempEditorId, unit))
            },
            onUpdateItemPrice = { tempEditorId, price ->
                onEvent(OrderEvent.UpdateItemPrice(tempEditorId, price))
            },
        )

        OrderTotalsSection(
            subtotal = orderInput.subtotal,
            debt = orderInput.selectedClient?.debt ?: 0.0,
            totalAmount = orderInput.totalAmount,
            amountPaid = orderInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = orderInput.amountRemaining
        )
    }
}