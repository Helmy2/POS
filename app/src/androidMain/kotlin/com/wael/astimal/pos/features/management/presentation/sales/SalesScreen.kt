package com.wael.astimal.pos.features.management.presentation.sales

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.compoenents.editableOrderItems
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SalesRoute(
    viewModel: SalesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredOrders by viewModel.filteredOrdersState.collectAsStateWithLifecycle()

    SalesScreen(
        state = state,
        filteredOrders = filteredOrders,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun SalesScreen(
    state: SalesContract.State,
    filteredOrders: List<SalesOrder>,
    onEvent: (SalesContract.Event) -> Unit,
) {
    val language = LocalAppLocale.current
    val orderInput = state.currentOrderInput

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        canSave = state.canSave,
        onQueryChange = { onEvent(SalesContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(SalesContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(SalesContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(SalesContract.Event.BackClicked) },
        lastModifiedDate = state.selectedOrder?.updatedAt,
        onDelete = { onEvent(SalesContract.Event.DeleteClicked) },
        onCreate = { onEvent(SalesContract.Event.SaveClicked) },
        onUpdate = { onEvent(SalesContract.Event.SaveClicked) },
        onNew = { onEvent(SalesContract.Event.NewOrderClicked) },
        searchResults = {
            ItemGrid(
                list = filteredOrders,
                onItemClick = { onEvent(SalesContract.Event.OrderSelected(it)) },
                label = {
                    Label(
                        stringResource(
                            R.string.order_to_with_args,
                            it.client.name.displayName(language),
                            it.invoiceNumber
                        )
                    )
                },
                isSelected = { order -> order.id.local == state.selectedOrder?.id?.local },
            )
        },
        mainContent = {
            item {
                DataPicker(
                    selectedDateMillis = orderInput.date,
                    onDateSelected = { onEvent(SalesContract.Event.DateChanged(it)) },
                    modifier = Modifier.padding(8.dp),
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.client),
                    items = state.dropdownData.clients,
                    currentSelection = state.selectedClient?.name?.displayName(language) ?: "",
                    onItemSelected = { onEvent(SalesContract.Event.ClientSelected(it)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    modifier = Modifier.padding(8.dp),
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.employee),
                    items = state.dropdownData.employees,
                    currentSelection = orderInput.selectedEmployee?.name?.displayName(language)
                        ?: "",
                    onItemSelected = { onEvent(SalesContract.Event.EmployeeChanged(it)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    enabled = state.currentUser?.isAdmin == true,
                    modifier = Modifier.padding(8.dp),
                )
            }
            editableOrderItems(
                itemList = orderInput.items,
                availableProducts = state.dropdownData.products,
                onRemoveItemFromOrder = { editorId ->
                    onEvent(SalesContract.Event.RemoveItem(editorId))
                },
                onUpdateItemUnit = { editorId, isMaxUnitSelected ->
                    onEvent(SalesContract.Event.ItemUnitChanged(editorId, isMaxUnitSelected))
                },
                onUpdateItemMaxUnitPrice = { editorId, maxUnitPrice ->
                    onEvent(SalesContract.Event.ItemMaxPriceChanged(editorId, maxUnitPrice))
                },
                onUpdateItemMinUnitPrice = { editorId, minUnitPrice ->
                    onEvent(SalesContract.Event.ItemMinPriceChanged(editorId, minUnitPrice))
                },
                onUpdateItemMaxUnitQuantity = { editorId, maxUnitQuantity ->
                    onEvent(
                        SalesContract.Event.ItemMaxQuantityChanged(
                            editorId, maxUnitQuantity
                        )
                    )
                },
                onUpdateItemMinUnitQuantity = { editorId, minUnitQuantity ->
                    onEvent(
                        SalesContract.Event.ItemMinQuantityChanged(
                            editorId, minUnitQuantity
                        )
                    )
                },
                onUpdateAmountPaid = {
                    onEvent(SalesContract.Event.AmountPaidChanged(it))
                },
                selectedPaymentType = orderInput.paymentType,
                onSelectPaymentType = { onEvent(SalesContract.Event.PaymentTypeChanged(it)) },
                onItemProductChanged = { editorId, product ->
                    onEvent(SalesContract.Event.ItemProductChanged(editorId, product))
                },
                totalAmount = orderInput.totalAmount,
                amountRemaining = orderInput.amountRemaining,
                amountPaid = orderInput.amountPaid,
                partnerBalance = orderInput.partnerBalance,
                partnerBalanceAfterThisOrder = orderInput.partnerBalanceAfterThisOrder,
                onAddNewItemToOrder = {
                    onEvent(SalesContract.Event.AddItem)
                },
            )
        },
    )
}
