package com.wael.astimal.pos.features.management.presentation.purchase_return

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
    state: PurchaseReturnState,
    onEvent: (PurchaseReturnEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage, state.error) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseReturnEvent.ClearSnackbar)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseReturnEvent.ClearError)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        canEdit = state.canEdit,
        onQueryChange = { onEvent(PurchaseReturnEvent.UpdateQuery(it)) },
        onSearch = { onEvent(PurchaseReturnEvent.SearchReturns(it)) },
        onSearchActiveChange = { onEvent(PurchaseReturnEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedReturn?.lastModified,
        onDelete = { onEvent(PurchaseReturnEvent.DeleteReturn) },
        onCreate = { onEvent(PurchaseReturnEvent.SaveReturn) },
        onUpdate = { onEvent(PurchaseReturnEvent.SaveReturn) },
        onNew = { onEvent(PurchaseReturnEvent.OpenNewReturnForm) },
        searchResults = {
            ItemGrid(
                list = state.returns,
                onItemClick = { onEvent(PurchaseReturnEvent.SelectReturnToView(it)) },
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
    state: PurchaseReturnState,
    onEvent: (PurchaseReturnEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DataPicker(
            selectedDateMillis = state.currentReturnInput.date,
            onDateSelected = { onEvent(PurchaseReturnEvent.UpdateReturnDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.supplier),
            items = state.availableSuppliers,
            selectedItemId = state.selectedSupplier?.id,
            onItemSelected = { onEvent(PurchaseReturnEvent.SelectSupplier(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.id }
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.availableEmployees,
            selectedItemId = state.currentReturnInput.selectedEmployeeId,
            onItemSelected = { onEvent(PurchaseReturnEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
            enabled = state.currentUser?.isAdmin ?: false
        )

        OrderInputFields(
            itemList = state.currentReturnInput.items,
            selectedPaymentType = state.currentReturnInput.paymentType,
            amountPaid = state.currentReturnInput.amountPaid,
            onUpdateAmountPaid = { onEvent(PurchaseReturnEvent.UpdateAmountPaid(it)) },
            onAddNewItemToOrder = { onEvent(PurchaseReturnEvent.AddItemToReturn) },
            availableProducts = state.availableProducts,
            onSelectPaymentType = { onEvent(PurchaseReturnEvent.UpdatePaymentType(it)) },
            onItemSelected = { tempEditorId, product ->
                onEvent(PurchaseReturnEvent.UpdateItemProduct(tempEditorId, product))
            },
            onRemoveItemFromOrder = { tempEditorId ->
                onEvent(PurchaseReturnEvent.RemoveItemFromReturn(tempEditorId))
            },
            onUpdateItemUnit = { tempEditorId, isMaxUnitSelected ->
                onEvent(PurchaseReturnEvent.UpdateItemUnit(tempEditorId, isMaxUnitSelected))
            },
            onUpdateItemMaxUnitPrice = { tempEditorId, maxUnitPrice ->
                onEvent(PurchaseReturnEvent.UpdateItemMaxUnitPrice(tempEditorId, maxUnitPrice))
            },
            onUpdateItemMinUnitPrice = { tempEditorId, minUnitPrice ->
                onEvent(PurchaseReturnEvent.UpdateItemMinUnitPrice(tempEditorId, minUnitPrice))
            },
            onUpdateItemMaxUnitQuantity = { tempEditorId, maxUnitQuantity ->
                onEvent(PurchaseReturnEvent.UpdateItemMaxUnitQuantity(tempEditorId, maxUnitQuantity))
            },
            onUpdateItemMinUnitQuantity = { tempEditorId, minUnitQuantity ->
                onEvent(PurchaseReturnEvent.UpdateItemMinUnitQuantity(tempEditorId, minUnitQuantity))
            }
        )

        OrderTotalsSection(
            totalAmount = state.currentReturnInput.totalAmount,
            amountPaid = state.currentReturnInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = state.currentReturnInput.amountRemaining
        )
    }
}
