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
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import com.wael.astimal.pos.features.user.domain.PermissionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.no_permission

class CustomerStatementViewModel(
    private val partnerRepository: BusinessPartnerRepository,
    private val reportRepository: ReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator,
    private val navigationController: NavigationController,
) : BaseViewModel<CustomerStatementReducer.State, CustomerStatementReducer.Event, CustomerStatementReducer.Effect>(
    CustomerStatementReducer(), CustomerStatementReducer.State()
) {
    private var transactionJob: Job? = null

    init {
        // Start the initial data load when the ViewModel is created
        processEvent(CustomerStatementReducer.Event.LoadInitialData)
    }

    /**
     * Handles all incoming events from the UI.
     * For complex events with side effects (like data loading), it launches coroutines.
     * For simple state changes, it calls setState to update the state via the reducer.
     */
    override fun handleEvent(event: CustomerStatementReducer.Event) {
        when (event) {
            is CustomerStatementReducer.Event.LoadInitialData -> loadPartners()
            is CustomerStatementReducer.Event.ApplyFilters -> {
                setState(event) // Update state to show loading indicator
                loadTransactions()
            }

            is CustomerStatementReducer.Event.TransactionClicked -> navigateToTransaction(event.transaction)
            is CustomerStatementReducer.Event.GeneratePdf -> generatePdf()
            // For all other events, the change is purely a state mutation,
            // so we just pass them to the reducer.

            is CustomerStatementReducer.Event.PdfGenerationFinished -> {
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
            setState(CustomerStatementReducer.Event.ShowInitialData(partners))
        }
    }

    private fun loadTransactions() {
        transactionJob?.cancel()
        val currentState = state.value
        val partnerId = currentState.selectedPartner?.id ?: return

        transactionJob = viewModelScope.launch {
            reportRepository.getTransactionsForPartner(
                partnerId, currentState.startDate, currentState.endDate
            ).collect { transactions ->
                // As new data arrives, send it to the reducer
                setState(CustomerStatementReducer.Event.ShowTransactions(transactions))
            }
        }
    }

    private fun generatePdf() {
        viewModelScope.launch {
            val currentState = state.value
            currentState.selectedPartner?.let { partner ->
                val html = htmlReportGenerator.createCustomerStatementHtml(
                    partner = partner,
                    transactions = currentState.transactions,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate
                )

                setState(CustomerStatementReducer.Event.PdfGenerationSuccessful(html = html))
            }
        }
    }

    private fun navigateToTransaction(transaction: DetailedTransaction) {
        viewModelScope.launch {
            val destination = when {
                transaction.transactionType == TransactionType.SALE_INVOICE &&
                        PermissionManager.canView(Destination.SalesOrders()) -> {
                    Destination.SalesOrders(
                        transaction.invoiceId
                    )
                }

                transaction.transactionType == TransactionType.PURCHASE_INVOICE &&
                        PermissionManager.canView(Destination.PurchaseOrders()) -> Destination.PurchaseOrders(
                    transaction.invoiceId
                )

                transaction.transactionType == TransactionType.SALE_RETURN_INVOICE &&
                        PermissionManager.canView(Destination.SalesReturns()) -> Destination.SalesReturns(
                    transaction.invoiceId
                )

                transaction.transactionType == TransactionType.PURCHASE_RETURN_INVOICE &&
                        PermissionManager.canView(Destination.PurchaseReturns()) -> Destination.PurchaseReturns(
                    transaction.invoiceId
                )

                else -> {
                    snackbarController.sendEvent(
                        SnackbarEvent(
                            StringResource.FromResource(Res.string.no_permission)
                        )
                    )
                    null
                }
            }

            destination?.let { navigationController.navigate(it) }
        }
    }
}
