package com.wael.astimal.pos.features.reports.presentation.employee_ledger

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EmployeeLedgerViewModel(
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator,
    private val navigationController: NavigationController,
) : BaseViewModel<EmployeeLedgerReducer.State, EmployeeLedgerReducer.Event, EmployeeLedgerReducer.Effect>(
    EmployeeLedgerReducer(),
    EmployeeLedgerReducer.State()
) {
    private var job: Job? = null

    init {
        processEvent(EmployeeLedgerReducer.Event.LoadInitialData)
    }

    override fun handleEvent(event: EmployeeLedgerReducer.Event) {
        when (event) {
            is EmployeeLedgerReducer.Event.LoadInitialData -> loadEmployees()
            is EmployeeLedgerReducer.Event.ApplyFilters -> {
                setState(event)
                loadLedger()
            }

            is EmployeeLedgerReducer.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }

            is EmployeeLedgerReducer.Event.SelectEntry -> navigateToTransaction(event.activity)
            else -> setState(event)
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            val employees = userRepository.getEmployeesFlow().first()
            setState(EmployeeLedgerReducer.Event.ShowInitialData(employees))
        }
    }

    private fun loadLedger() {
        job?.cancel()
        val currentState = state.value
        val employeeId = currentState.selectedEmployee?.id ?: return
        job = viewModelScope.launch {
            reportRepository.getEmployeeLedger(
                employeeId,
                currentState.startDate,
                currentState.endDate
            )
                .collect { entries ->
                    setState(EmployeeLedgerReducer.Event.ShowLedger(entries))
                }
        }
    }

    private suspend fun generatePdf() {
        val currentState = state.value
        currentState.selectedEmployee?.let { employee ->
            val html = htmlReportGenerator.createEmployeeLedgerHtml(
                employee = employee,
                entries = currentState.ledgerEntries,
                startDate = currentState.startDate,
                endDate = currentState.endDate
            )
            setState(EmployeeLedgerReducer.Event.PdfGenerationSuccess(html))
        }
    }

    private fun navigateToTransaction(transaction: EmployeeLedgerEntry) {

        val destination = when (transaction.transactionType) {
            EmployeeTransactionType.COMMISSION_FOR_ORDER,
            EmployeeTransactionType.COMMISSION_FOR_RESPONSIBILITY_FOR_ORDER,
            EmployeeTransactionType.COMMISSION_TO_ADMIN_FOR_ORDER -> Destination.SalesOrders(
                transaction.invoiceId
            )

            EmployeeTransactionType.COMMISSION_FOR_RETURN_ORDER,
            EmployeeTransactionType.COMMISSION_FOR_RESPONSIBILITY_FOR_RETURN_ORDER,
            EmployeeTransactionType.COMMISSION_TO_ADMIN_FOR_RETURN_ORDER -> Destination.SalesReturns(
                transaction.invoiceId
            )

            else -> null
        }
        viewModelScope.launch {
            destination?.let { navigationController.navigate(it) }
        }
    }
}