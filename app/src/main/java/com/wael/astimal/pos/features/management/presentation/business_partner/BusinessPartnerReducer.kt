package com.wael.astimal.pos.features.management.presentation.business_partner

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.user.domain.entity.User

class BusinessPartnerReducer :
    Reducer<BusinessPartnerContract.State, BusinessPartnerContract.Event, Nothing> {
    override fun reduce(
        previousState: BusinessPartnerContract.State,
        event: BusinessPartnerContract.Event
    ): Pair<BusinessPartnerContract.State, Nothing?> {
        return when (event) {
            is BusinessPartnerContract.Event.LoadInitialData ->
                previousState.copy(isLoading = true) to null

            is BusinessPartnerContract.Event.UserLoaded ->
                previousState.copy(currentUser = event.user) to null

            is BusinessPartnerContract.Event.PartnersLoaded ->
                previousState.copy(isLoading = false, partners = event.partners) to null

            is BusinessPartnerContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is BusinessPartnerContract.Event.PartnerClicked ->
                previousState.copy(
                    dialog = BusinessPartnerContract.Dialog.Details(event.partner),
                    isLoading = false
                ) to null

            is BusinessPartnerContract.Event.AddNewPartnerClicked -> {
                val previousUser = previousState.currentUser
                if (previousUser == null) {
                    return previousState to null
                }
                val newPartner = createBlankBusinessPartner(previousState.currentUser)
                previousState.copy(
                    dialog = BusinessPartnerContract.Dialog.Edit(newPartner),
                    isLoading = false
                ) to null
            }

            is BusinessPartnerContract.Event.EditPartnerClicked ->
                previousState.copy(
                    dialog = BusinessPartnerContract.Dialog.Edit(event.partner),
                    isLoading = false
                ) to null

            is BusinessPartnerContract.Event.DismissDialog,
            is BusinessPartnerContract.Event.SaveSucceeded ->
                previousState.copy(
                    dialog = BusinessPartnerContract.Dialog.None,
                    isLoading = false
                ) to null

            is BusinessPartnerContract.Event.BackClicked,
            is BusinessPartnerContract.Event.DeletePartnerClicked,
            is BusinessPartnerContract.Event.SaveChangesClicked -> previousState to null
        }
    }

    private fun createBlankBusinessPartner(currentUser: User): BusinessPartner {
        return BusinessPartner(
            clientId = null,
            supplierId = null,
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
}
