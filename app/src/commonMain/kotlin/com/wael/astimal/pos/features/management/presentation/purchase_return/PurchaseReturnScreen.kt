package com.wael.astimal.pos.features.management.presentation.purchase_return

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
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.employee
import pos.app.generated.resources.return_from_with_args
import pos.app.generated.resources.supplier

@Composable
fun PurchaseReturnRoute(
    viewModel: PurchaseReturnViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredReturns by viewModel.filteredReturnsState.collectAsStateWithLifecycle()

    PurchaseReturnScreen(
        state = state,
        filteredReturns = filteredReturns,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun PurchaseReturnScreen(
    state: PurchaseReturnContract.State,
    filteredReturns: List<PurchaseReturn>,
    onEvent: (PurchaseReturnContract.Event) -> Unit,
) {
    val language = LocalAppLocale.current
    val returnInput = state.currentReturnInput

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        canSave = state.canSave,
        onQueryChange = { onEvent(PurchaseReturnContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(PurchaseReturnContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(PurchaseReturnContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(PurchaseReturnContract.Event.BackClicked) },
        lastModifiedDate = state.selectedReturn?.updatedAt,
        onDelete = { onEvent(PurchaseReturnContract.Event.DeleteClicked) },
        onCreate = { onEvent(PurchaseReturnContract.Event.SaveClicked) },
        onUpdate = { onEvent(PurchaseReturnContract.Event.SaveClicked) },
        onNew = { onEvent(PurchaseReturnContract.Event.NewReturnClicked) },
        searchResults = {
            ItemGrid(
                list = filteredReturns,
                onItemClick = { onEvent(PurchaseReturnContract.Event.ReturnSelected(it)) },
                label = {
                    Label(
                        stringResource(
                            Res.string.return_from_with_args,
                            it.supplier.name.displayName(language),
                            it.invoiceNumber
                        )
                    )
                },
                isSelected = { purchaseReturn -> purchaseReturn.id.local == state.selectedReturn?.id?.local },
            )
        },
        mainContent = {
            item {
                DataPicker(
                    selectedDateMillis = returnInput.date,
                    onDateSelected = { onEvent(PurchaseReturnContract.Event.DateChanged(it)) },
                    modifier = Modifier.padding(8.dp),
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.supplier),
                    items = state.dropdownData.suppliers,
                    currentSelection = state.selectedSupplier?.name?.displayName(language) ?: "",
                    onItemSelected = { onEvent(PurchaseReturnContract.Event.SupplierSelected(it)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    modifier = Modifier.padding(8.dp),
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.employee),
                    items = state.dropdownData.employees,
                    currentSelection = returnInput.selectedEmployee?.localizedName?.displayName(
                        language
                    )
                        ?: "",
                    onItemSelected = { onEvent(PurchaseReturnContract.Event.EmployeeChanged(it)) },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    enabled = state.currentUser?.isAdmin == true,
                    modifier = Modifier.padding(8.dp),
                )
            }
            editableOrderItems(
                partnerBalance = returnInput.partnerBalance,
                partnerBalanceAfterThisOrder = returnInput.partnerBalanceAfterThisOrder,
                totalAmount = returnInput.totalAmount,
                amountRemaining = returnInput.amountRemaining,
                itemList = returnInput.items,
                selectedPaymentType = returnInput.paymentType,
                amountPaid = returnInput.amountPaid,
                onUpdateAmountPaid = {
                    onEvent(PurchaseReturnContract.Event.AmountPaidChanged(it))
                },
                onAddNewItemToOrder = {
                    onEvent(PurchaseReturnContract.Event.AddItem)
                },
                availableProducts = state.dropdownData.products,
                onSelectPaymentType = { onEvent(PurchaseReturnContract.Event.PaymentTypeChanged(it)) },
                onItemProductChanged = { editorId, product ->
                    onEvent(PurchaseReturnContract.Event.ItemProductChanged(editorId, product))
                },
                onRemoveItemFromOrder = { editorId ->
                    onEvent(PurchaseReturnContract.Event.RemoveItem(editorId))
                },
                onUpdateItemMaxUnitQuantity = { editorId, maxUnitQuantity ->
                    onEvent(
                        PurchaseReturnContract.Event.ItemMaxQuantityChanged(
                            editorId, maxUnitQuantity
                        )
                    )
                },
                onUpdateItemMinUnitQuantity = { editorId, minUnitQuantity ->
                    onEvent(
                        PurchaseReturnContract.Event.ItemMinQuantityChanged(
                            editorId, minUnitQuantity
                        )
                    )
                },
                onUpdateItemUnit = { editorId, isMaxUnitSelected ->
                    onEvent(
                        PurchaseReturnContract.Event.ItemUnitChanged(
                            editorId,
                            isMaxUnitSelected
                        )
                    )
                },
                onUpdateItemMaxUnitPrice = { editorId, maxUnitPrice ->
                    onEvent(
                        PurchaseReturnContract.Event.ItemMaxPriceChanged(
                            editorId,
                            maxUnitPrice
                        )
                    )
                },
                onUpdateItemMinUnitPrice = { editorId, minUnitPrice ->
                    onEvent(
                        PurchaseReturnContract.Event.ItemMinPriceChanged(
                            editorId,
                            minUnitPrice
                        )
                    )
                },
            )
        },
    )
}
