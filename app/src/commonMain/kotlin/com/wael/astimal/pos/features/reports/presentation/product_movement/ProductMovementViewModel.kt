package com.wael.astimal.pos.features.reports.presentation.product_movement

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductMovementViewModel(
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val reportRepository: ReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator,
) : BaseViewModel<ProductMovementReducer.State, ProductMovementReducer.Event, ProductMovementReducer.Effect>(
    ProductMovementReducer(),
    ProductMovementReducer.State()
) {
    private var job: Job? = null

    init {
        processEvent(ProductMovementReducer.Event.LoadInitialData)
    }

    override fun handleEvent(event: ProductMovementReducer.Event) {
        when (event) {
            is ProductMovementReducer.Event.LoadInitialData -> loadFilters()
            is ProductMovementReducer.Event.ApplyFilters -> {
                setState(event)
                loadMovement()
            }

            is ProductMovementReducer.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }
            else -> setState(event)
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            val products = productRepository.getProducts().first()
            val stores = storeRepository.getStores().first()
            setState(ProductMovementReducer.Event.ShowInitialData(products, stores))
        }
    }

    private fun loadMovement() {
        job?.cancel()
        val currentState = state.value
        job = viewModelScope.launch {
            reportRepository.getProductMovement(
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                productId = currentState.selectedProduct?.id,
                storeId = currentState.selectedStore?.id
            ).collect { groups ->
                setState(ProductMovementReducer.Event.ShowMovement(groups))
            }
        }
    }

    private suspend fun generatePdf() {
        val currentState = state.value
        val html = htmlReportGenerator.createProductMovementReportHtml(
            groups = currentState.movementGroups,
            startDate = currentState.startDate,
            endDate = currentState.endDate
        )

        setState(ProductMovementReducer.Event.PdfGenerationSuccess(html))
    }
}