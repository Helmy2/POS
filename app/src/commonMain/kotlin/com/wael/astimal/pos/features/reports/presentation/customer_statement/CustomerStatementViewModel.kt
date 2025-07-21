package com.wael.astimal.pos.features.reports.presentation.customer_statement

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.navigation.AppKoinComponent.snackbarController
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import com.wael.astimal.pos.features.reports.domain.repository.CustomerStatementRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CustomerStatementViewModel(
    private val partnerRepository: BusinessPartnerRepository,
    private val statementRepository: CustomerStatementRepository,
    private val htmlReportGenerator: HtmlReportGenerator,
    private val navigationController: NavigationController
) : BaseViewModel<CustomerStatementContract.State, CustomerStatementContract.Event, CustomerStatementContract.Effect>(
    CustomerStatementReducer(), CustomerStatementContract.State()
) {
    private var transactionJob: Job? = null

    init {
        // Start the initial data load when the ViewModel is created
        processEvent(CustomerStatementContract.Event.LoadInitialData)
    }

    /**
     * Handles all incoming events from the UI.
     * For complex events with side effects (like data loading), it launches coroutines.
     * For simple state changes, it calls setState to update the state via the reducer.
     */
    override fun handleEvent(event: CustomerStatementContract.Event) {
        when (event) {
            is CustomerStatementContract.Event.LoadInitialData -> loadPartners()
            is CustomerStatementContract.Event.ApplyFilters -> {
                setState(event) // Update state to show loading indicator
                loadTransactions()
            }

            is CustomerStatementContract.Event.TransactionClicked -> navigateToTransaction(event.transaction)
            is CustomerStatementContract.Event.GeneratePdf -> generatePdf()
            // For all other events, the change is purely a state mutation,
            // so we just pass them to the reducer.

            is CustomerStatementContract.Event.PdfGenerationFinished -> {
                viewModelScope.launch {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.Dynamic(event.message)))
                }
                setState(event)
            }

            else -> setState(event)
        }
    }

    private fun loadPartners() {
        viewModelScope.launch {
            val partners = partnerRepository.getBusinessPartners().first()
            setState(CustomerStatementContract.Event.ShowInitialData(partners))
        }
    }

    private fun loadTransactions() {
        transactionJob?.cancel()
        val currentState = state.value
        val partnerId = currentState.selectedPartner?.id ?: return

        transactionJob = viewModelScope.launch {
            statementRepository.getTransactionsForPartner(
                partnerId,
                currentState.startDate,
                currentState.endDate
            ).collect { transactions ->
                // As new data arrives, send it to the reducer
                setState(CustomerStatementContract.Event.ShowTransactions(transactions))
            }
        }
    }

    private fun generatePdf() {
        val currentState = state.value
        currentState.selectedPartner?.let { partner ->
            val html = htmlReportGenerator.createCustomerStatementHtml(
                partner = partner,
                transactions = currentState.transactions,
                startDate = currentState.startDate,
                endDate = currentState.endDate
            )

            setState(CustomerStatementContract.Event.PdfGenerationSuccessful(html = html))
        }
    }

    private fun navigateToTransaction(transaction: DetailedTransaction) {
        val destination = when (transaction.transactionType) {
            TransactionType.SALE_INVOICE -> Destination.SalesOrders(transaction.invoiceId)
            TransactionType.PURCHASE_INVOICE -> Destination.PurchaseOrders(transaction.invoiceId)
            TransactionType.SALE_RETURN_INVOICE -> Destination.SalesReturns(transaction.invoiceId)
            TransactionType.PURCHASE_RETURN_INVOICE -> Destination.PurchaseReturns(transaction.invoiceId)
            else -> null
        }
        viewModelScope.launch {
            destination?.let { navigationController.navigate(it) }
        }
    }
}
