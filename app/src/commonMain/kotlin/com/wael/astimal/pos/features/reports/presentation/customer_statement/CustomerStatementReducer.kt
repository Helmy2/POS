package com.wael.astimal.pos.features.reports.presentation.customer_statement

import com.wael.astimal.pos.core.base.mvi.Reducer

class CustomerStatementReducer :
    Reducer<CustomerStatementContract.State, CustomerStatementContract.Event, CustomerStatementContract.Effect> {

    override fun reduce(
        previousState: CustomerStatementContract.State,
        event: CustomerStatementContract.Event
    ): Pair<CustomerStatementContract.State, CustomerStatementContract.Effect?> {
        return when (event) {
            is CustomerStatementContract.Event.ShowInitialData -> {
                previousState.copy(partners = event.partners) to null
            }

            is CustomerStatementContract.Event.SelectPartner -> {
                previousState.copy(selectedPartner = event.partner) to null
            }

            is CustomerStatementContract.Event.SetStartDate -> {
                previousState.copy(startDate = event.date) to null
            }

            is CustomerStatementContract.Event.SetEndDate -> {
                previousState.copy(endDate = event.date) to null
            }

            is CustomerStatementContract.Event.ApplyFilters -> {
                previousState.copy(isLoading = true, transactions = emptyList()) to null
            }

            is CustomerStatementContract.Event.ShowTransactions -> {
                previousState.copy(isLoading = false, transactions = event.transactions) to null
            }

            is CustomerStatementContract.Event.PdfGenerationFinished -> {
                previousState.copy(pdfHtmlToGenerate = null) to null
            }

            is CustomerStatementContract.Event.PdfGenerationSuccessful -> {
                previousState.copy(pdfHtmlToGenerate = event.html) to null
            }

            else -> previousState to null
        }
    }
}
