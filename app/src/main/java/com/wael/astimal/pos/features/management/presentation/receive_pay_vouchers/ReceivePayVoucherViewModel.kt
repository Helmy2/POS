package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.ReceivePayVoucherRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            val currentUser = sessionManager.getCurrentUser().first()
            _state.update {
                it.copy(
                    currentUser = currentUser
                )
            }
        }
        loadInitialData()
    }

    fun onEvent(event: ReceivePayVoucherEvent) {
        when (event) {
            is ReceivePayVoucherEvent.SelectPartyType -> _state.update {
                it.copy(
                    partyType = event.type,
                    selectedClient = null,
                    selectedSupplier = null
                )
            }

            is ReceivePayVoucherEvent.SelectClient -> _state.update { it.copy(selectedClient = event.client) }
            is ReceivePayVoucherEvent.SelectSupplier -> _state.update { it.copy(selectedSupplier = event.supplier) }
            is ReceivePayVoucherEvent.UpdateAmount -> _state.update { it.copy(amount = event.amount) }
            is ReceivePayVoucherEvent.UpdateNotes -> _state.update { it.copy(notes = event.notes) }
            is ReceivePayVoucherEvent.UpdateDate -> _state.update { it.copy(date = event.date) }
            is ReceivePayVoucherEvent.AddVoucher -> addVoucher()
            is ReceivePayVoucherEvent.EditVoucherClicked -> _state.update {
                it.copy(
                    showEditDialog = true,
                    voucherToEdit = event.voucher
                )
            }

            is ReceivePayVoucherEvent.SaveVoucher -> saveVoucher(event.voucher)
            is ReceivePayVoucherEvent.DeleteVoucherClicked -> deleteVoucher(event.voucher)
            is ReceivePayVoucherEvent.DismissEditDialog -> _state.update {
                it.copy(
                    showEditDialog = false,
                    voucherToEdit = null
                )
            }
        }
    }

    private fun addVoucher() {
        viewModelScope.launch {

            val currentState = _state.value
            val currentUser = _state.value.currentUser
            val amount = currentState.amount.toDoubleOrNull()

            if (currentUser == null || amount == null || amount <= 0) {
                viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar(R.string.invalid_data)) }
                return@launch
            }

            val party: Any = when (currentState.partyType) {
                VoucherPartyType.CLIENT -> currentState.selectedClient ?: run {
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar(R.string.please_select_a_party)) }
                    return@launch
                }

                VoucherPartyType.SUPPLIER -> currentState.selectedSupplier ?: run {
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar(R.string.please_select_a_party)) }
                    return@launch
                }
            }

            val newVoucher = ReceivePayVoucher(
                localId = 0,
                serverId = null,
                amount = amount,
                party = party,
                partyType = currentState.partyType,
                date = currentState.date,
                notes = currentState.notes.takeIf { it.isNotBlank() },
                createdBy = currentUser,
                isSynced = false
            )
            saveVoucher(newVoucher)
        }
    }

    private fun saveVoucher(voucher: ReceivePayVoucher) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = if (voucher.localId == 0L) {
                voucherRepository.addVoucher(voucher)
            } else {
                voucherRepository.updateVoucher(voucher)
            }
            result.fold(
                onSuccess = {
                    _eventFlow.emit(UiEvent.ShowSnackbar(R.string.voucher_saved_successfully))
                    _state.update {
                        it.copy(
                            isSaving = false,
                            showEditDialog = false,
                            voucherToEdit = null,
                            amount = "",
                            notes = "",
                            selectedClient = null,
                        )
                    }
                },
                onFailure = {
                    _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_saving_voucher))
                    _state.update { it.copy(isSaving = false) }
                }
            )
        }
    }

    private fun deleteVoucher(voucher: ReceivePayVoucher) {
        viewModelScope.launch {
            val result = voucherRepository.deleteVoucher(voucher.localId)
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.voucher_deleted_successfully))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_deleting_voucher))
            }
        }
    }

    private fun loadInitialData() {
        _state.update { it.copy(isLoading = true) }
        combine(
            voucherRepository.getVouchers(),
            clientRepository.searchClients(""),
            supplierRepository.getSuppliers("")
        ) { vouchers, clients, suppliers ->
            _state.update {
                it.copy(
                    vouchers = vouchers,
                    clients = clients,
                    suppliers = suppliers,
                    isLoading = false
                )
            }
        }.catch {
            _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_loading_data))
        }.launchIn(viewModelScope)
    }
}
