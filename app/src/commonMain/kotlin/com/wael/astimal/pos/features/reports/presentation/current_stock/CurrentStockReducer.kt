package com.wael.astimal.pos.features.reports.presentation.current_stock

import com.wael.astimal.pos.core.base.mvi.Reducer

class CurrentStockReducer :
    Reducer<CurrentStockContract.State, CurrentStockContract.Event, CurrentStockContract.Effect> {
    override fun reduce(
        previousState: CurrentStockContract.State,
        event: CurrentStockContract.Event
    ): Pair<CurrentStockContract.State, CurrentStockContract.Effect?> {
        return when (event) {
            is CurrentStockContract.Event.ShowInitialData -> previousState.copy(
                products = event.products,
                stores = event.stores
            ) to null

            is CurrentStockContract.Event.SelectProduct -> previousState.copy(selectedProductId = event.productId) to null
            is CurrentStockContract.Event.SelectStore -> previousState.copy(selectedStoreId = event.storeId) to null
            is CurrentStockContract.Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                stockList = emptyList()
            ) to null

            is CurrentStockContract.Event.ShowStockList -> previousState.copy(
                isLoading = false,
                stockList = event.stockList
            ) to null

            is CurrentStockContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is CurrentStockContract.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.pdfHtml
            ) to null

            else -> previousState to null
        }
    }
}