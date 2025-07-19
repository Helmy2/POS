package com.wael.astimal.pos.features.reports.presentation.account_statement

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.reports.domain.repository.AccountStatementRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_generating_pdf

class AccountStatementViewModel(
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val accountStatementRepository: AccountStatementRepository,
    private val htmlReportGenerator: HtmlReportGenerator,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController
) : BaseViewModel<AccountStatementContract.State, AccountStatementContract.Event, AccountStatementContract.Effect>(
    reducer = AccountStatementReducer(), initialState = AccountStatementContract.State()
) {

    private var searchJob: Job? = null
    private var statementJob: Job? = null

    init {
        searchPartners("")
    }

    override fun handleEvent(event: AccountStatementContract.Event) {
        when (event) {
            is AccountStatementContract.Event.SearchQueryChanged -> {
                setState(event)
                searchPartners(event.query)
            }

            is AccountStatementContract.Event.PartnerSelected -> {
                setState(event)
                loadAccountStatement(event.partner)
            }

            is AccountStatementContract.Event.ClearPartnerSelection -> {
                statementJob?.cancel()
                setState(event)
            }

            is AccountStatementContract.Event.ExportToPdfClicked -> {
                exportStatementToPdf()
            }

            is AccountStatementContract.Event.NavigateBack -> {
                viewModelScope.launch {
                    navigationController.navigateBack()
                }
            }

            is AccountStatementContract.Event.PdfGenerationFinished -> {
                viewModelScope.launch {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.Dynamic(event.message)))
                }
                setState(event)
            }

            else -> setState(event)
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchPartners(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            setState(AccountStatementContract.Event.PartnerListLoading)
            businessPartnerRepository.getBusinessPartners(query)
                .debounce(300)
                .collect { partners ->
                    setState(AccountStatementContract.Event.PartnersLoaded(partners))
                }
        }
    }

    private fun loadAccountStatement(partner: BusinessPartner) {
        statementJob?.cancel()
        setState(AccountStatementContract.Event.StatementLoading)
        statementJob =
            accountStatementRepository.getAccountStatement(partner).onEach { transactions ->
                setState(AccountStatementContract.Event.StatementLoaded(transactions))
            }.launchIn(viewModelScope)
    }

    private fun exportStatementToPdf() {
        viewModelScope.launch {
            val partner = state.value.selectedPartner
            val transactions = state.value.transactions
            if (partner == null || transactions.isEmpty()) {
                snackbarController.sendEvent(
                    SnackbarEvent(StringResource.FromResource(Res.string.error_generating_pdf))
                )
                return@launch
            }
            try {
                setState(AccountStatementContract.Event.IsPdfGeneratingChanged(true))
                delay(500)
                val html = htmlReportGenerator.createStatementHtml(
                    partner = partner,
                    transactions = state.value.transactions
                )
                setState(AccountStatementContract.Event.PdfGenerationSuccessFul(html))
            } catch (e: Exception) {
                e.printStackTrace()
                snackbarController.sendEvent(
                    SnackbarEvent(StringResource.FromResource(Res.string.error_generating_pdf))
                )
                setState(
                    AccountStatementContract.Event.IsPdfGeneratingChanged(false)
                )
            }
        }
    }
}
