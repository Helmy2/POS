package com.wael.astimal.pos.features.reports.presentation.employee_report

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.reports.domain.repository.EmployeeReportRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EmployeeReportViewModel(
    private val userRepository: UserRepository,
    private val reportRepository: EmployeeReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator
) : BaseViewModel<EmployeeReportContract.State, EmployeeReportContract.Event, EmployeeReportContract.Effect>(
    EmployeeReportReducer(),
    EmployeeReportContract.State()
) {
    private var transactionJob: Job? = null

    init {
        processEvent(EmployeeReportContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: EmployeeReportContract.Event) {
        when (event) {
            is EmployeeReportContract.Event.LoadInitialData -> loadEmployees()
            is EmployeeReportContract.Event.ApplyFilters -> {
                setState(event) // Set loading state
                loadTransactions()
            }

            is EmployeeReportContract.Event.GeneratePdf -> generatePdf()
            else -> setState(event)
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            val employees = userRepository.getEmployeesFlow().first()
            setState(EmployeeReportContract.Event.ShowInitialData(employees))
        }
    }

    private fun loadTransactions() {
        transactionJob?.cancel()
        val currentState = state.value
        val employeeId = currentState.selectedEmployee?.id ?: return

        transactionJob = viewModelScope.launch {
            reportRepository.getEmployeeTransactionsForDate(
                employeeId,
                currentState.startDate,
                currentState.endDate
            )
                .collect { transactions ->
                    setState(EmployeeReportContract.Event.ShowTransactions(transactions))
                }
        }
    }

    private fun generatePdf() {
        val currentState = state.value
        currentState.selectedEmployee?.let { employee ->
            val html = htmlReportGenerator.createEmployeeReportHtml(
                employee = employee,
                transactions = currentState.transactions,
                startDate = currentState.startDate,
                endDate = currentState.endDate
            )
            setState(
                EmployeeReportContract.Event.PdfGenerationSuccess(html = html),
            )
        }
    }
}