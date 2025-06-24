package com.wael.astimal.pos.features.reports.presentation.account_statement

import android.net.Uri
import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.management.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner

object AccountStatementContract {

    data class State(
        val selectedPartner: BusinessPartner? = null,
        val searchQuery: String = "",
        val isPartnerListLoading: Boolean = true,
        val partners: List<BusinessPartner> = emptyList(),
        val isStatementLoading: Boolean = false,
        val transactions: List<AccountTransaction> = emptyList()
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data class SearchQueryChanged(val query: String) : Event
        data class PartnerSelected(val partner: BusinessPartner) : Event
        data object ClearPartnerSelection : Event
        data object ExportToPdfClicked : Event
        data class PartnersLoaded(val partners: List<BusinessPartner>) : Event
        data class StatementLoaded(val transactions: List<AccountTransaction>) : Event
        data object StatementLoading : Event
        data object PartnerListLoading : Event
        data class GenerateStatementPdfSuccessfully(val fileUri: Uri) : Event

        data object NavigateBack : Event
    }

    sealed interface Effect : Reducer.ViewEffect {
        data class SharePdf(val fileUri: Uri) : Effect
    }
}
