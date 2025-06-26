package com.wael.astimal.pos.features.management.presentation.purchase_return

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.EditableItem

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

            is PurchaseReturnContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is PurchaseReturnContract.Event.PartnerBalanceChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        partnerBalance = event.balance
                    )
                ) to null

            is PurchaseReturnContract.Event.UserLoaded ->
                previousState.copy(
                    currentUser = event.user,
                    currentReturnInput = previousState.currentReturnInput.copy(
                        selectedEmployee = event.user
                    )
                ) to null

            is PurchaseReturnContract.Event.DropdownDataLoaded ->
                previousState.copy(dropdownData = event.data) to null

            is PurchaseReturnContract.Event.ReturnsLoaded ->
                previousState.copy(isLoading = false, returns = event.returns) to null

            is PurchaseReturnContract.Event.ReturnSelected -> {
                val editableItems = event.purchaseReturn.items.map {
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
                    selectedReturn = event.purchaseReturn,
                    selectedSupplier = event.purchaseReturn.supplier,
                    currentReturnInput = PurchaseReturnContract.EditableReturn(
                        selectedEmployee = event.purchaseReturn.employee,
                        paymentType = event.purchaseReturn.paymentType,
                        date = event.purchaseReturn.data,
                        items = editableItems,
                        amountPaid = event.purchaseReturn.amountPaid.toString()
                    ),
                    isSearchActive = false
                ) to null
            }

            is PurchaseReturnContract.Event.NewReturnClicked,
            is PurchaseReturnContract.Event.SaveSucceeded,
            is PurchaseReturnContract.Event.DeleteSucceeded ->
                previousState.copy(
                    isLoading = false,
                    selectedReturn = null,
                    currentReturnInput = PurchaseReturnContract.EditableReturn(
                        date = Clock.now(),
                        selectedEmployee = previousState.currentUser,
                    ),
                    selectedSupplier = null
                ) to null

            // Form input updates
            is PurchaseReturnContract.Event.SupplierSelected ->
                previousState.copy(selectedSupplier = event.supplier) to null

            is PurchaseReturnContract.Event.EmployeeChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        selectedEmployee = event.employee
                    )
                ) to null

            is PurchaseReturnContract.Event.DateChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        date = event.date
                    )
                ) to null

            is PurchaseReturnContract.Event.PaymentTypeChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        paymentType = event.type
                    )
                ) to null

            is PurchaseReturnContract.Event.AmountPaidChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        amountPaid = event.amount
                    )
                ) to null

            is PurchaseReturnContract.Event.AddItem ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = previousState.currentReturnInput.items + EditableItem()
                    )
                ) to null

            is PurchaseReturnContract.Event.RemoveItem ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = previousState.currentReturnInput.items.filterNot { it.tempEditorId == event.editorId }
                    )
                ) to null

            is PurchaseReturnContract.Event.ItemStockChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        item.copy(currentStock = event.stock)
                    } else item
                }
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseReturnContract.Event.ItemProductChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) {
                        val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            product = event.product,
                            maxUnitPrice = event.product?.averagePrice.toString(),
                            minUnitPrice = (event.product?.averagePrice?.div(conversionFactor)).toString(),
                            minUnitQuantity = conversionFactor.toString(),
                            maxUnitQuantity = "1.0",
                        )
                    } else item
                }
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseReturnContract.Event.ItemMaxPriceChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
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
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseReturnContract.Event.ItemMinPriceChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
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
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseReturnContract.Event.ItemUnitChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) item.copy(isSelectedUnitIsMax = event.isMaxUnit) else item
                }
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseReturnContract.Event.ItemMaxQuantityChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
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
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseReturnContract.Event.ItemMinQuantityChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
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
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is PurchaseReturnContract.Event.BackClicked,
            is PurchaseReturnContract.Event.SaveClicked,
            is PurchaseReturnContract.Event.DeleteClicked -> previousState to null
        }
    }
}
