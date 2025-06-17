package com.wael.astimal.pos.features.management.presentation.purchase

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
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.OrderInputFields
import com.wael.astimal.pos.core.presentation.compoenents.OrderTotalsSection
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun PurchaseRoute(
    viewModel: PurchaseViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PurchaseScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        eventFlow = viewModel.eventFlow,
    )
}

@Composable
fun PurchaseScreen(
    state: PurchaseState,
    onEvent: (PurchaseEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>
) {
    SearchScreen(
        eventFlow = eventFlow,
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        canEdit = state.canEdit,
        onQueryChange = { onEvent(PurchaseEvent.UpdateQuery(it)) },
        onSearch = { onEvent(PurchaseEvent.SearchPurchases(it)) },
        onSearchActiveChange = { onEvent(PurchaseEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedPurchase?.lastModified,
        onDelete = { onEvent(PurchaseEvent.DeletePurchase) },
        onCreate = { onEvent(PurchaseEvent.SavePurchase) },
        onUpdate = { onEvent(PurchaseEvent.SavePurchase) },
        onNew = { onEvent(PurchaseEvent.OpenNewPurchaseForm) },
        searchResults = {
            ItemGrid(
                list = state.purchases,
                onItemClick = { onEvent(PurchaseEvent.SelectPurchaseToView(it)) },
                label = {
                    Label("Purchase from ${it.supplier?.name?.displayName(LocalAppLocale.current)}: ${it.invoiceNumber}")
                },
                isSelected = { purchase -> purchase.localId == state.selectedPurchase?.localId },
            )
        },
        mainContent = {
            PurchaseForm(state = state, onEvent = onEvent)
        }
    )
}

@Composable
fun PurchaseForm(
    state: PurchaseState,
    onEvent: (PurchaseEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val purchaseInput = state.currentPurchaseInput
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        DataPicker(
            selectedDateMillis = purchaseInput.date,
            onDateSelected = { onEvent(PurchaseEvent.UpdatePurchaseDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.supplier),
            items = state.availableSuppliers,
            selectedItemId = state.selectedSupplier?.id,
            onItemSelected = { onEvent(PurchaseEvent.SelectSupplier(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.id },
            canClearSelection = false,
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.availableEmployees,
            selectedItemId = purchaseInput.selectedEmployeeId,
            onItemSelected = { onEvent(PurchaseEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
            enabled = state.currentUser?.isAdmin ?: false,
            canClearSelection = false,
        )

        OrderInputFields(
            itemList = purchaseInput.items,
            selectedPaymentType = purchaseInput.paymentType,
            amountPaid = purchaseInput.amountPaid,
            onUpdateAmountPaid = { onEvent(PurchaseEvent.UpdateAmountPaid(it)) },
            onAddNewItemToOrder = { onEvent(PurchaseEvent.AddItemToPurchase) },
            availableProducts = state.availableProducts,
            onSelectPaymentType = { onEvent(PurchaseEvent.UpdatePaymentType(it)) },
            onItemSelected = { tempEditorId, product ->
                onEvent(PurchaseEvent.UpdateItemProduct(tempEditorId, product))
            },
            onRemoveItemFromOrder = { tempEditorId ->
                onEvent(PurchaseEvent.RemoveItemFromPurchase(tempEditorId))
            },
            onUpdateItemUnit = { tempEditorId, isMaxUnitSelected ->
                onEvent(PurchaseEvent.UpdateItemUnit(tempEditorId, isMaxUnitSelected))
            },
            onUpdateItemMaxUnitPrice = { tempEditorId, maxUnitPrice ->
                onEvent(PurchaseEvent.UpdateItemMaxUnitPrice(tempEditorId, maxUnitPrice))
            },
            onUpdateItemMinUnitPrice = { tempEditorId, minUnitPrice ->
                onEvent(PurchaseEvent.UpdateItemMinUnitPrice(tempEditorId, minUnitPrice))
            },
            onUpdateItemMaxUnitQuantity = { tempEditorId, maxUnitQuantity ->
                onEvent(PurchaseEvent.UpdateItemMaxUnitQuantity(tempEditorId, maxUnitQuantity))
            },
            onUpdateItemMinUnitQuantity = { tempEditorId, minUnitQuantity ->
                onEvent(PurchaseEvent.UpdateItemMinUnitQuantity(tempEditorId, minUnitQuantity))
            }
        )

        OrderTotalsSection(
            totalAmount = purchaseInput.totalAmount,
            amountPaid = purchaseInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = purchaseInput.amountRemaining
        )
    }
}