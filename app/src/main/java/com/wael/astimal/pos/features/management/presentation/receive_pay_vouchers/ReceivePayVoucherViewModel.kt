package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.ReceivePayVoucherRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceivePayVoucherViewModel(
    private val voucherRepository: ReceivePayVoucherRepository,
    private val clientRepository: ClientRepository,
    private val supplierRepository: SupplierRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ReceivePayVoucherState())
    val state: StateFlow<ReceivePayVoucherState> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    fun onEvent(event: ReceivePayVoucherEvent) {
        when (event) {
            is ReceivePayVoucherEvent.SelectPartyType -> _state.update { it.copy(partyType = event.type, selectedClient = null, selectedSupplier = null) }
            is ReceivePayVoucherEvent.SelectClient -> _state.update { it.copy(selectedClient = event.client) }
            is ReceivePayVoucherEvent.SelectSupplier -> _state.update { it.copy(selectedSupplier = event.supplier) }
            is ReceivePayVoucherEvent.UpdateAmount -> _state.update { it.copy(amount = event.amount) }
            is ReceivePayVoucherEvent.UpdateNotes -> _state.update { it.copy(notes = event.notes) }
            is ReceivePayVoucherEvent.UpdateDate -> _state.update { it.copy(date = event.date) }
            is ReceivePayVoucherEvent.SaveVoucher -> saveVoucher()
            is ReceivePayVoucherEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is ReceivePayVoucherEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadInitialData() {
        val vouchersFlow = voucherRepository.getVouchers()
        val clientsFlow = clientRepository.searchClients()
        val suppliersFlow = supplierRepository.getSuppliers()

        combine(vouchersFlow, clientsFlow, suppliersFlow) { vouchers, clients, suppliers ->
            _state.update {
                it.copy(
                    vouchers = vouchers,
                    clients = clients,
                    suppliers = suppliers,
                    isLoading = false
                )
            }
        }.catch {
            _state.update { it.copy(isLoading = false, error = R.string.error_loading_data) }
        }.launchIn(viewModelScope)
    }

    private fun saveVoucher() {
        viewModelScope.launch {
            val currentState = _state.value
            val currentUser = sessionManager.getCurrentUser().first()
            val amount = currentState.amount.toDoubleOrNull()

            if (currentUser == null) {
                _state.update { it.copy(error = R.string.user_not_identified) }
                return@launch
            }

            if (amount == null || amount <= 0) {
                _state.update { it.copy(error = R.string.invalid_amount) }
                return@launch
            }

            val party: Any? = when (currentState.partyType) {
                VoucherPartyType.CLIENT -> currentState.selectedClient
                VoucherPartyType.SUPPLIER -> currentState.selectedSupplier
            }

            if (party == null) {
                _state.update { it.copy(error = R.string.please_select_a_party) }
                return@launch
            }

            val voucher = ReceivePayVoucher(
                localId = 0L,
                serverId = null,
                amount = amount,
                party = party,
                partyType = currentState.partyType,
                date = currentState.date,
                notes = currentState.notes.takeIf { it.isNotBlank() },
                createdBy = currentUser,
                isSynced = false
            )

            voucherRepository.addVoucher(voucher).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            snackbarMessage = R.string.voucher_saved_successfully,
                            amount = "",
                            notes = "",
                            selectedClient = null,
                            selectedSupplier = null
                        )
                    }
                },
                onFailure = {
                    _state.update { it.copy(error = R.string.error_saving_voucher) }
                }
            )
        }
    }
}
