package com.wael.astimal.pos.features.reports.presentation.current_stock

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.reports.domain.model.CurrentStockInfo

object CurrentStockContract {
    data class State(
        val products: List<Product> = emptyList(),
        val stores: List<Store> = emptyList(),
        val stockList: List<CurrentStockInfo> = emptyList(),
        val selectedProductId: String? = null,
        val selectedStoreId: String? = null,
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val products: List<Product>, val stores: List<Store>) : Event
        data class SelectProduct(val productId: String?) : Event
        data class SelectStore(val storeId: String?) : Event
        data object ApplyFilters : Event
        data class ShowStockList(val stockList: List<CurrentStockInfo>) : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val pdfHtml: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}

