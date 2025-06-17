package com.wael.astimal.pos.features.management.presentation.business_partner

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.user.domain.entity.User

data class BusinessPartnerInfoState(
    val currentUser: User? = null,
    val loading: Boolean = false,
    val searchResults: List<BusinessPartner> = emptyList(),
    val selectedBusinessPartner: BusinessPartner? = null,
    val query: String = "",
    val showDetailDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val partnerToEdit: BusinessPartner? = null,
    val isSaving: Boolean = false
) {
    val canEdit get() = currentUser?.isAdmin == true
}

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

fun createBlankBusinessPartner(currentUser: User): BusinessPartner {
    return BusinessPartner(
        clientLocalId = null,
        supplierLocalId = null,
        name = LocalizedString(),
        address = "",
        phone = "",
        responsibleEmployee = currentUser,
        type = PartnerType.CLIENT,
        clientDebt = 0.0,
        supplierIndebtedness = 0.0,
        isSynced = false
    )
}