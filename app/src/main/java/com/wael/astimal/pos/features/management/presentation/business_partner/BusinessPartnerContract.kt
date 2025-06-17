package com.wael.astimal.pos.features.management.presentation.business_partner

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType

data class BusinessPartnerInfoState(
    val loading: Boolean = false,
    val searchResults: List<BusinessPartner> = emptyList(),
    val selectedBusinessPartner: BusinessPartner? = null,
    val query: String = "",
    val showDetailDialog: Boolean = false,
    val isAdmin: Boolean = false,
    val showEditDialog: Boolean = false,
    val partnerToEdit: BusinessPartner? = null,
    val isSaving: Boolean = false
)

sealed interface BusinessPartnerInfoEvent {
    data class SearchBusinessPartners(val query: String) : BusinessPartnerInfoEvent
    data class SelectBusinessPartner(val businessPartner: BusinessPartner?) : BusinessPartnerInfoEvent
    data class UpdateQuery(val query: String) : BusinessPartnerInfoEvent
    data object AddNewPartnerClicked : BusinessPartnerInfoEvent
    data class EditPartnerClicked(val partner: BusinessPartner) : BusinessPartnerInfoEvent
    data class DeletePartnerClicked(val partner: BusinessPartner) : BusinessPartnerInfoEvent
    data object DismissEditDialog : BusinessPartnerInfoEvent
    data object DismissDetailDialog : BusinessPartnerInfoEvent

    data class SavePartnerClicked(
        val partner: BusinessPartner,
        val openingDebt: Double = 0.0,
        val openingIndebtedness: Double = 0.0
    ) : BusinessPartnerInfoEvent
}

fun createBlankBusinessPartner(): BusinessPartner {
    return BusinessPartner(
        clientLocalId = null,
        supplierLocalId = null,
        name = LocalizedString(),
        address = "",
        phone = "",
        responsibleEmployee = null,
        type = PartnerType.CLIENT,
        clientDebt = 0.0,
        supplierIndebtedness = 0.0,
        isSynced = false
    )
}