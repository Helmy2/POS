package com.wael.astimal.pos.features.management.presentation.business_partner

import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner

data class BusinessPartnerInfoState(
    val loading: Boolean = false,
    val searchResults: List<BusinessPartner> = emptyList(),
    val selectedBusinessPartner: BusinessPartner? = null,
    val query: String = "",
    val showDetailDialog: Boolean = false,
)

sealed interface BusinessPartnerInfoEvent {
    data class SearchBusinessPartners(val query: String) : BusinessPartnerInfoEvent
    data class SelectBusinessPartner(val businessPartner: BusinessPartner?) : BusinessPartnerInfoEvent
    data class UpdateQuery(val query: String) : BusinessPartnerInfoEvent
    data object DetailBusinessPartner : BusinessPartnerInfoEvent
    data object ShowDetailDialog : BusinessPartnerInfoEvent
}