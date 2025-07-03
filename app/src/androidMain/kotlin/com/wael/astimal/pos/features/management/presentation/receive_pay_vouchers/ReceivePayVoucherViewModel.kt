package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.management.domain.entity.matchesQuery
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.ReceivePayVoucherRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReceivePayVoucherViewModel(
    private val voucherRepository: ReceivePayVoucherRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController,
) : BaseViewModel<ReceivePayVoucherContract.State, ReceivePayVoucherContract.Event, Nothing>(
    reducer = ReceivePayVoucherReducer(),
    initialState = ReceivePayVoucherContract.State(
        dialogState = ReceivePayVoucherContract.DialogState(
            date = Clock.now()
        )
    )
) {
    val filteredVouchersState: StateFlow<List<ReceivePayVoucher>> =
        combine(
            state,
            voucherRepository.getVouchers()
        ) { state, allVouchers ->
            if (state.vouchers != allVouchers) {
                setState(ReceivePayVoucherContract.Event.VouchersLoaded(allVouchers))
            }
            if (state.searchQuery.isBlank()) {
                allVouchers
            } else {
                allVouchers.filter { it.matchesQuery(state.searchQuery) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        handleEvent(ReceivePayVoucherContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: ReceivePayVoucherContract.Event) {
        when (event) {
            is ReceivePayVoucherContract.Event.LoadInitialData -> loadInitialData()
            is ReceivePayVoucherContract.Event.SaveChangesClicked -> saveVoucher()
            is ReceivePayVoucherContract.Event.DeleteVoucherClicked -> deleteVoucher(event.voucher)
            is ReceivePayVoucherContract.Event.BackClicked -> navigateBack()
            else -> setState(event)
        }
    }

    private fun loadInitialData() {
        setState(ReceivePayVoucherContract.Event.LoadInitialData)
        viewModelScope.launch {
            setState(ReceivePayVoucherContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
        viewModelScope.launch {
            combine(
                partnerRepository.getBusinessPartners(""),
                voucherRepository.getVouchers()
            ) { partners, vouchers ->
                val clients =
                    partners.filter { it.type == PartnerType.CLIENT || it.type == PartnerType.CLIENT_AND_CAN_BE_SUPPLIER }
                val suppliers =
                    partners.filter { it.type == PartnerType.SUPPLIER || it.type == PartnerType.SUPPLIER_AND_CAN_BE_CLIENT }

                setState(
                    ReceivePayVoucherContract.Event.DropdownDataLoaded(
                        ReceivePayVoucherContract.DropdownData(clients, suppliers)
                    )
                )
                setState(ReceivePayVoucherContract.Event.VouchersLoaded(vouchers))
            }.catch {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_loading_data)))
            }.launchIn(viewModelScope)
        }
    }

    private fun saveVoucher() {
        viewModelScope.launch {
            val dialogState = state.value.dialogState
            val currentUser = state.value.currentUser
            val amount = dialogState.amount.toDoubleOrNull()

            if (currentUser == null || amount == null || amount <= 0 || dialogState.selectedPartnerId == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_some_field_are_required)))
                return@launch
            }

            val partner = if (dialogState.partyType == VoucherPartyType.CLIENT) {
                state.value.dropdownData.clients.find { it.id.local == dialogState.selectedPartnerId }
            } else {
                state.value.dropdownData.suppliers.find { it.id.local == dialogState.selectedPartnerId }
            }

            if (partner == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_party_not_found)))
                return@launch
            }

            val voucherToSave = ReceivePayVoucher(
                id = dialogState.voucherToEdit?.id ?: Id.new,
                party = partner,
                partyType = dialogState.partyType,
                createdBy = currentUser,
                amount = amount,
                notes = dialogState.notes,
                createdAt = dialogState.date,
            )

            val result = voucherRepository.saveVoucher(voucherToSave)


            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.voucher_saved_successfully)))
                setState(ReceivePayVoucherContract.Event.SaveSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_saving_voucher)))
            }
        }
    }

    private fun deleteVoucher(voucher: ReceivePayVoucher) {
        viewModelScope.launch {
            voucherRepository.deleteVoucher(voucher).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.voucher_deleted_successfully)))
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_deleting_voucher)))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}
