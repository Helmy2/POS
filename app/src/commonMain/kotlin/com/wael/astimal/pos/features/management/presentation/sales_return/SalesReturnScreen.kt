package com.wael.astimal.pos.features.management.presentation.sales_return

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.compoenents.editableOrderItems
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.client
import pos.app.generated.resources.employee
import pos.app.generated.resources.return_from_with_args

@Composable
fun SalesReturnRoute(
    viewModel: SalesReturnViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredReturns by viewModel.filteredReturnsState.collectAsStateWithLifecycle()

    SalesReturnScreen(
        state = state,
        filteredReturns = filteredReturns,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun SalesReturnScreen(
    state: SalesReturnContract.State,
    filteredReturns: List<SalesReturn>,
    onEvent: (SalesReturnContract.Event) -> Unit,
) {
    val language = LocalAppLocale.current
    val returnInput = state.currentReturnInput

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        canSave = state.canSave,
        onQueryChange = { onEvent(SalesReturnContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(SalesReturnContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(SalesReturnContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(SalesReturnContract.Event.BackClicked) },
        lastModifiedDate = state.selectedReturn?.updatedAt,
        onDelete = { onEvent(SalesReturnContract.Event.DeleteClicked) },
        onCreate = { onEvent(SalesReturnContract.Event.SaveClicked) },
        onUpdate = { onEvent(SalesReturnContract.Event.SaveClicked) },
        onNew = { onEvent(SalesReturnContract.Event.NewReturnClicked) },
        searchResults = {
            ItemGrid(
                list = filteredReturns,
                onItemClick = { onEvent(SalesReturnContract.Event.ReturnSelected(it)) },
                label = {
                    Label(
                        stringResource(
                            Res.string.return_from_with_args,
                            it.client.name.displayName(language),
                            it.invoiceNumber
                        )
                    )
                },
                isSelected = { salesReturn -> salesReturn.id.local == state.selectedReturn?.id?.local },
            )
        },
        mainContent = {
            item {
                DataPicker(
                    selectedDateMillis = returnInput.createdAt,
                    onDateSelected = { onEvent(SalesReturnContract.Event.DateChanged(it)) },
                    modifier = Modifier.padding(8.dp),
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.client),
                    items = state.dropdownData.clients,
                    currentSelection = state.selectedClient?.name?.displayName(language) ?: "",
                    onItemSelected = { onEvent(SalesReturnContract.Event.ClientSelected(it)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    modifier = Modifier.padding(8.dp),
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.employee),
                    items = state.dropdownData.employees,
                    currentSelection = returnInput.selectedEmployee?.name?.displayName(language)
                        ?: "",
                    onItemSelected = { onEvent(SalesReturnContract.Event.EmployeeChanged(it)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    enabled = state.currentUser?.isAdmin == true,
                    modifier = Modifier.padding(8.dp),
                )
            }
            editableOrderItems(
                amountPaid = returnInput.amountPaid,
                partnerBalance = returnInput.partnerBalance,
                partnerBalanceAfterThisOrder = returnInput.partnerBalanceAfterThisOrder,
                totalAmount = returnInput.totalAmount,
                amountRemaining = returnInput.amountRemaining,
                itemList = returnInput.items,
                selectedPaymentType = returnInput.paymentType,
                onUpdateAmountPaid = {
                    onEvent(SalesReturnContract.Event.AmountPaidChanged(it))
                },
                onAddNewItemToOrder = {
                    onEvent(SalesReturnContract.Event.AddItem)
                },
                availableProducts = state.dropdownData.products,
                onSelectPaymentType = { onEvent(SalesReturnContract.Event.PaymentTypeChanged(it)) },
                onItemProductChanged = { editorId, product ->
                    onEvent(SalesReturnContract.Event.ItemProductChanged(editorId, product))
                },
                onRemoveItemFromOrder = { editorId ->
                    onEvent(SalesReturnContract.Event.RemoveItem(editorId))
                },
                onUpdateItemMaxUnitQuantity = { editorId, maxUnitQuantity ->
                    onEvent(
                        SalesReturnContract.Event.ItemMaxQuantityChanged(
                            editorId, maxUnitQuantity
                        )
                    )
                },
                onUpdateItemMinUnitQuantity = { editorId, minUnitQuantity ->
                    onEvent(
                        SalesReturnContract.Event.ItemMinQuantityChanged(
                            editorId, minUnitQuantity
                        )
                    )
                },
                onUpdateItemUnit = { editorId, isMaxUnitSelected ->
                    onEvent(SalesReturnContract.Event.ItemUnitChanged(editorId, isMaxUnitSelected))
                },
                onUpdateItemMaxUnitPrice = { editorId, maxUnitPrice ->
                    onEvent(SalesReturnContract.Event.ItemMaxPriceChanged(editorId, maxUnitPrice))
                },
                onUpdateItemMinUnitPrice = { editorId, minUnitPrice ->
                    onEvent(SalesReturnContract.Event.ItemMinPriceChanged(editorId, minUnitPrice))
                },
            )
        },
    )
}
