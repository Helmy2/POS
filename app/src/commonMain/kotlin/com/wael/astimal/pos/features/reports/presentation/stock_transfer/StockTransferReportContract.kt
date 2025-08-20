package com.wael.astimal.pos.features.reports.presentation.stock_transfer

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object StockTransferReportContract {
    data class State(
        val stores: List<Store> = emptyList(),
        val transfers: List<StockTransfer> = emptyList(),
        val selectedFromStoreId: String? = null,
        val selectedToStoreId: String? = null,
        val startDate: LocalDateTime = Clock.currentLocalDateTime(),
        val endDate: LocalDateTime = Clock.currentLocalDateTime(),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val stores: List<Store>) : Event
        data class SelectFromStore(val storeId: String?) : Event
        data class SelectToStore(val storeId: String?) : Event
        data class SetStartDate(val date: LocalDateTime) : Event
        data class SetEndDate(val date: LocalDateTime) : Event
        data object ApplyFilters : Event
        data class ShowTransfers(val transfers: List<StockTransfer>) : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val html: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}

