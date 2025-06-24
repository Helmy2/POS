package com.wael.astimal.pos.features.management.presentation.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.OrderInputFields
import com.wael.astimal.pos.core.presentation.compoenents.OrderTotalsSection
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun SalesRoute(
    viewModel: SalesViewModel = koinViewModel(), onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SalesScreen(
        state = state,
        eventFlow = viewModel.eventFlow,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@Composable
fun SalesScreen(
    state: OrderState,
    onEvent: (OrderEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        canEdit = state.canEdit,
        onQueryChange = { onEvent(OrderEvent.UpdateQuery(it)) },
        onSearch = { onEvent(OrderEvent.SearchOrders(it)) },
        onSearchActiveChange = { onEvent(OrderEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedOrder?.updatedAt,
        onDelete = {
            state.selectedOrder?.let { onEvent(OrderEvent.DeleteOrder(it.id.local)) }
        },
        onCreate = { onEvent(OrderEvent.SaveOrder) },
        onUpdate = { onEvent(OrderEvent.SaveOrder) },
        onNew = { onEvent(OrderEvent.OpenNewOrderForm) },
        searchResults = {
            ItemGrid(
                list = state.orders,
                onItemClick = { onEvent(OrderEvent.SelectOrderToView(it)) },
                label = {
                    Label("Order to ${it.client.name.displayName(language)}: ${it.invoiceNumber}")
                },
                isSelected = { product -> product.id.local == state.selectedOrder?.id?.local },
            )
        },
        mainContent = {
            OrderForm(state = state, onEvent = onEvent)
        },
        eventFlow = eventFlow
    )
}

@Composable
fun OrderForm(
    state: OrderState, onEvent: (OrderEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val orderInput = state.currentOrderInput
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {

        DataPicker(
            selectedDateMillis = orderInput.date,
            onDateSelected = { onEvent(OrderEvent.UpdateTransferDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.client),
            items = state.availableClients,
            selectedItemId = state.selectedClient?.clientId?.local,
            onItemSelected = { onEvent(OrderEvent.SelectClient(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.clientId?.local },
            canClearSelection = false,
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.availableEmployees,
            selectedItemId = orderInput.selectedEmployeeId,
            onItemSelected = { onEvent(OrderEvent.SelectEmployee(it.id)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.id },
            enabled = state.currentUser?.isAdmin ?: false,
            canClearSelection = false,
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
            onUpdateItemUnit = { tempEditorId, isMaxUnitSelected ->
                onEvent(OrderEvent.UpdateItemUnit(tempEditorId, isMaxUnitSelected))
            },
            onUpdateItemMaxUnitPrice = { tempEditorId, maxUnitPrice ->
                onEvent(OrderEvent.UpdateItemMaxUnitPrice(tempEditorId, maxUnitPrice))
            },
            onUpdateItemMinUnitPrice = { tempEditorId, minUnitPrice ->
                onEvent(OrderEvent.UpdateItemMinUnitPrice(tempEditorId, minUnitPrice))
            },
            onUpdateItemMaxUnitQuantity = { tempEditorId, maxUnitQuantity ->
                onEvent(OrderEvent.UpdateItemMaxUnitQuantity(tempEditorId, maxUnitQuantity))
            },
            onUpdateItemMinUnitQuantity = { tempEditorId, minUnitQuantity ->
                onEvent(OrderEvent.UpdateItemMinUnitQuantity(tempEditorId, minUnitQuantity))
            })

        OrderTotalsSection(
            totalAmount = orderInput.totalAmount,
            amountPaid = orderInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = orderInput.amountRemaining
        )
    }
}