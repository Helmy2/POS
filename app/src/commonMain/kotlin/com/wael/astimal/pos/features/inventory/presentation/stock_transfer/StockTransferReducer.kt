package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock

class StockTransferReducer() :
    Reducer<StockTransferContract.State, StockTransferContract.Event, Nothing> {
    override fun reduce(
        previousState: StockTransferContract.State,
        event: StockTransferContract.Event
    ): Pair<StockTransferContract.State, Nothing?> {
        return when (event) {
            is StockTransferContract.Event.ItemProductChanged -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) {
                        val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            product = event.product,
                            maxUnitQuantity = "1.0",
                            minUnitQuantity = conversionFactor.toString(),
                            isSelectedUnitMax = true
                        )
                    } else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is StockTransferContract.Event.ItemUnitChanged -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) item.copy(isSelectedUnitMax = event.isMaxUnit) else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is StockTransferContract.Event.ItemMaxQuantityChanged -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        val maxQty = event.quantity.toDoubleOrNull() ?: 0.0
                        item.copy(
                            maxUnitQuantity = event.quantity,
                            minUnitQuantity = (maxQty * conversionFactor).toString()
                        )
                    } else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is StockTransferContract.Event.ItemMinQuantityChanged -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        val minQty = event.quantity.toDoubleOrNull() ?: 0.0
                        item.copy(
                            minUnitQuantity = event.quantity,
                            maxUnitQuantity = if (conversionFactor > 0) (minQty / conversionFactor).toString() else "0.0"
                        )
                    } else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            // ... other reducer cases remain the same

            is StockTransferContract.Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is StockTransferContract.Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is StockTransferContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is StockTransferContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is StockTransferContract.Event.UserLoaded ->
                previousState.copy(
                    currentUser = event.user,
                    currentTransferInput = previousState.currentTransferInput.copy(
                        selectedEmployee = event.user,
                        fromStore = if (previousState.currentUser?.isAdmin == false) event.fromStore else previousState.currentTransferInput.fromStore
                    )
                ) to null

            is StockTransferContract.Event.DropdownDataLoaded ->
                previousState.copy(dropdownData = event.data) to null

            is StockTransferContract.Event.TransfersLoaded ->
                previousState.copy(isLoading = false, transfers = event.transfers) to null

            is StockTransferContract.Event.TransferSelected -> {
                val editableItems = event.transfer.items.map {
                    StockTransferContract.EditableStockTransferItem(
                        editorId = it.id,
                        product = it.product,
                        maxUnitQuantity = it.quantity.toString(),
                        minUnitQuantity = (it.quantity * it.product.subUnitsPerMainUnit).toString()
                    )
                }
                previousState.copy(
                    selectedTransfer = event.transfer,
                    currentTransferInput = StockTransferContract.EditableStockTransfer(
                        fromStore = event.transfer.fromStore,
                        toStore = event.transfer.toStore,
                        selectedEmployee = event.transfer.initiatingUser,
                        transferDate = event.transfer.createdAt,
                        items = editableItems
                    ),
                    isSearchActive = false
                ) to null
            }

            is StockTransferContract.Event.NewTransferClicked,
            is StockTransferContract.Event.SaveSucceeded,
            is StockTransferContract.Event.DeleteSucceeded ->
                previousState.copy(
                    isLoading = false,
                    selectedTransfer = null,
                    currentTransferInput = StockTransferContract.EditableStockTransfer(
                        transferDate = Clock.now(),
                        selectedEmployee = previousState.currentUser,
                        fromStore = if (previousState.currentUser?.isAdmin == false) previousState.currentTransferInput.fromStore else null
                    )
                ) to null

            // Form input updates
            is StockTransferContract.Event.FromStoreChanged ->
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        fromStore = event.store
                    )
                ) to null

            is StockTransferContract.Event.ToStoreChanged ->
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        toStore = event.store
                    )
                ) to null

            is StockTransferContract.Event.EmployeeChanged ->
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        selectedEmployee = event.employee
                    )
                ) to null

            is StockTransferContract.Event.DateChanged ->
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        transferDate = event.date
                    )
                ) to null

            is StockTransferContract.Event.AddItem -> {
                val newItems =
                    previousState.currentTransferInput.items + StockTransferContract.EditableStockTransferItem()
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = newItems
                    )
                ) to null
            }

            is StockTransferContract.Event.RemoveItem -> {
                val updatedItems =
                    previousState.currentTransferInput.items.filterNot { it.editorId == event.editorId }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is StockTransferContract.Event.StockForItemSelected -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) item.copy(currentMaxStock = event.stock) else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is StockTransferContract.Event.BackClicked,
            is StockTransferContract.Event.SaveClicked,
            is StockTransferContract.Event.DeleteClicked,
            is StockTransferContract.Event.ApprovedClicked,
            is StockTransferContract.Event.RejectedClicked -> previousState to null
        }
    }
}
