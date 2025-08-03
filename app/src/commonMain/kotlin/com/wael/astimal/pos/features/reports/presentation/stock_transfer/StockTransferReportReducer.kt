package com.wael.astimal.pos.features.reports.presentation.stock_transfer

import com.wael.astimal.pos.core.base.mvi.Reducer

class StockTransferReportReducer :
    Reducer<StockTransferReportContract.State, StockTransferReportContract.Event, StockTransferReportContract.Effect> {
    override fun reduce(
        previousState: StockTransferReportContract.State,
        event: StockTransferReportContract.Event
    ): Pair<StockTransferReportContract.State, StockTransferReportContract.Effect?> {
        return when (event) {
            is StockTransferReportContract.Event.ShowInitialData -> previousState.copy(stores = event.stores) to null
            is StockTransferReportContract.Event.SelectFromStore -> previousState.copy(
                selectedFromStoreId = event.storeId
            ) to null

            is StockTransferReportContract.Event.SelectToStore -> previousState.copy(
                selectedToStoreId = event.storeId
            ) to null

            is StockTransferReportContract.Event.SetStartDate -> previousState.copy(startDate = event.date) to null
            is StockTransferReportContract.Event.SetEndDate -> previousState.copy(endDate = event.date) to null
            is StockTransferReportContract.Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                transfers = emptyList()
            ) to null

            is StockTransferReportContract.Event.ShowTransfers -> previousState.copy(
                isLoading = false,
                transfers = event.transfers
            ) to null

            is StockTransferReportContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is StockTransferReportContract.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.html
            ) to null

            else -> previousState to null
        }
    }
}