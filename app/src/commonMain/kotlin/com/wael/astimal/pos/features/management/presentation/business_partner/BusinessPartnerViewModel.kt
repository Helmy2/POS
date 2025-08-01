package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.PartnerTransactionRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_deleting_partner
import pos.app.generated.resources.error_saving_partner
import pos.app.generated.resources.partner_deleted_successfully
import pos.app.generated.resources.partner_saved_successfully

class BusinessPartnerViewModel(
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val voucherRepository: PartnerTransactionRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController
) : BaseViewModel<BusinessPartnerContract.State, BusinessPartnerContract.Event, Nothing>(
    reducer = BusinessPartnerReducer(),
    initialState = BusinessPartnerContract.State()
) {

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
            is BusinessPartnerContract.Event.LoadInitialData -> {
                loadCurrentUser {
                    if (event.isOpenNew) {
                        setState(BusinessPartnerContract.Event.AddNewPartnerClicked)
                    }
                }
            }

            is BusinessPartnerContract.Event.CreateClicked -> createPartner(
                event.partner,
                event.amount
            )

            is BusinessPartnerContract.Event.UpdateClicked -> updatePartner(event.partner)

            is BusinessPartnerContract.Event.DeletePartnerClicked -> deletePartner(event.partner)
            is BusinessPartnerContract.Event.BackClicked -> navigateBack()
            else -> setState(event)
        }
    }

    private fun createPartner(
        partner: BusinessPartner,
        amount: Double
    ) {
        viewModelScope.launch {

            businessPartnerRepository.saveBusinessPartner(partner).onSuccess {
                val voucherToSave = ReceivePayVoucher(
                    id = "",
                    partner = partner.copy(id = it),
                    createdBy = state.value.currentUser!!,
                    amount = amount,
                    notes = "",
                    createdAt = Clock.now(),
                    invoiceId = null,
                    transactionType = TransactionType.OPENING_BALANCE
                )

                voucherRepository.saveVoucher(voucherToSave).onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.partner_saved_successfully)))
                    setState(BusinessPartnerContract.Event.SaveSucceeded)
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_saving_partner)))
                }
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_saving_partner)))
            }
        }
    }

    private fun loadCurrentUser(
        doAfterLoad: suspend () -> Unit
    ) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()!!
            val users = userRepository.getEmployeesFlow().first()
            setState(BusinessPartnerContract.Event.UserLoaded(currentUser, users))
            doAfterLoad()
        }
    }

    private fun updatePartner(partner: BusinessPartner) {
        viewModelScope.launch {
            businessPartnerRepository.saveBusinessPartner(partner).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.partner_saved_successfully)))
                setState(BusinessPartnerContract.Event.SaveSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_saving_partner)))
            }
        }
    }

    private fun deletePartner(partner: BusinessPartner) {
        viewModelScope.launch {
            businessPartnerRepository.deleteBusinessPartner(partner).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.partner_deleted_successfully)))
                // Close the dialog after deletion
                setState(BusinessPartnerContract.Event.DismissDialog)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_deleting_partner)))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}
