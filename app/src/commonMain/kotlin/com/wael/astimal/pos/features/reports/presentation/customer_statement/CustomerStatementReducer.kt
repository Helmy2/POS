package com.wael.astimal.pos.features.reports.presentation.customer_statement

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime

class CustomerStatementReducer :
    Reducer<CustomerStatementReducer.State, CustomerStatementReducer.Event, CustomerStatementReducer.Effect> {
    data class State @OptIn(ExperimentalTime::class) constructor(
        val partners: List<BusinessPartner> = emptyList(),
        val transactions: List<DetailedTransaction> = emptyList(),
        val selectedPartner: BusinessPartner? = null,
        val startDate: LocalDateTime = Clock.currentLocalDateTime(),
        val endDate: LocalDateTime = Clock.currentLocalDateTime(),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val partners: List<BusinessPartner>) : Event
        data class SelectPartner(val partner: BusinessPartner?) : Event
        data class SetStartDate(val date: LocalDateTime) : Event
        data class SetEndDate(val date: LocalDateTime) : Event
        data object ApplyFilters : Event
        data class ShowTransactions(val transactions: List<DetailedTransaction>) : Event
        data class TransactionClicked(val transaction: DetailedTransaction) : Event
        data object GeneratePdf : Event
        data class PdfGenerationFinished(val message: String) : Event
        data class PdfGenerationSuccessful(val html: String) : Event
    }

    // Add an empty Effect sealed interface to match the BaseViewModel
    sealed interface Effect : Reducer.ViewEffect
    override fun reduce(
        previousState: CustomerStatementReducer.State,
        event: CustomerStatementReducer.Event
    ): Pair<CustomerStatementReducer.State, CustomerStatementReducer.Effect?> {
        return when (event) {
            is CustomerStatementReducer.Event.ShowInitialData -> {
                previousState.copy(partners = event.partners) to null
            }

            is CustomerStatementReducer.Event.SelectPartner -> {
                previousState.copy(selectedPartner = event.partner) to null
            }

            is CustomerStatementReducer.Event.SetStartDate -> {
                previousState.copy(startDate = event.date) to null
            }

            is CustomerStatementReducer.Event.SetEndDate -> {
                previousState.copy(endDate = event.date) to null
            }

            is CustomerStatementReducer.Event.ApplyFilters -> {
                previousState.copy(isLoading = true, transactions = emptyList()) to null
            }

            is CustomerStatementReducer.Event.ShowTransactions -> {
                previousState.copy(isLoading = false, transactions = event.transactions) to null
            }

            is CustomerStatementReducer.Event.PdfGenerationFinished -> {
                previousState.copy(pdfHtmlToGenerate = null) to null
            }

            is CustomerStatementReducer.Event.PdfGenerationSuccessful -> {
                previousState.copy(pdfHtmlToGenerate = event.html) to null
            }

            else -> previousState to null
        }
    }
}
