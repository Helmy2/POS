package com.wael.astimal.pos.features.reports.presentation.account_statement

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.reports.domain.repository.AccountStatementRepository
import com.wael.astimal.pos.features.reports.presentation.pdf.PdfGenerator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AccountStatementViewModel(
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val accountStatementRepository: AccountStatementRepository,
    private val pdfGenerator: PdfGenerator,
    private val snackbarController: SnackbarController
) : BaseViewModel<AccountStatementContract.State, AccountStatementContract.Event, AccountStatementContract.Effect>(
    reducer = AccountStatementReducer(),
    initialState = AccountStatementContract.State()
) {

    private var searchJob: Job? = null
    private var statementJob: Job? = null

    init {
        searchPartners("")
    }

    override fun handleEvent(event: AccountStatementContract.Event) {
        when (event) {
            is AccountStatementContract.Event.SearchQueryChanged -> {
                setState(event) // Update the query in the state immediately
                searchPartners(event.query)
            }

            is AccountStatementContract.Event.PartnerSelected -> {
                setState(event) // Set the selected partner immediately
                loadAccountStatement(event.partner)
            }

            is AccountStatementContract.Event.ClearPartnerSelection -> {
                statementJob?.cancel()
                setState(event)
            }

            is AccountStatementContract.Event.ExportToPdfClicked -> {
                exportStatementToPdf()
            }
            // Other events are for synchronous state updates only
            else -> setState(event)
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchPartners(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            setState(AccountStatementContract.Event.PartnerListLoading)
            businessPartnerRepository.getBusinessPartners(query)
                .debounce(300) // Debounce to avoid excessive queries while typing
                .collect { partners ->
                    setState(AccountStatementContract.Event.PartnersLoaded(partners))
                }
        }
    }

    private fun loadAccountStatement(partner: BusinessPartner) {
        statementJob?.cancel()
        setState(AccountStatementContract.Event.StatementLoading)
        statementJob = accountStatementRepository.getAccountStatement(partner)
            .onEach { transactions ->
                setState(AccountStatementContract.Event.StatementLoaded(transactions))
            }
            .launchIn(viewModelScope)
    }

    private fun exportStatementToPdf() {
        val partner = state.value.selectedPartner
        val transactions = state.value.transactions
        if (partner == null || transactions.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val fileUri = pdfGenerator.generateStatementPdf(partner, transactions)
            if (fileUri != null) {
                setState(AccountStatementContract.Event.GenerateStatementPdfSuccessfully(fileUri))
            } else {
                snackbarController.sendEvent(
                    SnackbarEvent(StringResource.FromResource(R.string.error_creating_pdf))
                )
            }
        }
    }
}
