package com.wael.astimal.pos.features.inventory.presentation.stock_management

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason

class StockManagementReducer :
    Reducer<StockManagementContract.State, StockManagementContract.Event, Nothing> {
    override fun reduce(
        previousState: StockManagementContract.State, event: StockManagementContract.Event
    ): Pair<StockManagementContract.State, Nothing?> {
        return when (event) {
            is StockManagementContract.Event.LoadInitialData -> previousState.copy(isLoading = true) to null

            is StockManagementContract.Event.SearchQueryChanged -> previousState.copy(query = event.query) to null

            is StockManagementContract.Event.UserLoaded -> previousState.copy(currentUser = event.user) to null

            is StockManagementContract.Event.StoresLoaded -> previousState.copy(stores = event.stores) to null

            is StockManagementContract.Event.StocksAdjustmentLoaded -> previousState.copy(
                isLoading = false, stockAdjustments = event.stockAdjustments
            ) to null

            is StockManagementContract.Event.ProductsLoaded -> previousState.copy(products = event.products) to null

            is StockManagementContract.Event.SelectedAdjustmentChanged -> previousState.copy(
                isSearchActive = false,
                selectedAdjustment = event.adjustment,
                adjustmentStore = event.adjustment?.store,
                adjustmentProduct = event.adjustment?.product,
                adjustmentNotes = event.adjustment?.notes ?: "",
                adjustmentReason = event.adjustment?.reason ?: StockAdjustmentReason.RECOUNT,
                adjustmentQuantityChange = event.adjustment?.quantityChange.toString(),
                isLoading = false
            ) to null

            is StockManagementContract.Event.AdjustmentQuantityChanged -> previousState.copy(
                adjustmentQuantityChange = event.quantity
            ) to null

            is StockManagementContract.Event.AdjustmentReasonChanged -> previousState.copy(
                adjustmentReason = event.reason
            ) to null

            is StockManagementContract.Event.AdjustmentNotesChanged -> previousState.copy(
                adjustmentNotes = event.notes
            ) to null

            is StockManagementContract.Event.AdjustmentProductChanged -> previousState.copy(
                adjustmentProduct = event.product
            ) to null

            is StockManagementContract.Event.AdjustmentStoreChanged -> previousState.copy(
                adjustmentStore = event.store
            ) to null

            is StockManagementContract.Event.SearchActiveChanged -> previousState.copy(
                isSearchActive = event.isActive
            ) to null

            is StockManagementContract.Event.NewStockAdjustmentClicked -> previousState.copy(
                isSearchActive = false,
                adjustmentQuantityChange = "",
                adjustmentReason = StockAdjustmentReason.RECOUNT,
                adjustmentNotes = "",
                adjustmentStore = null,
                adjustmentProduct = null,
                selectedAdjustment = null
            ) to null

            is StockManagementContract.Event.SaveClicked, is StockManagementContract.Event.DeleteClicked, is StockManagementContract.Event.NavigateBack, is StockManagementContract.Event.AdjustmentSucceeded, is StockManagementContract.Event.AdjustmentFailed -> previousState to null
        }
    }
}
