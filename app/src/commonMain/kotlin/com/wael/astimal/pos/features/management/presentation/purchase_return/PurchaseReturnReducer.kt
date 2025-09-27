package com.wael.astimal.pos.features.management.presentation.purchase_return

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.presentation.purchase.PurchaseContract

class PurchaseReturnReducer() :
    Reducer<PurchaseReturnContract.State, PurchaseReturnContract.Event, Nothing> {
    override fun reduce(
        previousState: PurchaseReturnContract.State,
        event: PurchaseReturnContract.Event
    ): Pair<PurchaseReturnContract.State, Nothing?> {
        return when (event) {
            is PurchaseReturnContract.Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is PurchaseReturnContract.Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is PurchaseReturnContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is PurchaseReturnContract.Event.PartnerBalanceChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        partnerBalance = event.balance
                    )
                ) to null

            is PurchaseReturnContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is PurchaseReturnContract.Event.UserLoaded ->
                previousState.copy(
                    currentUser = event.user,
                ) to null

            is PurchaseReturnContract.Event.DropdownDataLoaded ->
                previousState.copy(
                    dropdownData = event.data,
                ) to null

            is PurchaseReturnContract.Event.OrdersLoaded ->
                previousState.copy(orders = event.orders) to null

            is PurchaseReturnContract.Event.OrderSelected -> {
                previousState.copy(
                    selectedOrder = event.order,
                    currentOrderInput = PurchaseReturnContract.EditableOrder(
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

            is PurchaseReturnContract.Event.NewOrderClicked,
            is PurchaseReturnContract.Event.DeleteSucceeded ->
                previousState.copy(
                    isLoading = false,
                    selectedOrder = null,
                    currentOrderInput = PurchaseReturnContract.EditableOrder(
                        date = Clock.now(),
                        selectedStore = previousState.currentOrderInput.selectedStore,
                        selectedPartner = previousState.currentOrderInput.selectedPartner
                    ),
                ) to null

            // Form input updates
            is PurchaseReturnContract.Event.PartnerSelected ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        selectedPartner = event.partner
                    )
                ) to null

            is PurchaseReturnContract.Event.StoreChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        selectedStore = event.store
                    )
                ) to null

            is PurchaseReturnContract.Event.DateChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        date = event.date
                    )
                ) to null

            is PurchaseReturnContract.Event.PaymentMethodChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        paymentType = event.type
                    )
                ) to null

            is PurchaseReturnContract.Event.AmountPaidChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        amountPaid = event.amount
                    )
                ) to null

            is PurchaseReturnContract.Event.AddItem ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = previousState.currentOrderInput.items + EditableItem()
                    )
                ) to null

            is PurchaseReturnContract.Event.RemoveItem ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = previousState.currentOrderInput.items.filterNot { it.tempEditorId == event.editorId }
                    )
                ) to null

            is PurchaseReturnContract.Event.ItemStockChanged -> {
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

            is PurchaseReturnContract.Event.ItemProductChanged -> {
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

            is PurchaseReturnContract.Event.ItemMaxPriceChanged -> {
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

            is PurchaseReturnContract.Event.ItemMinPriceChanged -> {
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

            is PurchaseReturnContract.Event.ItemUnitChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) item.copy(isSelectedUnitIsMax = event.isMaxUnit) else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseReturnContract.Event.ItemMaxQuantityChanged -> {
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

            is PurchaseReturnContract.Event.ItemMinQuantityChanged -> {
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

            is PurchaseReturnContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is PurchaseReturnContract.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.html
            ) to null

            is PurchaseReturnContract.Event.SaveSucceeded ->
                previousState.copy(
                    invoiceForPdfDialog = event.invoice,
                    isLoading = false,
                    selectedOrder = null,
                    currentOrderInput = PurchaseReturnContract.EditableOrder(
                        date = Clock.now(),
                        selectedStore = previousState.currentOrderInput.selectedStore,
                        selectedPartner = previousState.currentOrderInput.selectedPartner
                    ),
                ) to null

            is PurchaseReturnContract.Event.DismissInvoiceForPdfDialog -> previousState .copy(
                invoiceForPdfDialog = null
            ) to null

            else -> previousState to null
        }
    }
}
