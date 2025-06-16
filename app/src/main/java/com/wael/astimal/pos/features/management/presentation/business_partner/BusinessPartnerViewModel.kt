package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BusinessPartnerViewModel(
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(BusinessPartnerInfoState())
    val state: StateFlow<BusinessPartnerInfoState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var businessPartnerSearchJob: Job? = null

    init {
        viewModelScope.launch {
            val user = sessionManager.getCurrentUser().firstOrNull()
            _state.update { it.copy(isAdmin = user?.isAdmin == true) }
        }
        onEvent(BusinessPartnerInfoEvent.SearchBusinessPartners(_state.value.query))
    }

    fun onEvent(event: BusinessPartnerInfoEvent) {
        when (event) {
            is BusinessPartnerInfoEvent.SearchBusinessPartners -> searchBusinessPartnersList(event.query)
            is BusinessPartnerInfoEvent.SelectBusinessPartner -> {
                _state.update {
                    it.copy(
                        selectedBusinessPartner = event.businessPartner,
                        showDetailDialog = event.businessPartner != null
                    )
                }
            }
            is BusinessPartnerInfoEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                searchBusinessPartnersList(event.query)
            }
            is BusinessPartnerInfoEvent.AddNewPartnerClicked -> {
                _state.update {
                    it.copy(
                        showEditDialog = true,
                        partnerToEdit = createBlankBusinessPartner()
                    )
                }
            }
            is BusinessPartnerInfoEvent.EditPartnerClicked -> {
                _state.update {
                    it.copy(
                        showDetailDialog = false,
                        showEditDialog = true,
                        partnerToEdit = event.partner
                    )
                }
            }
            is BusinessPartnerInfoEvent.SavePartnerClicked -> {
                // The partner object from the dialog state is combined with the opening balances
                val finalPartner = event.partner.copy(
                    clientDebt = event.openingDebt,
                    supplierIndebtedness = event.openingIndebtedness
                )
                savePartner(finalPartner)
            }
            is BusinessPartnerInfoEvent.DeletePartnerClicked -> {
                deletePartner(event.partner)
            }
            BusinessPartnerInfoEvent.DismissEditDialog -> {
                _state.update { it.copy(showEditDialog = false, partnerToEdit = null) }
            }
            BusinessPartnerInfoEvent.DismissDetailDialog -> {
                _state.update { it.copy(showDetailDialog = false, selectedBusinessPartner = null) }
            }
        }
    }

    private fun savePartner(partner: BusinessPartner) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = businessPartnerRepository.saveBusinessPartner(partner)
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.partner_saved_successfully))
                _state.update { it.copy(isSaving = false, showEditDialog = false, partnerToEdit = null) }
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_saving_partner))
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun deletePartner(partner: BusinessPartner) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = businessPartnerRepository.deleteBusinessPartner(partner)
            _state.update { it.copy(isSaving = false, showDetailDialog = false, selectedBusinessPartner = null) }
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.partner_deleted_successfully))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_deleting_partner))
            }
        }
    }

    private fun searchBusinessPartnersList(query: String) {
        businessPartnerSearchJob?.cancel()
        businessPartnerSearchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, query = query) }
            if (query.length > 1 || query.isEmpty()) {
                delay(300)
            }
            businessPartnerRepository.getBusinessPartners(query).catch {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_searching_business_partners))
            }.collect { businessPartners ->
                _state.update { it.copy(loading = false, searchResults = businessPartners) }
            }
        }
    }
}
