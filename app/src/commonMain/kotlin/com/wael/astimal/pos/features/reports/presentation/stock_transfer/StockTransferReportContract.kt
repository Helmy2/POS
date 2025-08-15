package com.wael.astimal.pos.features.reports.presentation.stock_transfer

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object StockTransferReportContract {
    data class State(
        val stores: List<Store> = emptyList(),
        val transfers: List<StockTransfer> = emptyList(),
        val selectedFromStoreId: String? = null,
        val selectedToStoreId: String? = null,
        val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val stores: List<Store>) : Event
        data class SelectFromStore(val storeId: String?) : Event
        data class SelectToStore(val storeId: String?) : Event
        data class SetStartDate(val date: LocalDate) : Event
        data class SetEndDate(val date: LocalDate) : Event
        data object ApplyFilters : Event
        data class ShowTransfers(val transfers: List<StockTransfer>) : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val html: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}

