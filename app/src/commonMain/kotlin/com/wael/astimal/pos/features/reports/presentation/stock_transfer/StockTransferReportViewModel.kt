package com.wael.astimal.pos.features.reports.presentation.stock_transfer

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.reports.domain.repository.StockTransferReportRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StockTransferReportViewModel(
    private val storeRepository: StoreRepository,
    private val reportRepository: StockTransferReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator
) : BaseViewModel<StockTransferReportContract.State, StockTransferReportContract.Event, StockTransferReportContract.Effect>(
    StockTransferReportReducer(),
    StockTransferReportContract.State()
) {
    private var job: Job? = null

    init {
        processEvent(StockTransferReportContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: StockTransferReportContract.Event) {
        when (event) {
            is StockTransferReportContract.Event.LoadInitialData -> loadStores()
            is StockTransferReportContract.Event.ApplyFilters -> {
                setState(event)
                loadTransfers()
            }

            is StockTransferReportContract.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }
            else -> setState(event)
        }
    }

    private fun loadStores() {
        viewModelScope.launch {
            val stores = storeRepository.getStores().first()
            setState(StockTransferReportContract.Event.ShowInitialData(stores))
        }
    }

    private fun loadTransfers() {
        job?.cancel()
        val currentState = state.value
        job = viewModelScope.launch {
            reportRepository.getStockTransfers(
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                fromStoreId = currentState.selectedFromStoreId,
                toStoreId = currentState.selectedToStoreId
            ).collect { transfers ->
                setState(StockTransferReportContract.Event.ShowTransfers(transfers))
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

        setState(StockTransferReportContract.Event.PdfGenerationSuccess(html))
    }
}