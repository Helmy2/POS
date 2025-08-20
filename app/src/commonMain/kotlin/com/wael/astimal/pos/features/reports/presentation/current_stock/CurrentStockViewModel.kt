package com.wael.astimal.pos.features.reports.presentation.current_stock

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CurrentStockViewModel(
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val reportRepository: ReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator
) : BaseViewModel<CurrentStockContract.State, CurrentStockContract.Event, CurrentStockContract.Effect>(
    CurrentStockReducer(),
    CurrentStockContract.State()
) {
    private var job: Job? = null

    init {
        processEvent(CurrentStockContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: CurrentStockContract.Event) {
        when (event) {
            is CurrentStockContract.Event.LoadInitialData -> loadFilters()
            is CurrentStockContract.Event.ApplyFilters -> {
                setState(event)
                loadCurrentStock()
            }

            is CurrentStockContract.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }
            else -> setState(event)
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            val products = productRepository.getProducts().first()
            val stores = storeRepository.getStores().first()
            setState(CurrentStockContract.Event.ShowInitialData(products, stores))
        }
    }

    private fun loadCurrentStock() {
        job?.cancel()
        val currentState = state.value
        job = viewModelScope.launch {
            reportRepository.getCurrentStock(
                productId = currentState.selectedProductId,
                storeId = currentState.selectedStoreId
            ).collect { stockList ->
                setState(CurrentStockContract.Event.ShowStockList(stockList))
            }
        }
    }

    private suspend fun generatePdf() {
        val currentState = state.value
        val html = htmlReportGenerator.createCurrentStockReportHtml(
            stockList = currentState.stockList
        )

        setState(CurrentStockContract.Event.PdfGenerationSuccess(html))
    }
}