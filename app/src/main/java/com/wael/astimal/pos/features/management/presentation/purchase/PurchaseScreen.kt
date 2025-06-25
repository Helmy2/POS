package com.wael.astimal.pos.features.management.presentation.purchase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.OrderItemRow
import com.wael.astimal.pos.core.presentation.compoenents.OrderTotalsSection
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
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
                    Label("Purchase from ${it.supplier.name.displayName(language)}: ${it.invoiceNumber}")
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
                    selectedItemId = purchaseInput.selectedEmployeeId,
                    onItemSelected = { onEvent(PurchaseContract.Event.EmployeeChanged(it.id)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id },
                    enabled = state.currentUser?.isAdmin == true,
                    canClearSelection = false,
                    modifier = Modifier.padding(8.dp),
                )
            }
            editableOrderItems(
                itemList = purchaseInput.items,
                availableProducts = state.dropdownData.products,
                onRemoveItemFromOrder = { editorId ->
                    onEvent(PurchaseContract.Event.RemoveItem(editorId))
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
                onUpdateItemMaxUnitQuantity = { editorId, maxUnitQuantity ->
                    onEvent(
                        PurchaseContract.Event.ItemMaxQuantityChanged(
                            editorId,
                            maxUnitQuantity
                        )
                    )
                },
                onUpdateItemMinUnitQuantity = { editorId, minUnitQuantity ->
                    onEvent(
                        PurchaseContract.Event.ItemMinQuantityChanged(
                            editorId,
                            minUnitQuantity
                        )
                    )
                },
                onUpdateAmountPaid = {
                    onEvent(PurchaseContract.Event.AmountPaidChanged(it))
                },
                selectedPaymentType = purchaseInput.paymentType,
                onSelectPaymentType = { onEvent(PurchaseContract.Event.PaymentTypeChanged(it)) },
                onItemProductChanged = { editorId, product ->
                    onEvent(PurchaseContract.Event.ItemProductChanged(editorId, product))
                },
                totalAmount = purchaseInput.totalAmount.toString(),
                amountRemaining = purchaseInput.amountRemaining.toString(),
                amountPaid = purchaseInput.amountRemaining.toString(),
                onAddNewItemToOrder = {
                    onEvent(PurchaseContract.Event.AddItem)
                })
        },
    )
}

fun LazyStaggeredGridScope.editableOrderItems(
    totalAmount: String,
    amountRemaining: String,
    itemList: List<EditableItem>,
    selectedPaymentType: PaymentType,
    amountPaid: String,
    onUpdateAmountPaid: (String) -> Unit,
    onAddNewItemToOrder: () -> Unit,
    availableProducts: List<Product>,
    onSelectPaymentType: (PaymentType) -> Unit,
    onItemProductChanged: (tempEditorId: String, product: Product?) -> Unit,
    onRemoveItemFromOrder: (tempEditorId: String) -> Unit,
    onUpdateItemMaxUnitQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemMinUnitQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemUnit: (tempEditorId: String, isMaxUnitSelected: Boolean) -> Unit,
    onUpdateItemMaxUnitPrice: (tempEditorId: String, price: String) -> Unit,
    onUpdateItemMinUnitPrice: (tempEditorId: String, price: String) -> Unit,
) {
    items(itemList) { item ->
        OrderItemRow(
            item = item,
            availableProducts = availableProducts,
            onUpdateSelectedItem = onItemProductChanged,
            onRemoveItemFromOrder = onRemoveItemFromOrder,
            onUpdateItemUnit = onUpdateItemUnit,
            onUpdateItemMaxUnitQuantity = onUpdateItemMaxUnitQuantity,
            onUpdateItemMinUnitQuantity = onUpdateItemMinUnitQuantity,
            onUpdateItemMaxUnitPrice = onUpdateItemMaxUnitPrice,
            onUpdateItemMinUnitPrice = onUpdateItemMinUnitPrice,
            modifier = Modifier.padding(8.dp),
        )
    }
    item {
        val context = LocalContext.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onAddNewItemToOrder() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.add_item))
            }

            CustomExposedDropdownMenu(
                label = stringResource(R.string.payment_type),
                items = PaymentType.entries,
                selectedItemId = selectedPaymentType.ordinal.toLong(),
                onItemSelected = onSelectPaymentType,
                itemToDisplayString = { context.getString(it.stringResource()) },
                itemToId = { it.ordinal.toLong() },
                canClearSelection = false,
                modifier = Modifier.padding(8.dp),
            )

            LabeledTextField(
                value = amountPaid,
                onValueChange = onUpdateAmountPaid,
                label = stringResource(R.string.amount_paid),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                ),
                enabled = true,
                modifier = Modifier.padding(8.dp),
            )

            OrderTotalsSection(
                totalAmount = totalAmount.toDoubleOrNull() ?: 0.0,
                amountPaid = amountPaid.toDoubleOrNull() ?: 0.0,
                amountRemaining = amountRemaining.toDoubleOrNull() ?: 0.0,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}