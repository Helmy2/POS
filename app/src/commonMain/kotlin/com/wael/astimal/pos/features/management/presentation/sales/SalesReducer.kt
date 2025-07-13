package com.wael.astimal.pos.features.management.presentation.sales

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.EditableItem

class SalesReducer() : Reducer<SalesContract.State, SalesContract.Event, Nothing> {
    override fun reduce(
        previousState: SalesContract.State,
        event: SalesContract.Event
    ): Pair<SalesContract.State, Nothing?> {
        return when (event) {
            is SalesContract.Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is SalesContract.Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is SalesContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is SalesContract.Event.PartnerBalanceChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        partnerBalance = event.balance
                    )
                ) to null

            is SalesContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is SalesContract.Event.UserLoaded ->
                previousState.copy(
                    currentUser = event.user,
                ) to null

            is SalesContract.Event.DropdownDataLoaded ->
                previousState.copy(
                    dropdownData = event.data,
                ) to null

            is SalesContract.Event.OrdersLoaded ->
                previousState.copy(isLoading = false, orders = event.orders) to null

            is SalesContract.Event.OrderSelected -> {
                val editableItems = event.order.items.map {
                    val conversionFactor = it.product.subUnitsPerMainUnit
                    EditableItem(
                        tempEditorId = it.id,
                        product = it.product,
                        maxUnitQuantity = it.quantity.toString(),
                        minUnitQuantity = (it.quantity * conversionFactor).toString(),
                        maxUnitPrice = "",
                        minUnitPrice = ""
                    )
                }
                previousState.copy(
                    selectedOrder = event.order,
                    currentOrderInput = SalesContract.EditableOrder(
                        selectedPartner = event.order.partner,
                        selectedStore = event.order.store,
                        paymentType = event.order.paymentMethod,
                        date = event.order.createdAt,
                        items = editableItems,
                        amountPaid = event.order.paidAmount.toString()
                    ),
                    isSearchActive = false
                ) to null
            }

            is SalesContract.Event.NewOrderClicked,
            is SalesContract.Event.SaveSucceeded,
            is SalesContract.Event.DeleteSucceeded ->
                previousState.copy(
                    isLoading = false,
                    selectedOrder = null,
                    currentOrderInput = SalesContract.EditableOrder(
                        date = Clock.now(),
                        selectedStore = previousState.currentOrderInput.selectedStore,
                        selectedPartner = previousState.currentOrderInput.selectedPartner
                    ),
                ) to null

            // Form input updates
            is SalesContract.Event.PartnerSelected ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        selectedPartner = event.partner
                    )
                ) to null

            is SalesContract.Event.StoreChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        selectedStore = event.store
                    )
                ) to null

            is SalesContract.Event.DateChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        date = event.date
                    )
                ) to null

            is SalesContract.Event.PaymentMethodChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        paymentType = event.type
                    )
                ) to null

            is SalesContract.Event.AmountPaidChanged ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        amountPaid = event.amount
                    )
                ) to null

            is SalesContract.Event.AddItem ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = previousState.currentOrderInput.items + EditableItem()
                    )
                ) to null

            is SalesContract.Event.RemoveItem ->
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = previousState.currentOrderInput.items.filterNot { it.tempEditorId == event.editorId }
                    )
                ) to null

            is SalesContract.Event.ItemStockChanged -> {
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

            is SalesContract.Event.ItemProductChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            product = event.product,
                            maxUnitPrice = event.product?.sellingPrice.toString(),
                            minUnitPrice = (event.product?.sellingPrice?.div(conversionFactor)).toString(),
                            minUnitQuantity = conversionFactor.toString(),
                            maxUnitQuantity = "1.0",
                        )
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesContract.Event.ItemMaxPriceChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            maxUnitPrice = event.price,
                            minUnitPrice = (event.price.toDoubleOrNull()
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

            is SalesContract.Event.ItemMinPriceChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            minUnitPrice = event.price,
                            maxUnitPrice = (event.price.toDoubleOrNull()
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

            is SalesContract.Event.ItemUnitChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) item.copy(isSelectedUnitIsMax = event.isMaxUnit) else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesContract.Event.ItemMaxQuantityChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        val maxQty = event.quantity.toDoubleOrNull() ?: 0.0
                        item.copy(
                            maxUnitQuantity = event.quantity,
                            minUnitQuantity = (maxQty * conversionFactor).toString()
                        )
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesContract.Event.ItemMinQuantityChanged -> {
                val updatedItems = previousState.currentOrderInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        val minQty = event.quantity.toDoubleOrNull() ?: 0.0
                        item.copy(
                            minUnitQuantity = event.quantity,
                            maxUnitQuantity = if (conversionFactor > 0) (minQty / conversionFactor).toString() else "0.0"
                        )
                    } else item
                }
                previousState.copy(
                    currentOrderInput = previousState.currentOrderInput.copy(
                        items = updatedItems
                    )
                ) to null
            }


            is SalesContract.Event.BackClicked,
            is SalesContract.Event.SaveClicked,
            is SalesContract.Event.DeleteClicked -> previousState to null
        }
    }
}
