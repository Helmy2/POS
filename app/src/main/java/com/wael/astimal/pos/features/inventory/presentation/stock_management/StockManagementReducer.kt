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

            is StockManagementContract.Event.StocksLoaded -> previousState.copy(
                isLoading = false,
                productBundles = event.stocks
                    .filter { it.quantity != 0.0 }
                    .groupBy {
                        it.store
                    }.map {
                        StockManagementContract.ProductBundle(
                            store = it.key,
                            quantities = it.value.map { stock ->
                                StockManagementContract.ProductQuantity(
                                    product = stock.product, quantity = stock.quantity
                                )
                            },
                        )
                    }) to null

            is StockManagementContract.Event.ProductsLoaded -> previousState.copy(products = event.products) to null

            is StockManagementContract.Event.ShowAdjustmentDialog -> previousState.copy(
                showAdjustmentDialog = true,
                adjustmentQuantityChange = "",
                adjustmentReason = StockAdjustmentReason.RECOUNT,
                adjustmentNotes = "",
                adjustmentStore = null,
                adjustmentProduct = null
            ) to null

            is StockManagementContract.Event.ShowAdjustmentDialogWithStore -> previousState.copy(
                showAdjustmentDialog = true,
                adjustmentQuantityChange = "",
                adjustmentReason = StockAdjustmentReason.RECOUNT,
                adjustmentNotes = "",
                adjustmentStore = event.store,
                adjustmentProduct = null
            ) to null

            is StockManagementContract.Event.DismissAdjustmentDialog, is StockManagementContract.Event.AdjustmentSucceeded -> previousState.copy(
                showAdjustmentDialog = false
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

            is StockManagementContract.Event.SaveAdjustmentClicked -> previousState to null

        }
    }
}
