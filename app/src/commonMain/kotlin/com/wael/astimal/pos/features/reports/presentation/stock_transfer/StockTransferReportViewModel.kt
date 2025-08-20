package com.wael.astimal.pos.features.reports.presentation.stock_transfer

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StockTransferReportViewModel(
    private val storeRepository: StoreRepository,
    private val reportRepository: ReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator
) : BaseViewModel<StockTransferReportReducer.State, StockTransferReportReducer.Event, StockTransferReportReducer.Effect>(
    StockTransferReportReducer(),
    StockTransferReportReducer.State()
) {
    private var job: Job? = null

    init {
        processEvent(StockTransferReportReducer.Event.LoadInitialData)
    }

    override fun handleEvent(event: StockTransferReportReducer.Event) {
        when (event) {
            is StockTransferReportReducer.Event.LoadInitialData -> loadStores()
            is StockTransferReportReducer.Event.ApplyFilters -> {
                setState(event)
                loadTransfers()
            }

            is StockTransferReportReducer.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }
            else -> setState(event)
        }
    }

    private fun loadStores() {
        viewModelScope.launch {
            val stores = storeRepository.getStores().first()
            setState(StockTransferReportReducer.Event.ShowInitialData(stores))
        }
    }

    private fun loadTransfers() {
        job?.cancel()
        val currentState = state.value
        job = viewModelScope.launch {
            reportRepository.getStockTransfers(
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                fromStoreId = currentState.selectedFromStore?.id,
                toStoreId = currentState.selectedToStore?.id
            ).collect { transfers ->
                setState(StockTransferReportReducer.Event.ShowTransfers(transfers))
            }
        }
    }

    private suspend fun generatePdf() {
        val currentState = state.value
        val html = htmlReportGenerator.createStockTransferReportHtml(
            transfers = currentState.transfers,
            startDate = currentState.startDate,
            endDate = currentState.endDate
        )

        setState(StockTransferReportReducer.Event.PdfGenerationSuccess(html))
    }
}