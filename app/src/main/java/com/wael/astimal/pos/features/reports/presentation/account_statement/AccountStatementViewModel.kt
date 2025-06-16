package com.wael.astimal.pos.features.reports.presentation.account_statement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.reports.domain.repository.AccountStatementRepository
import com.wael.astimal.pos.features.reports.presentation.pdf.PdfGenerator
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
    private val accountStatementRepository: AccountStatementRepository,
    private val pdfGenerator: PdfGenerator // Inject the generator
) : ViewModel() {

    private val _state = MutableStateFlow(AccountStatementState())
    val state: StateFlow<AccountStatementState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null
    private var statementJob: Job? = null

    init {
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
                statementJob?.cancel()
                _state.update {
                    it.copy(
                        selectedPartner = null,
                        transactions = emptyList(),
                        isStatementLoading = false
                    )
                }
            }
            is AccountStatementEvent.ExportToPdf -> {
                exportStatementToPdf()
            }
        }
    }

    private fun exportStatementToPdf() {
        val partner = _state.value.selectedPartner
        val transactions = _state.value.transactions
        if (partner == null || transactions.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val fileUri = pdfGenerator.generateStatementPdf(partner, transactions)
            if (fileUri != null) {
                _eventFlow.emit(UiEvent.ShareFile(fileUri, R.string.share_statement_pdf))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_creating_pdf))
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchPartners(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isPartnerListLoading = true) }
            businessPartnerRepository.getBusinessPartners(query)
                .debounce(300)
                .catch { _state.update { it.copy(isPartnerListLoading = false) } }
                .collect { partners ->
                    _state.update {
                        it.copy(isPartnerListLoading = false, partners = partners)
                    }
                }
        }
    }

    private fun loadAccountStatement(partner: BusinessPartner) {
        statementJob?.cancel()
        statementJob = accountStatementRepository.getAccountStatement(partner)
            .onEach { transactions ->
                _state.update {
                    it.copy(isStatementLoading = false, transactions = transactions)
                }
            }
            .catch { _state.update { it.copy(isStatementLoading = false) } }
            .launchIn(viewModelScope)

        _state.update { it.copy(isStatementLoading = true) }
    }
}
