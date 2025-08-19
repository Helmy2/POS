package com.wael.astimal.pos.features.management.presentation.sales_return

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.displayName
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.presentation.compoenents.EditableOrderItems
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.client
import pos.app.generated.resources.generate_pdf
import pos.app.generated.resources.order_to_with_args
import pos.app.generated.resources.stores

@Composable
fun SalesReturnRoute(
    viewModel: SalesReturnViewModel = koinViewModel(),
    invoiceId: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredReturns by viewModel.filteredOrdersState.collectAsStateWithLifecycle()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "sales_return_report",
        onFinish = { viewModel.processEvent(SalesReturnContract.Event.PdfGenerationFinished) }
    )

    LaunchedEffect(Unit) {
        viewModel.processEvent(SalesReturnContract.Event.LoadInitialInvoice(invoiceId))
    }

    SalesReturnScreen(
        state = state,
        filteredOrders = filteredReturns,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun SalesReturnScreen(
    state: SalesReturnContract.State,
    filteredOrders: List<Invoice>,
    onEvent: (SalesReturnContract.Event) -> Unit,
) {
    val language = LocalAppLocale.current
    val orderInput = state.currentOrderInput

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        enableFab = state.canSave,
        onQueryChange = { onEvent(SalesReturnContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(SalesReturnContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(SalesReturnContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(SalesReturnContract.Event.BackClicked) },
        lastModifiedDate = state.selectedOrder?.updatedAt,
        onDelete = { onEvent(SalesReturnContract.Event.DeleteClicked) },
        onCreate = { onEvent(SalesReturnContract.Event.SaveClicked) },
        onUpdate = { onEvent(SalesReturnContract.Event.SaveClicked) },
        onNew = { onEvent(SalesReturnContract.Event.NewOrderClicked) },
        searchResults = {
            ItemGrid(
                list = filteredOrders,
                onItemClick = { onEvent(SalesReturnContract.Event.OrderSelected(it)) },
                label = {
                    Label(
                        stringResource(
                            Res.string.order_to_with_args,
                            it.partner.name.displayName(language),
                            it.id
                        )
                    )
                },
                isSelected = { order -> order.id == state.selectedOrder?.id },
            )
        },
        mainContent = {
            if (state.selectedOrder != null) {
                Button(
                    {
                        onEvent(SalesReturnContract.Event.GeneratePdf(state.selectedOrder))
                    }, modifier = Modifier.width(320.dp).padding(top = 32.dp)
                ) {
                    Text(text = stringResource(Res.string.generate_pdf))
                }
            }
            DataPicker(
                selectedDateMillis = orderInput.date,
                onDateSelected = { onEvent(SalesReturnContract.Event.DateChanged(it)) },
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.client),
                options = state.dropdownData.partners.map { it.name.displayName(language) },
                initialText = state.currentOrderInput.selectedPartner?.name.displayName(language),
                onItemSelected = {
                    onEvent(SalesReturnContract.Event.PartnerSelected(it?.let {
                        state.dropdownData.partners.getOrNull(
                            it
                        )
                    }))
                },
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.stores),
                options = state.dropdownData.stores.map { it.name.displayName(language) },
                initialText = orderInput.selectedStore?.name.displayName(language),
                onItemSelected = {
                    onEvent(SalesReturnContract.Event.StoreChanged(it?.let {
                        state.dropdownData.stores.getOrNull(
                            it
                        )
                    }))
                },
                enabled = state.currentUser?.isAdmin == true,
            )
            EditableOrderItems(
                itemList = orderInput.items,
                availableProducts = state.dropdownData.products,
                onRemoveItemFromOrder = { editorId ->
                    onEvent(SalesReturnContract.Event.RemoveItem(editorId))
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
                onUpdateAmountPaid = {
                    onEvent(SalesReturnContract.Event.AmountPaidChanged(it))
                },
                selectedPaymentType = orderInput.paymentType,
                onSelectPaymentType = { onEvent(SalesReturnContract.Event.PaymentMethodChanged(it)) },
                onItemProductChanged = { editorId, product ->
                    onEvent(SalesReturnContract.Event.ItemProductChanged(editorId, product))
                },
                totalAmount = orderInput.totalAmount,
                amountRemaining = orderInput.amountRemaining,
                amountPaid = orderInput.amountPaid,
                partnerBalance = orderInput.partnerBalance,
                partnerBalanceAfterThisOrder = orderInput.partnerBalanceAfterThisOrder,
                onAddNewItemToOrder = {
                    onEvent(SalesReturnContract.Event.AddItem)
                },
            )
        },
    )
}
