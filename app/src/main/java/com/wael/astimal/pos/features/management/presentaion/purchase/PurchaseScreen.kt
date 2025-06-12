package com.wael.astimal.pos.features.management.presentaion.purchase

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
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.OrderInputFields
import com.wael.astimal.pos.core.presentation.compoenents.OrderTotalsSection
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.koin.androidx.compose.koinViewModel

@Composable
fun PurchaseRoute(
    viewModel: PurchaseViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PurchaseScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    )
}

@Composable
fun PurchaseScreen(
    state: PurchaseScreenState,
    onEvent: (PurchaseScreenEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage, state.error) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseScreenEvent.ClearSnackbar)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseScreenEvent.ClearError)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        canEdit = state.canEdit,
        onQueryChange = { onEvent(PurchaseScreenEvent.UpdateQuery(it)) },
        onSearch = { onEvent(PurchaseScreenEvent.SearchPurchases(it)) },
        onSearchActiveChange = { onEvent(PurchaseScreenEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedPurchase?.lastModified,
        onDelete = { onEvent(PurchaseScreenEvent.DeletePurchase) },
        onCreate = { onEvent(PurchaseScreenEvent.SavePurchase) },
        onUpdate = { onEvent(PurchaseScreenEvent.SavePurchase) },
        onNew = { onEvent(PurchaseScreenEvent.OpenNewPurchaseForm) },
        searchResults = {
            ItemGrid(
                list = state.purchases,
                onItemClick = { onEvent(PurchaseScreenEvent.SelectPurchaseToView(it)) },
                label = {
                    Label("Purchase from ${it.supplier?.name?.displayName(LocalAppLocale.current)}")
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
    state: PurchaseScreenState,
    onEvent: (PurchaseScreenEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val purchaseInput = state.currentPurchaseInput
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DataPicker(
            selectedDateMillis = purchaseInput.date,
            onDateSelected = { onEvent(PurchaseScreenEvent.UpdateTransferDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.supplier),
            items = state.availableSuppliers,
            selectedItemId = state.selectedSupplier?.id,
            onItemSelected = { onEvent(PurchaseScreenEvent.SelectSupplier(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.id }
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.availableEmployees,
            selectedItemId = purchaseInput.selectedEmployeeId,
            onItemSelected = { onEvent(PurchaseScreenEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
            enabled = state.currentUser?.isAdmin ?: false
        )

        OrderInputFields(
            itemList = purchaseInput.items,
            selectedPaymentType = purchaseInput.paymentType,
            amountPaid = purchaseInput.amountPaid,
            onUpdateAmountPaid = { onEvent(PurchaseScreenEvent.UpdateAmountPaid(it)) },
            onAddNewItemToOrder = { onEvent(PurchaseScreenEvent.AddItemToPurchase) },
            availableProducts = state.availableProducts,
            onSelectPaymentType = { onEvent(PurchaseScreenEvent.UpdatePaymentType(it)) },
            onItemSelected = { tempEditorId, product ->
                onEvent(PurchaseScreenEvent.UpdateItemProduct(tempEditorId, product))
            },
            onRemoveItemFromOrder = { tempEditorId ->
                onEvent(PurchaseScreenEvent.RemoveItemFromPurchase(tempEditorId))
            },
            onUpdateItemQuantity = { tempEditorId, quantity ->
                onEvent(PurchaseScreenEvent.UpdateItemQuantity(tempEditorId, quantity))
            },
            onUpdateItemUnit = { tempEditorId, unit ->
                onEvent(PurchaseScreenEvent.UpdateItemUnit(tempEditorId, unit))
            },
            onUpdateItemPrice = { tempEditorId, price ->
                onEvent(PurchaseScreenEvent.UpdateItemPrice(tempEditorId, price))
            },
        )

        OrderTotalsSection(
            totalAmount = purchaseInput.totalAmount,
            amountPaid = purchaseInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = purchaseInput.amountRemaining
        )
    }
}