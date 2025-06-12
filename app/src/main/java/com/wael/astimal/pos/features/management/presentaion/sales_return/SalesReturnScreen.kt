package com.wael.astimal.pos.features.management.presentaion.sales_return

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
fun SalesReturnRoute(
    viewModel: SalesReturnViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SalesReturnScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    )
}

@Composable
fun SalesReturnScreen(
    state: SalesReturnScreenState,
    onEvent: (SalesReturnScreenEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage, state.error) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(SalesReturnScreenEvent.ClearSnackbar)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(SalesReturnScreenEvent.ClearError)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        canEdit = state.canEdit,
        onQueryChange = { onEvent(SalesReturnScreenEvent.UpdateQuery(it)) },
        onSearch = { onEvent(SalesReturnScreenEvent.SearchReturns(it)) },
        onSearchActiveChange = { onEvent(SalesReturnScreenEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedReturn?.lastModified,
        onCreate = { onEvent(SalesReturnScreenEvent.SaveReturn) },
        onNew = { onEvent(SalesReturnScreenEvent.OpenNewReturnForm) },
        searchResults = {
            ItemGrid(
                list = state.returns,
                onItemClick = { onEvent(SalesReturnScreenEvent.SelectReturnToView(it)) },
                label = {
                    Text(
                        "Return to ${it.client?.name?.displayName(LocalAppLocale.current)}",
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
            SalesReturnForm(state = state, onEvent = onEvent)
        },
        onDelete = { onEvent(SalesReturnScreenEvent.DeleteReturn) },
        onUpdate = { onEvent(SalesReturnScreenEvent.SaveReturn) },
    )
}


@Composable
fun SalesReturnForm(
    state: SalesReturnScreenState, onEvent: (SalesReturnScreenEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val returnInput = state.input
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DataPicker(
            selectedDateMillis = state.input.date,
            onDateSelected = { onEvent(SalesReturnScreenEvent.UpdateItemDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.client),
            items = state.availableClients,
            selectedItemId = state.selectedClient?.id,
            onItemSelected = { onEvent(SalesReturnScreenEvent.SelectClient(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.id })

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.availableEmployees,
            selectedItemId = returnInput.selectedEmployeeId,
            onItemSelected = { onEvent(SalesReturnScreenEvent.UpdateSelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
            enabled = state.currentUser?.isAdmin ?: false
        )

        OrderInputFields(
            itemList = state.input.items,
            selectedPaymentType = state.input.paymentType,
            amountPaid = state.input.amountPaid,
            onUpdateAmountPaid = { onEvent(SalesReturnScreenEvent.UpdateAmountRefunded(it)) },
            onAddNewItemToOrder = { onEvent(SalesReturnScreenEvent.AddItemToReturn) },
            availableProducts = state.availableProducts,
            onSelectPaymentType = { onEvent(SalesReturnScreenEvent.UpdatePaymentType(it)) },
            onItemSelected = { tempEditorId, product ->
                onEvent(SalesReturnScreenEvent.UpdateItemProduct(tempEditorId, product))
            },
            onRemoveItemFromOrder = { tempEditorId ->
                onEvent(SalesReturnScreenEvent.RemoveItemFromReturn(tempEditorId))
            },
            onUpdateItemQuantity = { tempEditorId, quantity ->
                onEvent(SalesReturnScreenEvent.UpdateItemQuantity(tempEditorId, quantity))
            },
            onUpdateItemUnit = { tempEditorId, unit ->
                onEvent(SalesReturnScreenEvent.UpdateItemUnit(tempEditorId, unit))
            },
            onUpdateItemPrice = { tempEditorId, price ->
                onEvent(SalesReturnScreenEvent.UpdateItemPrice(tempEditorId, price))
            },
        )

        OrderTotalsSection(
            totalAmount = returnInput.totalAmount,
            amountPaid = returnInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = returnInput.amountRemaining
        )
    }
}