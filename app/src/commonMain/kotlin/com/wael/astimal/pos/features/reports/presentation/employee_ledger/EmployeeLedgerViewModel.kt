package com.wael.astimal.pos.features.reports.presentation.employee_ledger

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.reports.domain.repository.EmployeeLedgerRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EmployeeLedgerViewModel(
    private val userRepository: UserRepository,
    private val reportRepository: EmployeeLedgerRepository,
    private val htmlReportGenerator: HtmlReportGenerator,
) : BaseViewModel<EmployeeLedgerContract.State, EmployeeLedgerContract.Event, EmployeeLedgerContract.Effect>(
    EmployeeLedgerReducer(),
    EmployeeLedgerContract.State()
) {
    private var job: Job? = null

    init {
        processEvent(EmployeeLedgerContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: EmployeeLedgerContract.Event) {
        when (event) {
            is EmployeeLedgerContract.Event.LoadInitialData -> loadEmployees()
            is EmployeeLedgerContract.Event.ApplyFilters -> {
                setState(event)
                loadLedger()
            }

            is EmployeeLedgerContract.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }
            else -> setState(event)
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            val employees = userRepository.getEmployeesFlow().first()
            setState(EmployeeLedgerContract.Event.ShowInitialData(employees))
        }
    }

    private fun loadLedger() {
        job?.cancel()
        val currentState = state.value
        val employeeId = currentState.selectedEmployeeId ?: return
        job = viewModelScope.launch {
            reportRepository.getEmployeeLedger(
                employeeId,
                currentState.startDate,
                currentState.endDate
            )
                .collect { entries ->
                    setState(EmployeeLedgerContract.Event.ShowLedger(entries))
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
            setState(EmployeeLedgerContract.Event.PdfGenerationSuccess(html))
        }
    }
}