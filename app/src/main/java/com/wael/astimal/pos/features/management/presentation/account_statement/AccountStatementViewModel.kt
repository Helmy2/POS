package com.wael.astimal.pos.features.management.presentation.account_statement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.repository.AccountStatementRepository
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountStatementViewModel(
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val accountStatementRepository: AccountStatementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountStatementState())
    val state: StateFlow<AccountStatementState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null
    private var statementJob: Job? = null

    init {
        // Initially load all partners with an empty query.
        searchPartners("")
    }

    fun onEvent(event: AccountStatementEvent) {
        when (event) {
            is AccountStatementEvent.SearchPartner -> {
                _state.update { it.copy(searchQuery = event.query) }
                searchPartners(event.query)
            }
            is AccountStatementEvent.SelectPartner -> {
                _state.update { it.copy(selectedPartner = event.partner) }
                loadAccountStatement(event.partner)
            }
            is AccountStatementEvent.ClearPartnerSelection -> {
                // Go back to the partner list view
                statementJob?.cancel()
                _state.update {
                    it.copy(
                        selectedPartner = null,
                        transactions = emptyList(),
                        isStatementLoading = false
                    )
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchPartners(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isPartnerListLoading = true) }
            businessPartnerRepository.getBusinessPartners(query)
                .debounce(300) // Small delay to avoid querying on every keystroke
                .catch {
                    _state.update { it.copy(isPartnerListLoading = false) }
                    // Handle error
                }
                .collect { partners ->
                    _state.update {
                        it.copy(
                            isPartnerListLoading = false,
                            partners = partners
                        )
                    }
                }
        }
    }

    private fun loadAccountStatement(partner: BusinessPartner) {
        statementJob?.cancel()
        statementJob = accountStatementRepository.getAccountStatement(partner)
            .onEach { transactions ->
                _state.update {
                    it.copy(
                        isStatementLoading = false,
                        transactions = transactions
                    )
                }
            }
            .catch {
                _state.update { it.copy(isStatementLoading = false) }
                // Handle error
            }
            .launchIn(viewModelScope)

        _state.update { it.copy(isStatementLoading = true) }
    }
}
