package com.wael.astimal.pos.features.management.presentation.business_partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class BusinessPartnerViewModel(
    private val businessPartnerRepository: BusinessPartnerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BusinessPartnerInfoState())
    val state: StateFlow<BusinessPartnerInfoState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var businessPartnerSearchJob: Job? = null

    init {
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

            BusinessPartnerInfoEvent.DetailBusinessPartner -> _state.update {
                it.copy(
                    showDetailDialog = false
                )
            }

            BusinessPartnerInfoEvent.ShowDetailDialog -> _state.update { it.copy(showDetailDialog = true) }
        }
    }

    private fun searchBusinessPartnersList(query: String) {
        businessPartnerSearchJob?.cancel()
        businessPartnerSearchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, query = query) }
            if (query.length > 1 || query.isEmpty()) {
                delay(300)
            }
            businessPartnerRepository.getBusinessPartners(query).catch { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_searching_business_partners))
            }.collect { businessPartners ->
                _state.update { it.copy(loading = false, searchResults = businessPartners) }
            }
        }
    }
}