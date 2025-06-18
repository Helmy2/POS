package com.wael.astimal.pos.features.management.presentation.sales_return

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.OrderInputFields
import com.wael.astimal.pos.core.presentation.compoenents.OrderTotalsSection
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun SalesReturnRoute(
    viewModel: SalesReturnViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SalesReturnScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        eventFlow = viewModel.eventFlow,
    )
}

@Composable
fun SalesReturnScreen(
    state: SalesReturnState,
    onEvent: (SalesReturnEvent) -> Unit,
    onBack: () -> Unit,
    eventFlow: SharedFlow<UiEvent>,
) {

    SearchScreen(
        eventFlow = eventFlow,
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        canEdit = state.canEdit,
        onQueryChange = { onEvent(SalesReturnEvent.UpdateQuery(it)) },
        onSearch = { onEvent(SalesReturnEvent.SearchReturns(it)) },
        onSearchActiveChange = { onEvent(SalesReturnEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedReturn?.updatedAt,
        onCreate = { onEvent(SalesReturnEvent.SaveReturn) },
        onNew = { onEvent(SalesReturnEvent.OpenNewReturnForm) },
        searchResults = {
            ItemGrid(
                list = state.returns,
                onItemClick = { onEvent(SalesReturnEvent.SelectReturnToView(it)) },
                label = {
                    Text(
                        "Return to ${it.client.name.displayName(LocalAppLocale.current)}: ${it.invoiceNumber}",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                isSelected = { item -> item.id.local == state.selectedReturn?.id?.local },
            )
        },
        mainContent = {
            SalesReturnForm(state = state, onEvent = onEvent)
        },
        onDelete = { onEvent(SalesReturnEvent.DeleteReturn) },
        onUpdate = { onEvent(SalesReturnEvent.SaveReturn) },
    )
}


@Composable
fun SalesReturnForm(
    state: SalesReturnState, onEvent: (SalesReturnEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val returnInput = state.currentReturnInput
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        DataPicker(
            selectedDateMillis = returnInput.date,
            onDateSelected = { onEvent(SalesReturnEvent.UpdateReturnDate(it)) },
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.client),
            items = state.availableClients,
            selectedItemId = state.selectedClient?.clientLocalId?.local,
            onItemSelected = { onEvent(SalesReturnEvent.SelectClient(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.clientLocalId?.local },
            canClearSelection = false,
        )

        CustomExposedDropdownMenu(
            label = stringResource(R.string.employee),
            items = state.availableEmployees,
            selectedItemId = returnInput.selectedEmployeeId,
            onItemSelected = { onEvent(SalesReturnEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
            enabled = state.currentUser?.isAdmin ?: false,
            canClearSelection = false,
        )

        OrderInputFields(
            itemList = returnInput.items,
            selectedPaymentType = returnInput.paymentType,
            amountPaid = returnInput.amountPaid,
            onUpdateAmountPaid = { onEvent(SalesReturnEvent.UpdateAmountPaid(it)) },
            onAddNewItemToOrder = { onEvent(SalesReturnEvent.AddItemToReturn) },
            availableProducts = state.availableProducts,
            onSelectPaymentType = { onEvent(SalesReturnEvent.UpdatePaymentType(it)) },
            onItemSelected = { tempEditorId, product ->
                onEvent(SalesReturnEvent.UpdateItemProduct(tempEditorId, product))
            },
            onRemoveItemFromOrder = { tempEditorId ->
                onEvent(SalesReturnEvent.RemoveItemFromReturn(tempEditorId))
            },
            onUpdateItemUnit = { tempEditorId, isMaxUnitSelected ->
                onEvent(SalesReturnEvent.UpdateItemUnit(tempEditorId, isMaxUnitSelected))
            },
            onUpdateItemMaxUnitPrice = { tempEditorId, maxUnitPrice ->
                onEvent(SalesReturnEvent.UpdateItemMaxUnitPrice(tempEditorId, maxUnitPrice))
            },
            onUpdateItemMinUnitPrice = { tempEditorId, minUnitPrice ->
                onEvent(SalesReturnEvent.UpdateItemMinUnitPrice(tempEditorId, minUnitPrice))
            },
            onUpdateItemMaxUnitQuantity = { tempEditorId, maxUnitQuantity ->
                onEvent(SalesReturnEvent.UpdateItemMaxUnitQuantity(tempEditorId, maxUnitQuantity))
            },
            onUpdateItemMinUnitQuantity = { tempEditorId, minUnitQuantity ->
                onEvent(SalesReturnEvent.UpdateItemMinUnitQuantity(tempEditorId, minUnitQuantity))
            }
        )

        OrderTotalsSection(
            totalAmount = returnInput.totalAmount,
            amountPaid = returnInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = returnInput.amountRemaining
        )
    }
}