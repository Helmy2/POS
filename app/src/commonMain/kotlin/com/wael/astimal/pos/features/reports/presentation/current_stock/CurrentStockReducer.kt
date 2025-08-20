package com.wael.astimal.pos.features.reports.presentation.current_stock

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.reports.domain.model.CurrentStockInfo

class CurrentStockReducer :
    Reducer<CurrentStockReducer.State, CurrentStockReducer.Event, CurrentStockReducer.Effect> {
    data class State(
        val products: List<Product> = emptyList(),
        val stores: List<Store> = emptyList(),
        val stockList: List<CurrentStockInfo> = emptyList(),
        val selectedProduct: Product? = null,
        val selectedStore: Store? = null,
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val products: List<Product>, val stores: List<Store>) : Event
        data class SelectProduct(val product: Product?) : Event
        data class SelectStore(val store: Store?) : Event
        data object ApplyFilters : Event
        data class ShowStockList(val stockList: List<CurrentStockInfo>) : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val pdfHtml: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
    
    override fun reduce(
        previousState: State,
        event: Event
    ): Pair<State, Effect?> {
        return when (event) {
            is Event.ShowInitialData -> previousState.copy(
                products = event.products,
                stores = event.stores
            ) to null

            is Event.SelectProduct -> previousState.copy(selectedProduct = event.product) to null
            is Event.SelectStore -> previousState.copy(selectedStore = event.store) to null
            is Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                stockList = emptyList()
            ) to null

            is Event.ShowStockList -> previousState.copy(
                isLoading = false,
                stockList = event.stockList
            ) to null

            is Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.pdfHtml
            ) to null

            else -> previousState to null
        }
    }
}