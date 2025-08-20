package com.wael.astimal.pos.features.management.presentation.purchase

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.displayName
import com.wael.astimal.pos.core.presentation.compoenents.AppButton
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
fun PurchaseRoute(
    viewModel: PurchaseViewModel = koinViewModel(),
    invoiceId: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredPurchases by viewModel.filteredOrdersState.collectAsStateWithLifecycle()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "purchases_report",
        onFinish = { viewModel.processEvent(PurchaseContract.Event.PdfGenerationFinished) }
    )

    LaunchedEffect(Unit) {
        viewModel.processEvent(PurchaseContract.Event.LoadInitialInvoice(invoiceId))
    }

    PurchaseScreen(
        state = state,
        filteredOrders = filteredPurchases,
        onEvent = viewModel::processEvent,
    )
}

@Composable
fun PurchaseScreen(
    state: PurchaseContract.State,
    filteredOrders: List<Invoice>,
    onEvent: (PurchaseContract.Event) -> Unit,
) {
    val language = LocalAppLocale.current
    val orderInput = state.currentOrderInput

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        enableFab = state.canSave,
        onQueryChange = { onEvent(PurchaseContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(PurchaseContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(PurchaseContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(PurchaseContract.Event.BackClicked) },
        lastModifiedDate = state.selectedOrder?.updatedAt,
        onDelete = { onEvent(PurchaseContract.Event.DeleteClicked) },
        onCreate = { onEvent(PurchaseContract.Event.SaveClicked) },
        onUpdate = { onEvent(PurchaseContract.Event.SaveClicked) },
        onNew = { onEvent(PurchaseContract.Event.NewOrderClicked) },
        searchResults = {
            ItemGrid(
                list = filteredOrders,
                onItemClick = { onEvent(PurchaseContract.Event.OrderSelected(it)) },
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
                AppButton(
                    {
                        onEvent(PurchaseContract.Event.GeneratePdf(state.selectedOrder))
                    },
                    modifier = Modifier.width(320.dp)
                        .padding(top = 32.dp)
                ) {
                    Text(text = stringResource(Res.string.generate_pdf))
                }
            }
            DataPicker(
                selectedDateMillis = orderInput.date,
                onDateSelected = { onEvent(PurchaseContract.Event.DateChanged(it)) },
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.client),
                options = state.dropdownData.partners.map { it.name.displayName(language) },
                initialText = state.currentOrderInput.selectedPartner?.name.displayName(language),
                onItemSelected = {
                    onEvent(PurchaseContract.Event.PartnerSelected(it?.let {
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
                    onEvent(PurchaseContract.Event.StoreChanged(it?.let {
                        state.dropdownData.stores.getOrNull(
                            it
                        )
                    }))
                },
                enabled = state.currentUser?.isAdmin == true,
            )
            EditableOrderItems(
                totalAmount = orderInput.totalAmount,
                amountRemaining = orderInput.amountRemaining,
                partnerBalance = orderInput.partnerBalance,
                partnerBalanceAfterThisOrder = orderInput.partnerBalanceAfterThisOrder,
                amountPaid = orderInput.amountPaid,
                itemList = orderInput.items,
                selectedPaymentType = orderInput.paymentType,
                onUpdateAmountPaid = {
                    onEvent(PurchaseContract.Event.AmountPaidChanged(it))
                },
                onAddNewItemToOrder = {
                    onEvent(PurchaseContract.Event.AddItem)
                },
                availableProducts = state.dropdownData.products,
                onSelectPaymentType = { onEvent(PurchaseContract.Event.PaymentMethodChanged(it)) },
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
            ) { editorId, minUnitPrice ->
                onEvent(PurchaseContract.Event.ItemMinPriceChanged(editorId, minUnitPrice))
            }
        },
    )
}

