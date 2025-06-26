package com.wael.astimal.pos.features.management.presentation.purchase

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
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import org.koin.androidx.compose.koinViewModel

@Composable
fun PurchaseRoute(
    viewModel: PurchaseViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredPurchases by viewModel.filteredPurchasesState.collectAsStateWithLifecycle()

    PurchaseScreen(
        state = state,
        filteredPurchases = filteredPurchases,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun PurchaseScreen(
    state: PurchaseContract.State,
    filteredPurchases: List<PurchaseOrder>,
    onEvent: (PurchaseContract.Event) -> Unit,
) {
    val language = LocalAppLocale.current
    val purchaseInput = state.currentPurchaseInput

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        canSave = state.canSave,
        onQueryChange = { onEvent(PurchaseContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(PurchaseContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(PurchaseContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(PurchaseContract.Event.BackClicked) },
        lastModifiedDate = state.selectedPurchase?.updatedAt,
        onDelete = { onEvent(PurchaseContract.Event.DeleteClicked) },
        onCreate = { onEvent(PurchaseContract.Event.SaveClicked) },
        onUpdate = { onEvent(PurchaseContract.Event.SaveClicked) },
        onNew = { onEvent(PurchaseContract.Event.NewPurchaseClicked) },
        searchResults = {
            ItemGrid(
                list = filteredPurchases,
                onItemClick = { onEvent(PurchaseContract.Event.PurchaseSelected(it)) },
                label = {
                    Label(
                        stringResource(
                            R.string.purchase_from_with_args,
                            it.supplier.name.displayName(language),
                            it.invoiceNumber
                        )
                    )
                },
                isSelected = { purchase -> purchase.id.local == state.selectedPurchase?.id?.local },
            )
        },
        mainContent = {
            item {
                DataPicker(
                    selectedDateMillis = purchaseInput.date,
                    onDateSelected = { onEvent(PurchaseContract.Event.DateChanged(it)) },
                    modifier = Modifier.padding(8.dp),
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.supplier),
                    items = state.dropdownData.suppliers,
                    currentSelection = state.selectedSupplier?.name?.displayName(language) ?: "",
                    onItemSelected = { onEvent(PurchaseContract.Event.SupplierSelected(it)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    modifier = Modifier.padding(8.dp),
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.employee),
                    items = state.dropdownData.employees,
                    currentSelection = purchaseInput.selectedEmployee?.name?.displayName(language)
                        ?: "",
                    onItemSelected = { onEvent(PurchaseContract.Event.EmployeeChanged(it)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    enabled = state.currentUser?.isAdmin == true,
                    modifier = Modifier.padding(8.dp),
                )
            }
            editableOrderItems(
                partnerBalance = purchaseInput.partnerBalance,
                partnerBalanceAfterThisOrder = purchaseInput.partnerBalanceAfterThisOrder,
                totalAmount = purchaseInput.totalAmount,
                amountRemaining = purchaseInput.amountRemaining,
                itemList = purchaseInput.items,
                selectedPaymentType = purchaseInput.paymentType,
                amountPaid = purchaseInput.amountPaid,
                onUpdateAmountPaid = {
                    onEvent(PurchaseContract.Event.AmountPaidChanged(it))
                },
                onAddNewItemToOrder = {
                    onEvent(PurchaseContract.Event.AddItem)
                },
                availableProducts = state.dropdownData.products,
                onSelectPaymentType = { onEvent(PurchaseContract.Event.PaymentTypeChanged(it)) },
                onItemProductChanged = { editorId, product ->
                    onEvent(PurchaseContract.Event.ItemProductChanged(editorId, product))
                },
                onRemoveItemFromOrder = { editorId ->
                    onEvent(PurchaseContract.Event.RemoveItem(editorId))
                },
                onUpdateItemMaxUnitQuantity = { editorId, maxUnitQuantity ->
                    onEvent(
                        PurchaseContract.Event.ItemMaxQuantityChanged(
                            editorId, maxUnitQuantity
                        )
                    )
                },
                onUpdateItemMinUnitQuantity = { editorId, minUnitQuantity ->
                    onEvent(
                        PurchaseContract.Event.ItemMinQuantityChanged(
                            editorId, minUnitQuantity
                        )
                    )
                },
                onUpdateItemUnit = { editorId, isMaxUnitSelected ->
                    onEvent(PurchaseContract.Event.ItemUnitChanged(editorId, isMaxUnitSelected))
                },
                onUpdateItemMaxUnitPrice = { editorId, maxUnitPrice ->
                    onEvent(PurchaseContract.Event.ItemMaxPriceChanged(editorId, maxUnitPrice))
                },
                onUpdateItemMinUnitPrice = { editorId, minUnitPrice ->
                    onEvent(PurchaseContract.Event.ItemMinPriceChanged(editorId, minUnitPrice))
                },
            )
        },
    )
}

