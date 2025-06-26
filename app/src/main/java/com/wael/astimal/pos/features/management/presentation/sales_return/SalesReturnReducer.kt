package com.wael.astimal.pos.features.management.presentation.sales_return

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.EditableItem

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

            is SalesReturnContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is SalesReturnContract.Event.PartnerBalanceChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        partnerBalance = event.balance
                    )
                ) to null

            is SalesReturnContract.Event.UserLoaded ->
                previousState.copy(
                    currentUser = event.user,
                    currentReturnInput = previousState.currentReturnInput.copy(
                        selectedEmployee = event.user
                    )
                ) to null

            is SalesReturnContract.Event.DropdownDataLoaded ->
                previousState.copy(dropdownData = event.data) to null

            is SalesReturnContract.Event.ReturnsLoaded ->
                previousState.copy(isLoading = false, returns = event.returns) to null

            is SalesReturnContract.Event.ReturnSelected -> {
                val editableItems = event.salesReturn.items.map {
                    val conversionFactor = it.product.subUnitsPerMainUnit
                    EditableItem(
                        tempEditorId = it.id.local.toString(),
                        product = it.product,
                        maxUnitQuantity = it.quantity.toString(),
                        minUnitQuantity = (it.quantity * conversionFactor).toString(),
                        maxUnitPrice = it.priceAtReturn.toString(),
                        minUnitPrice = (it.priceAtReturn / conversionFactor).toString()
                    )
                }
                previousState.copy(
                    selectedReturn = event.salesReturn,
                    selectedClient = event.salesReturn.client,
                    currentReturnInput = SalesReturnContract.EditableReturn(
                        selectedEmployee = event.salesReturn.employee,
                        paymentType = event.salesReturn.paymentType,
                        createdAt = event.salesReturn.createdAt,
                        items = editableItems,
                        amountPaid = event.salesReturn.amountPaid.toString()
                    ),
                    isSearchActive = false
                ) to null
            }

            is SalesReturnContract.Event.NewReturnClicked,
            is SalesReturnContract.Event.SaveSucceeded,
            is SalesReturnContract.Event.DeleteSucceeded ->
                previousState.copy(
                    isLoading = false,
                    selectedReturn = null,
                    currentReturnInput = SalesReturnContract.EditableReturn(
                        createdAt = Clock.now(),
                        selectedEmployee = previousState.currentUser,
                    ),
                    selectedClient = null
                ) to null

            // Form input updates
            is SalesReturnContract.Event.ClientSelected ->
                previousState.copy(selectedClient = event.client) to null

            is SalesReturnContract.Event.EmployeeChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        selectedEmployee = event.employee
                    )
                ) to null

            is SalesReturnContract.Event.DateChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        createdAt = event.date
                    )
                ) to null

            is SalesReturnContract.Event.PaymentTypeChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        paymentType = event.type
                    )
                ) to null

            is SalesReturnContract.Event.AmountPaidChanged ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        amountPaid = event.amount
                    )
                ) to null

            is SalesReturnContract.Event.AddItem ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = previousState.currentReturnInput.items + EditableItem()
                    )
                ) to null

            is SalesReturnContract.Event.RemoveItem ->
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = previousState.currentReturnInput.items.filterNot { it.tempEditorId == event.editorId }
                    )
                ) to null

            is SalesReturnContract.Event.ItemProductChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
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
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.ItemMaxPriceChanged -> {
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

            is SalesReturnContract.Event.ItemMinPriceChanged -> {
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

            is SalesReturnContract.Event.ItemUnitChanged -> {
                val updatedItems = previousState.currentReturnInput.items.map { item ->
                    if (item.tempEditorId == event.editorId) item.copy(isSelectedUnitIsMax = event.isMaxUnit) else item
                }
                previousState.copy(
                    currentReturnInput = previousState.currentReturnInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is SalesReturnContract.Event.ItemMaxQuantityChanged -> {
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

            is SalesReturnContract.Event.ItemMinQuantityChanged -> {
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

            is SalesReturnContract.Event.BackClicked,
            is SalesReturnContract.Event.SaveClicked,
            is SalesReturnContract.Event.DeleteClicked -> previousState to null
        }
    }
}
