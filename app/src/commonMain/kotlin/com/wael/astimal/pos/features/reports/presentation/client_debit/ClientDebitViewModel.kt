package com.wael.astimal.pos.features.reports.presentation.client_debit

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClientDebitViewModel(
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator
) : BaseViewModel<ClientDebitContract.State, ClientDebitContract.Event, ClientDebitContract.Effect>(
    ClientDebitReducer(),
    ClientDebitContract.State()
) {
    private var job: Job? = null

    init {
        processEvent(ClientDebitContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: ClientDebitContract.Event) {
        when (event) {
            is ClientDebitContract.Event.LoadInitialData -> loadEmployees()
            is ClientDebitContract.Event.SelectEmployee -> {
                setState(event)
            }

            is ClientDebitContract.Event.ApplyFilters -> {
                setState(event)
                loadDebitList()
            }

            is ClientDebitContract.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }
            else -> setState(event)
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            val employees = userRepository.getEmployeesFlow().first()
            setState(ClientDebitContract.Event.EmployeesInitialData(employees))
        }
    }

    private fun loadDebitList() {
        job?.cancel()
        job = viewModelScope.launch {
            reportRepository.getClientsWithDebit(state.value.selectedEmployeeId)
                .collect { debitList ->
                    setState(ClientDebitContract.Event.ShowDebitList(debitList))
                }
        }
    }

    private suspend fun generatePdf() {
        val currentState = state.value
        val html = htmlReportGenerator.createClientDebitReportHtml(
            debitList = currentState.debitList
        )
        setState(ClientDebitContract.Event.PdfGenerationSuccess(html))
    }
}