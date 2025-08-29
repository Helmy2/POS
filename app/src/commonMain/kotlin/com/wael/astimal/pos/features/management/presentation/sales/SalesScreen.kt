package com.wael.astimal.pos.features.management.presentation.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import pos.app.generated.resources.add_partner
import pos.app.generated.resources.client
import pos.app.generated.resources.generate_pdf
import pos.app.generated.resources.order_to_with_args
import pos.app.generated.resources.stores

@Composable
fun SalesRoute(
    onNavigateToCreateBusinessPartner: () -> Unit,
    viewModel: SalesViewModel = koinViewModel(),
    invoiceId: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filteredOrders by viewModel.filteredOrdersState.collectAsStateWithLifecycle()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "sales_report",
        onFinish = { viewModel.processEvent(SalesContract.Event.PdfGenerationFinished) }
    )

    LaunchedEffect(Unit) {
        viewModel.processEvent(SalesContract.Event.LoadInitialInvoice(invoiceId))
    }

    SalesScreen(
        state = state,
        filteredOrders = filteredOrders,
        onEvent = viewModel::processEvent,
        onNavigateToCreateBusinessPartner = onNavigateToCreateBusinessPartner
    )
}

@Composable
fun SalesScreen(
    state: SalesContract.State,
    filteredOrders: List<Invoice>,
    onEvent: (SalesContract.Event) -> Unit,
    onNavigateToCreateBusinessPartner: () -> Unit,
) {
    val language = LocalAppLocale.current
    val orderInput = state.currentOrderInput

    SearchScreen(
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        enableFab = state.canSave,
        onQueryChange = { onEvent(SalesContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(SalesContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(SalesContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(SalesContract.Event.BackClicked) },
        lastModifiedDate = state.selectedOrder?.updatedAt,
        onDelete = { onEvent(SalesContract.Event.DeleteClicked) },
        onCreate = { onEvent(SalesContract.Event.SaveClicked) },
        onUpdate = { onEvent(SalesContract.Event.SaveClicked) },
        onNew = { onEvent(SalesContract.Event.NewOrderClicked) },
        searchResults = {
            ItemGrid(
                list = filteredOrders,
                onItemClick = { onEvent(SalesContract.Event.OrderSelected(it)) },
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
                        onEvent(SalesContract.Event.GeneratePdf(state.selectedOrder))
                    }, modifier = Modifier.width(320.dp).padding(top = 32.dp)
                ) {
                    Text(text = stringResource(Res.string.generate_pdf))
                }
            }
            DataPicker(
                selectedDateMillis = orderInput.date,
                onDateSelected = { onEvent(SalesContract.Event.DateChanged(it)) },
            )
            Row(
                modifier = Modifier.width(320.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExposedDropdownMenu(
                    label = stringResource(Res.string.client),
                    options = state.dropdownData.partners.map { it.name.displayName(language) },
                    initialText = state.currentOrderInput.selectedPartner?.name.displayName(language),
                    onItemSelected = {
                        onEvent(SalesContract.Event.PartnerSelected(it?.let {
                            state.dropdownData.partners.getOrNull(
                                it
                            )
                        }))
                    },
                    modifier = Modifier.width(250.dp),
                )
                IconButton(
                    onClick = { onNavigateToCreateBusinessPartner() },
                    modifier = Modifier.align(Alignment.Bottom).padding(horizontal = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.add_partner),
                    )
                }
            }
            ExposedDropdownMenu(
                label = stringResource(Res.string.stores),
                options = state.dropdownData.stores.map { it.name.displayName(language) },
                initialText = orderInput.selectedStore?.name.displayName(language),
                onItemSelected = {
                    onEvent(
                        SalesContract.Event.StoreChanged(
                            it?.let { state.dropdownData.stores.getOrNull(it) }
                        )
                    )
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
                    onEvent(SalesContract.Event.AmountPaidChanged(it))
                },
                onAddNewItemToOrder = {
                    onEvent(SalesContract.Event.AddItem)
                },
                availableProducts = state.dropdownData.products,
                onSelectPaymentType = { onEvent(SalesContract.Event.PaymentMethodChanged(it)) },
                onItemProductChanged = { editorId, product ->
                    onEvent(SalesContract.Event.ItemProductChanged(editorId, product))
                },
                onRemoveItemFromOrder = { editorId ->
                    onEvent(SalesContract.Event.RemoveItem(editorId))
                },
                onUpdateItemMaxUnitQuantity = { editorId, maxUnitQuantity ->
                    onEvent(
                        SalesContract.Event.ItemMaxQuantityChanged(
                            editorId, maxUnitQuantity
                        )
                    )
                },
                onUpdateItemMinUnitQuantity = { editorId, minUnitQuantity ->
                    onEvent(
                        SalesContract.Event.ItemMinQuantityChanged(
                            editorId, minUnitQuantity
                        )
                    )
                },
                onUpdateItemUnit = { editorId, isMaxUnitSelected ->
                    onEvent(SalesContract.Event.ItemUnitChanged(editorId, isMaxUnitSelected))
                },
                onUpdateItemMaxUnitPrice = { editorId, maxUnitPrice ->
                    onEvent(SalesContract.Event.ItemMaxPriceChanged(editorId, maxUnitPrice))
                },
            ) { editorId, minUnitPrice ->
                onEvent(SalesContract.Event.ItemMinPriceChanged(editorId, minUnitPrice))
            }
        },
    )
}
