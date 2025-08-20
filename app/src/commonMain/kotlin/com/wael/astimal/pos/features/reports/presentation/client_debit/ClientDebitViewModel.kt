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
) : BaseViewModel<ClientDebitReducer.State, ClientDebitReducer.Event, ClientDebitReducer.Effect>(
    ClientDebitReducer(),
    ClientDebitReducer.State()
) {
    private var job: Job? = null

    init {
        processEvent(ClientDebitReducer.Event.LoadInitialData)
    }

    override fun handleEvent(event: ClientDebitReducer.Event) {
        when (event) {
            is ClientDebitReducer.Event.LoadInitialData -> loadEmployees()
            is ClientDebitReducer.Event.SelectEmployee -> {
                setState(event)
            }

            is ClientDebitReducer.Event.ApplyFilters -> {
                setState(event)
                loadDebitList()
            }

            is ClientDebitReducer.Event.GeneratePdf -> viewModelScope.launch { generatePdf() }
            else -> setState(event)
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            val employees = userRepository.getEmployeesFlow().first()
            setState(ClientDebitReducer.Event.EmployeesInitialData(employees))
        }
    }

    private fun loadDebitList() {
        job?.cancel()
        job = viewModelScope.launch {
            reportRepository.getClientsWithDebit(state.value.selectedEmployee?.id)
                .collect { debitList ->
                    setState(ClientDebitReducer.Event.ShowDebitList(debitList))
                }
        }
    }

    private suspend fun generatePdf() {
        val currentState = state.value
        val html = htmlReportGenerator.createClientDebitReportHtml(
            debitList = currentState.debitList
        )
        setState(ClientDebitReducer.Event.PdfGenerationSuccess(html))
    }
}