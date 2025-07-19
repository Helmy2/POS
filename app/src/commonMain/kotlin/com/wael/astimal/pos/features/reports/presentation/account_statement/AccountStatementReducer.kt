package com.wael.astimal.pos.features.reports.presentation.account_statement

import com.wael.astimal.pos.core.base.mvi.Reducer

class AccountStatementReducer :
    Reducer<AccountStatementContract.State, AccountStatementContract.Event, AccountStatementContract.Effect> {
    override fun reduce(
        previousState: AccountStatementContract.State, event: AccountStatementContract.Event
    ): Pair<AccountStatementContract.State, AccountStatementContract.Effect?> {
        return when (event) {
            is AccountStatementContract.Event.SearchQueryChanged -> previousState.copy(searchQuery = event.query) to null

            is AccountStatementContract.Event.PartnerSelected -> previousState.copy(selectedPartner = event.partner) to null

            is AccountStatementContract.Event.ClearPartnerSelection -> previousState.copy(
                selectedPartner = null, transactions = emptyList(), isStatementLoading = false
            ) to null

            is AccountStatementContract.Event.PartnerListLoading -> previousState.copy(
                isPartnerListLoading = true
            ) to null

            is AccountStatementContract.Event.PartnersLoaded -> previousState.copy(
                isPartnerListLoading = false, partners = event.partners
            ) to null

            is AccountStatementContract.Event.StatementLoading -> previousState.copy(
                isStatementLoading = true
            ) to null

            is AccountStatementContract.Event.StatementLoaded -> previousState.copy(
                isStatementLoading = false, transactions = event.transactions
            ) to null

            is AccountStatementContract.Event.PdfGenerationFinished -> {
                previousState.copy(
                    selectedPartner = null, transactions = emptyList(), isStatementLoading = false,
                    pdfHtmlToGenerate = null, isPdfGenerating = false
                ) to null
            }

            is AccountStatementContract.Event.PdfGenerationSuccessFul -> {
                previousState.copy(pdfHtmlToGenerate = event.html, isPdfGenerating = false) to null
            }

            is AccountStatementContract.Event.IsPdfGeneratingChanged -> {
                previousState.copy(isPdfGenerating = event.isGenerating) to null
            }

            is AccountStatementContract.Event.ExportToPdfClicked, is AccountStatementContract.Event.NavigateBack -> previousState to null

        }
    }
}
