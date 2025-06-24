package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BusinessPartnerViewModel(
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController
) : BaseViewModel<BusinessPartnerContract.State, BusinessPartnerContract.Event, Nothing>(
    reducer = BusinessPartnerReducer(),
    initialState = BusinessPartnerContract.State()
) {
    init {
        processEvent(BusinessPartnerContract.Event.LoadInitialData)
    }

    val filteredPartnersState: StateFlow<List<BusinessPartner>> =
        combine(
            state,
            businessPartnerRepository.getBusinessPartners("")
        ) { state, allPartners ->
            if (state.searchQuery.isBlank()) {
                allPartners
            } else {
                allPartners.filter { it.name.contains(state.searchQuery) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override fun handleEvent(event: BusinessPartnerContract.Event) {
        when (event) {
            is BusinessPartnerContract.Event.LoadInitialData -> loadCurrentUser()
            is BusinessPartnerContract.Event.SaveChangesClicked -> savePartner(
                event.partner,
                event.openingDebt,
                event.openingIndebtedness
            )

            is BusinessPartnerContract.Event.DeletePartnerClicked -> deletePartner(event.partner)
            is BusinessPartnerContract.Event.BackClicked -> navigateBack()
            else -> setState(event)
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            setState(BusinessPartnerContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
    }

    private fun savePartner(
        partner: BusinessPartner,
        openingDebt: Double,
        openingIndebtedness: Double
    ) {
        viewModelScope.launch {
            val finalPartner = partner.copy(
                clientDebt = openingDebt,
                supplierIndebtedness = openingIndebtedness
            )
            businessPartnerRepository.saveBusinessPartner(finalPartner).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.partner_saved_successfully)))
                setState(BusinessPartnerContract.Event.SaveSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_saving_partner)))
            }
        }
    }

    private fun deletePartner(partner: BusinessPartner) {
        viewModelScope.launch {
            businessPartnerRepository.deleteBusinessPartner(partner).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.partner_deleted_successfully)))
                // Close the dialog after deletion
                setState(BusinessPartnerContract.Event.DismissDialog)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_deleting_partner)))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}
