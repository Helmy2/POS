package com.wael.astimal.pos.features.reports.presentation.stock_transfer

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import kotlinx.datetime.LocalDateTime

class StockTransferReportReducer :
    Reducer<StockTransferReportReducer.State, StockTransferReportReducer.Event, StockTransferReportReducer.Effect> {

    data class State(
        val stores: List<Store> = emptyList(),
        val transfers: List<StockTransfer> = emptyList(),
        val selectedFromStore: Store? = null,
        val selectedToStore: Store? = null,
        val startDate: LocalDateTime = Clock.getStartOfToday(),
        val endDate: LocalDateTime = Clock.getEndOfToday(),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val stores: List<Store>) : Event
        data class SelectFromStore(val store: Store?) : Event
        data class SelectToStore(val store: Store?) : Event
        data class SetStartDate(val date: LocalDateTime) : Event
        data class SetEndDate(val date: LocalDateTime) : Event
        data object ApplyFilters : Event
        data class ShowTransfers(val transfers: List<StockTransfer>) : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val html: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect

    override fun reduce(
        previousState: State,
        event: Event
    ): Pair<State, Effect?> {
        return when (event) {
            is Event.ShowInitialData -> previousState.copy(stores = event.stores) to null
            is Event.SelectFromStore -> previousState.copy(
                selectedFromStore = event.store
            ) to null

            is Event.SelectToStore -> previousState.copy(
                selectedToStore = event.store
            ) to null

            is Event.SetStartDate -> previousState.copy(startDate = event.date) to null
            is Event.SetEndDate -> previousState.copy(endDate = event.date) to null
            is Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                transfers = emptyList()
            ) to null

            is Event.ShowTransfers -> previousState.copy(
                isLoading = false,
                transfers = event.transfers
            ) to null

            is Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.html
            ) to null

            else -> previousState to null
        }
    }
}