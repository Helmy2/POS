package com.wael.astimal.pos.features.management.presentation.sales_return

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.presentation.purchase.PurchaseContract

class SalesReturnReducer() :
    Reducer<SalesReturnContract.State, SalesReturnContract.Event, Nothing> {
    override fun reduce(
        previousState: SalesReturnContract.State,
        event: SalesReturnContract.Event
    ): Pair<SalesReturnContract.State, Nothing?> {
        return when (event) {
            is SalesReturnContract.Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is SalesReturnContract.Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is SalesReturnContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is SalesReturnContract.Event.PartnerBalanceChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        partnerBalance = event.balance
                    )
                ) to null

            is SalesReturnContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is SalesReturnContract.Event.UserLoaded ->
                previousState.copy(
                    currentUser = event.user,
                ) to null

            is SalesReturnContract.Event.DropdownDataLoaded ->
                previousState.copy(
                    dropdownData = event.data,
                ) to null

            is SalesReturnContract.Event.OrdersLoaded ->
                previousState.copy(orders = event.orders) to null

            is SalesReturnContract.Event.OrderSelected -> {
                previousState.copy(
                    selectedOrder = event.order,
                    currentOrderInput = SalesReturnContract.EditableOrder(
                        selectedPartner = event.order.partner,
                        selectedStore = event.order.store,
                        paymentType = event.order.paymentMethod,
                        date = event.order.orderDate,
                        items = event.order.items.map {
                            EditableItem(
                                product = it.product,
                                mainUnitQuantity = it.quantity.toString(),
                                subUnitQuantity = (it.quantity * it.product.subUnitsPerMainUnit).toString(),
                                mainUnitPrice = it.unitPrice.toString(),
                                subUnitPrice = (it.unitPrice * it.product.subUnitsPerMainUnit).toString(),
                            )
                        },
                        amountPaid = event.order.paidAmount.toString()
                    ),
                    isSearchActive = false
                ) to null
            }

            is SalesReturnContract.Event.NewOrderClicked,
            is SalesReturnContract.Event.DeleteSucceeded ->
                previousState.copy(
                    isLoading = false,
                    selectedOrder = null,
                    currentOrderInput = SalesReturnContract.EditableOrder(
                        date = Clock.now(),
                        selectedStore = previousState.currentOrderInput.selectedStore,
                        selectedPartner = previousState.currentOrderInput.selectedPartner
                    ),
                ) to null

            // Form input updates
            is SalesReturnContract.Event.PartnerSelected ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        selectedPartner = event.partner
                    )
                ) to null

            is SalesReturnContract.Event.StoreChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        selectedStore = event.store
                    )
                ) to null

            is SalesReturnContract.Event.DateChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        date = event.date
                    )
                ) to null

            is SalesReturnContract.Event.PaymentMethodChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        paymentType = event.type
                    )
                ) to null

            is SalesReturnContract.Event.AmountPaidChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        amountPaid = event.amount
                    )
                ) to null

            is SalesReturnContract.Event.AddItem ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = previousState.currentOrderInput.items + EditableItem()
                    )
                ) to null

            is SalesReturnContract.Event.RemoveItem ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = previousState.currentOrderInput.items.filterNot { it.tempEditorId == event.editorId }
                    )
                ) to null

            is SalesReturnContract.Event.ItemStockChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        item.copy(currentStock = event.stock)
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.ItemProductChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            product = event.product,
                            mainUnitPrice = event.product?.sellingPrice.toString(),
                            subUnitPrice = (event.product?.sellingPrice?.div(conversionFactor)).toString(),
                            subUnitQuantity = conversionFactor.toString(),
                            mainUnitQuantity = "1.0",
                        )
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.ItemMaxPriceChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            mainUnitPrice = event.price,
                            subUnitPrice = (event.price.toDoubleOrNull()
                                ?.div(conversionFactor))?.toString() ?: "0.0"
                        )
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.ItemMinPriceChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            subUnitPrice = event.price,
                            mainUnitPrice = (event.price.toDoubleOrNull()
                                ?.times(conversionFactor))?.toString() ?: "0.0"
                        )
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.ItemUnitChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) item.copy(isSelectedUnitIsMax = event.isMaxUnit) else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.ItemMaxQuantityChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        val maxQty = event.quantity.toDoubleOrNull() ?: 0.0
                        item.copy(
                            mainUnitQuantity = event.quantity,
                            subUnitQuantity = (maxQty * conversionFactor).toString()
                        )
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.ItemMinQuantityChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        val minQty = event.quantity.toDoubleOrNull() ?: 0.0
                        item.copy(
                            subUnitQuantity = event.quantity,
                            mainUnitQuantity = if (conversionFactor > 0) (minQty / conversionFactor).toString() else "0.0"
                        )
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is SalesReturnContract.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.html
            ) to null

            is SalesReturnContract.Event.SaveSucceeded ->
                previousState.copy(
                    invoiceForPdfDialog = event.invoice,
                    isLoading = false,
                    selectedOrder = null,
                    currentOrderInput = SalesReturnContract.EditableOrder(
                        date = Clock.now(),
                        selectedStore = previousState.currentOrderInput.selectedStore,
                        selectedPartner = previousState.currentOrderInput.selectedPartner
                    ),
                ) to null

            is SalesReturnContract.Event.DismissInvoiceForPdfDialog -> previousState .copy(
                invoiceForPdfDialog = null
            ) to null
            else -> previousState to null
        }
    }
}
