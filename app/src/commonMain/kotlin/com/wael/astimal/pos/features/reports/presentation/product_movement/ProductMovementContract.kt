package com.wael.astimal.pos.features.reports.presentation.product_movement

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementGroup
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

object ProductMovementContract {
    data class State(
        val products: List<Product> = emptyList(),
        val stores: List<Store> = emptyList(),
        val movementGroups: List<ProductMovementGroup> = emptyList(),
        val selectedProductId: String? = null,
        val selectedStoreId: String? = null,
        val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0,
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val products: List<Product>, val stores: List<Store>) : Event
        data class SelectProduct(val productId: String?) : Event
        data class SelectStore(val storeId: String?) : Event
        data class SetStartDate(val date: LocalDate) : Event
        data class SetEndDate(val date: LocalDate) : Event
        data object ApplyFilters : Event
        data class ShowMovement(val groups: List<ProductMovementGroup>) : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val pdfHtml: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}