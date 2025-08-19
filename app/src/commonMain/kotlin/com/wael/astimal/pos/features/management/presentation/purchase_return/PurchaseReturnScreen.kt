package com.wael.astimal.pos.features.management.presentation.purchase_return

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
fun PurchaseReturnRoute(
    viewModel: PurchaseReturnViewModel = koinViewModel(), invoiceId: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredReturns by viewModel.filteredOrdersState.collectAsStateWithLifecycle()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "purchase_return_report",
        onFinish = { viewModel.processEvent(PurchaseReturnContract.Event.PdfGenerationFinished) })

    LaunchedEffect(Unit) {
        viewModel.processEvent(PurchaseReturnContract.Event.LoadInitialInvoice(invoiceId))
    }

    PurchaseReturnScreen(
        state = state,
        filteredOrders = filteredReturns,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun PurchaseReturnScreen(
    state: PurchaseReturnContract.State,
    filteredOrders: List<Invoice>,
    onEvent: (PurchaseReturnContract.Event) -> Unit,
) {
    val language = LocalAppLocale.current
    val orderInput = state.currentOrderInput

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        enableFab = state.canSave,
        onQueryChange = { onEvent(PurchaseReturnContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(PurchaseReturnContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(PurchaseReturnContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(PurchaseReturnContract.Event.BackClicked) },
        lastModifiedDate = state.selectedOrder?.updatedAt,
        onDelete = { onEvent(PurchaseReturnContract.Event.DeleteClicked) },
        onCreate = { onEvent(PurchaseReturnContract.Event.SaveClicked) },
        onUpdate = { onEvent(PurchaseReturnContract.Event.SaveClicked) },
        onNew = { onEvent(PurchaseReturnContract.Event.NewOrderClicked) },
        searchResults = {
            ItemGrid(
                list = filteredOrders,
                onItemClick = { onEvent(PurchaseReturnContract.Event.OrderSelected(it)) },
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
                        onEvent(PurchaseReturnContract.Event.GeneratePdf(state.selectedOrder))
                    }, modifier = Modifier.width(320.dp).padding(top = 32.dp)
                ) {
                    Text(text = stringResource(Res.string.generate_pdf))
                }
            }
            DataPicker(
                selectedDateMillis = orderInput.date,
                onDateSelected = { onEvent(PurchaseReturnContract.Event.DateChanged(it)) },
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.client),
                options = state.dropdownData.partners.map { it.name.displayName(language) },
                initialText = state.currentOrderInput.selectedPartner?.name.displayName(language),
                onItemSelected = {
                    onEvent(PurchaseReturnContract.Event.PartnerSelected(it?.let {
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
                    onEvent(PurchaseReturnContract.Event.StoreChanged(it?.let {
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
                    onEvent(PurchaseReturnContract.Event.RemoveItem(editorId))
                },
                onUpdateItemUnit = { editorId, isMaxUnitSelected ->
                    onEvent(
                        PurchaseReturnContract.Event.ItemUnitChanged(
                            editorId, isMaxUnitSelected
                        )
                    )
                },
                onUpdateItemMaxUnitPrice = { editorId, maxUnitPrice ->
                    onEvent(
                        PurchaseReturnContract.Event.ItemMaxPriceChanged(
                            editorId, maxUnitPrice
                        )
                    )
                },
                onUpdateItemMinUnitPrice = { editorId, minUnitPrice ->
                    onEvent(
                        PurchaseReturnContract.Event.ItemMinPriceChanged(
                            editorId, minUnitPrice
                        )
                    )
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
                onUpdateAmountPaid = {
                    onEvent(PurchaseReturnContract.Event.AmountPaidChanged(it))
                },
                selectedPaymentType = orderInput.paymentType,
                onSelectPaymentType = { onEvent(PurchaseReturnContract.Event.PaymentMethodChanged(it)) },
                onItemProductChanged = { editorId, product ->
                    onEvent(PurchaseReturnContract.Event.ItemProductChanged(editorId, product))
                },
                totalAmount = orderInput.totalAmount,
                amountRemaining = orderInput.amountRemaining,
                amountPaid = orderInput.amountPaid,
                partnerBalance = orderInput.partnerBalance,
                partnerBalanceAfterThisOrder = orderInput.partnerBalanceAfterThisOrder,
                onAddNewItemToOrder = {
                    onEvent(PurchaseReturnContract.Event.AddItem)
                },
            )
        },
    )
}