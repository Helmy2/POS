package com.wael.astimal.pos.features.management.presentaion.purchase_return

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.OrderInputFields
import com.wael.astimal.pos.core.presentation.compoenents.OrderTotalsSection
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.koin.androidx.compose.koinViewModel

@Composable
fun PurchaseReturnRoute(
    viewModel: PurchaseReturnViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PurchaseReturnScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    )
}

@Composable
fun PurchaseReturnScreen(
    state: PurchaseReturnScreenState,
    onEvent: (PurchaseReturnScreenEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage, state.error) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseReturnScreenEvent.ClearSnackbar)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseReturnScreenEvent.ClearError)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        canEdit = state.canEdit,
        onQueryChange = { onEvent(PurchaseReturnScreenEvent.UpdateQuery(it)) },
        onSearch = { onEvent(PurchaseReturnScreenEvent.SearchReturns(it)) },
        onSearchActiveChange = { onEvent(PurchaseReturnScreenEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedReturn?.lastModified,
        onDelete = { onEvent(PurchaseReturnScreenEvent.DeleteReturn) },
        onCreate = { onEvent(PurchaseReturnScreenEvent.SaveReturn) },
        onUpdate = { onEvent(PurchaseReturnScreenEvent.SaveReturn) },
        onNew = { onEvent(PurchaseReturnScreenEvent.OpenNewReturnForm) },
        searchResults = {
            ItemGrid(
                list = state.returns,
                onItemClick = { onEvent(PurchaseReturnScreenEvent.SelectReturnToView(it)) },
                label = {
                    Text(
                        "Return to ${it.supplier?.name?.displayName(LocalAppLocale.current)}",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                isSelected = { item -> item.localId == state.selectedReturn?.localId },
            )
        },
        mainContent = {
            PurchaseReturnForm(state = state, onEvent = onEvent)
        }
    )
}

@Composable
fun PurchaseReturnForm(
    state: PurchaseReturnScreenState,
    onEvent: (PurchaseReturnScreenEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val input = state.input
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DataPicker(
            selectedDateMillis = input.date,
            onDateSelected = { onEvent(PurchaseReturnScreenEvent.UpdateTransferDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.supplier),
            items = state.availableSuppliers,
            selectedItemId = state.selectedSupplier?.id,
            onItemSelected = { onEvent(PurchaseReturnScreenEvent.SelectSupplier(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.id }
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.availableEmployees,
            selectedItemId = input.selectedEmployeeId,
            onItemSelected = { onEvent(PurchaseReturnScreenEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
            enabled = state.currentUser?.isAdmin ?: false
        )

        OrderInputFields(
            itemList = input.items,
            selectedPaymentType = input.paymentType,
            amountPaid = input.amountPaid,
            onUpdateAmountPaid = { onEvent(PurchaseReturnScreenEvent.UpdateAmountPaid(it)) },
            onAddNewItemToOrder = { onEvent(PurchaseReturnScreenEvent.AddItemToReturn) },
            availableProducts = state.availableProducts,
            onSelectPaymentType = { onEvent(PurchaseReturnScreenEvent.UpdatePaymentType(it)) },
            onItemSelected = { tempEditorId, product ->
                onEvent(PurchaseReturnScreenEvent.UpdateItemProduct(tempEditorId, product))
            },
            onRemoveItemFromOrder = { tempEditorId ->
                onEvent(PurchaseReturnScreenEvent.RemoveItemFromReturn(tempEditorId))
            },
            onUpdateItemQuantity = { tempEditorId, quantity ->
                onEvent(PurchaseReturnScreenEvent.UpdateItemQuantity(tempEditorId, quantity))
            },
            onUpdateItemUnit = { tempEditorId, unit ->
                onEvent(PurchaseReturnScreenEvent.UpdateItemUnit(tempEditorId, unit))
            },
            onUpdateItemPrice = { tempEditorId, price ->
                onEvent(PurchaseReturnScreenEvent.UpdateItemPrice(tempEditorId, price))
            },
        )

        OrderTotalsSection(
            totalAmount = input.totalAmount,
            amountPaid = input.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = input.amountRemaining
        )
    }
}
