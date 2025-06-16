package com.wael.astimal.pos.features.reports.presentation.account_statement

import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.entity.AccountTransaction


data class AccountStatementState(
    val isPartnerListLoading: Boolean = true,
    val partners: List<BusinessPartner> = emptyList(),
    val searchQuery: String = "",

    val selectedPartner: BusinessPartner? = null,
    val isStatementLoading: Boolean = false,
    val transactions: List<AccountTransaction> = emptyList()
)

sealed interface AccountStatementEvent {
    data class SearchPartner(val query: String) : AccountStatementEvent

    data class SelectPartner(val partner: BusinessPartner) : AccountStatementEvent

    data object ClearPartnerSelection : AccountStatementEvent
}
