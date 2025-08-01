package com.wael.astimal.pos.features.management.presentation.business_partner

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.user.domain.entity.User

object BusinessPartnerContract {

    sealed interface Dialog {
        data object None : Dialog
        data class Details(val partner: BusinessPartner) : Dialog
        data class Edit(val partner: BusinessPartner) : Dialog
    }

    data class State(
        val isLoading: Boolean = true,
        val currentUser: User? = null,
        val partners: List<BusinessPartner> = emptyList(),
        val userDropdownData: List<User> = emptyList(),
        val searchQuery: String = "",
        val dialog: Dialog = Dialog.None
    ) : Reducer.ViewState {
        val canUserEdit: Boolean get() = true
        val isAdmin: Boolean get() = currentUser?.isAdmin == true
        val filteredPartners
            get() = partners.filter {
                it.name.contains(searchQuery)
            }
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class LoadInitialData(val isOpenNew: Boolean) : Event
        data class SearchQueryChanged(val query: String) : Event
        data class PartnerClicked(val partner: BusinessPartner) : Event
        data object AddNewPartnerClicked : Event
        data class EditPartnerClicked(val partner: BusinessPartner) : Event
        data class DeletePartnerClicked(val partner: BusinessPartner) : Event
        data class CreateClicked(val partner: BusinessPartner, val amount: Double) : Event
        data class UpdateClicked(val partner: BusinessPartner) : Event

        data object DismissDialog : Event
        data object BackClicked : Event

        // Data results from ViewModel
        data class UserLoaded(val currentUser: User, val users: List<User>) : Event
        data class PartnersLoaded(val partners: List<BusinessPartner>) : Event
        data object SaveSucceeded : Event
    }
}
