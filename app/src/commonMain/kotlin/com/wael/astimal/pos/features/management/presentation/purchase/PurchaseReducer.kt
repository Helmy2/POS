package com.wael.astimal.pos.features.management.presentation.purchase

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.EditableItem

class PurchaseReducer() : Reducer<PurchaseContract.State, PurchaseContract.Event, Nothing> {
    override fun reduce(
        previousState: PurchaseContract.State,
        event: PurchaseContract.Event
    ): Pair<PurchaseContract.State, Nothing?> {
        return when (event) {
            is PurchaseContract.Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is PurchaseContract.Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is PurchaseContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is PurchaseContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is PurchaseContract.Event.PartnerBalanceChanged ->
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        partnerBalance = event.balance
                    )
                ) to null

            is PurchaseContract.Event.UserLoaded ->
                previousState.copy(
                    currentUser = event.user,
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        selectedEmployee = event.user
                    )
                ) to null

            is PurchaseContract.Event.DropdownDataLoaded ->
                previousState.copy(
                    dropdownData = event.data,
                ) to null

            is PurchaseContract.Event.PurchasesLoaded ->
                previousState.copy(isLoading = false, purchases = event.purchases) to null

            is PurchaseContract.Event.PurchaseSelected -> {
                val editableItems = event.purchase.items.map {
                    val conversionFactor = it.product.subUnitsPerMainUnit
                    EditableItem(
                        tempEditorId = it.id.local.toString(),
                        product = it.product,
                        maxUnitQuantity = it.quantity.toString(),
                        minUnitQuantity = (it.quantity * conversionFactor).toString(),
                        maxUnitPrice = it.purchasePrice.toString(),
                        minUnitPrice = (it.purchasePrice / conversionFactor).toString()
                    )
                }
                previousState.copy(
                    selectedPurchase = event.purchase,
                    selectedSupplier = event.purchase.supplier,
                    currentPurchaseInput = PurchaseContract.EditablePurchase(
                        selectedEmployee = event.purchase.user,
                        paymentType = event.purchase.paymentType,
                        date = event.purchase.data,
                        items = editableItems,
                        amountPaid = event.purchase.amountPaid.toString()
                    ),
                    isSearchActive = false
                ) to null
            }

            is PurchaseContract.Event.NewPurchaseClicked,
            is PurchaseContract.Event.SaveSucceeded,
            is PurchaseContract.Event.DeleteSucceeded ->
                previousState.copy(
                    isLoading = false,
                    selectedPurchase = null,
                    currentPurchaseInput = PurchaseContract.EditablePurchase(
                        date = Clock.now(),
                        selectedEmployee = previousState.currentUser,
                    )
                ) to null

            // Form input updates
            is PurchaseContract.Event.SupplierSelected ->
                previousState.copy(selectedSupplier = event.supplier) to null

            is PurchaseContract.Event.EmployeeChanged ->
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        selectedEmployee = event.employee
                    )
                ) to null

            is PurchaseContract.Event.DateChanged ->
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        date = event.date
                    )
                ) to null

            is PurchaseContract.Event.PaymentTypeChanged ->
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        paymentType = event.type
                    )
                ) to null

            is PurchaseContract.Event.AmountPaidChanged ->
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        amountPaid = event.amount
                    )
                ) to null

            is PurchaseContract.Event.AddItem ->
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = previousState.currentPurchaseInput.items + EditableItem()
                    )
                ) to null

            is PurchaseContract.Event.RemoveItem ->
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = previousState.currentPurchaseInput.items.filterNot { it.tempEditorId == event.editorId }
                    )
                ) to null

            is PurchaseContract.Event.ItemStockChanged -> {
                val updatedItems = previousState.currentPurchaseInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        item.copy(currentStock = event.stock)
                    } else item
                }
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseContract.Event.ItemProductChanged -> {
                val updatedItems = previousState.currentPurchaseInput.items.map { item ->
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
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseContract.Event.ItemMaxPriceChanged -> {
                val updatedItems = previousState.currentPurchaseInput.items.map { item ->
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
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseContract.Event.ItemMinPriceChanged -> {
                val updatedItems = previousState.currentPurchaseInput.items.map { item ->
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
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseContract.Event.ItemUnitChanged -> {
                val updatedItems = previousState.currentPurchaseInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) item.copy(isSelectedUnitIsMax = event.isMaxUnit) else item
                }
                previousState.copy(
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseContract.Event.ItemMaxQuantityChanged -> {
                val updatedItems = previousState.currentPurchaseInput.items.map { item ->
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
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseContract.Event.ItemMinQuantityChanged -> {
                val updatedItems = previousState.currentPurchaseInput.items.map { item ->
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
                    currentPurchaseInput = previousState.currentPurchaseInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseContract.Event.BackClicked,
            is PurchaseContract.Event.SaveClicked,
            is PurchaseContract.Event.DeleteClicked -> previousState to null
        }
    }
}
