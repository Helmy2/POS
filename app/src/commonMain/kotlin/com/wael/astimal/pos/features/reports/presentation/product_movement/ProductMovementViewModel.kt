package com.wael.astimal.pos.features.reports.presentation.product_movement

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.reports.domain.repository.ProductMovementRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductMovementViewModel(
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val movementRepository: ProductMovementRepository,
    private val htmlReportGenerator: HtmlReportGenerator,
) : BaseViewModel<ProductMovementContract.State, ProductMovementContract.Event, ProductMovementContract.Effect>(
    ProductMovementReducer(),
    ProductMovementContract.State()
) {
    private var job: Job? = null

    init {
        processEvent(ProductMovementContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: ProductMovementContract.Event) {
        when (event) {
            is ProductMovementContract.Event.LoadInitialData -> loadFilters()
            is ProductMovementContract.Event.ApplyFilters -> {
                setState(event)
                loadMovement()
            }

            is ProductMovementContract.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }
            else -> setState(event)
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            val products = productRepository.getProducts().first()
            val stores = storeRepository.getStores().first()
            setState(ProductMovementContract.Event.ShowInitialData(products, stores))
        }
    }

    private fun loadMovement() {
        job?.cancel()
        val currentState = state.value
        job = viewModelScope.launch {
            movementRepository.getProductMovement(
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                productId = currentState.selectedProductId,
                storeId = currentState.selectedStoreId
            ).collect { groups ->
                setState(ProductMovementContract.Event.ShowMovement(groups))
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

        setState(ProductMovementContract.Event.PdfGenerationSuccess(html))
    }
}