package com.wael.astimal.pos.features.reports.presentation.profits_report

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.presentation.navigation.AppKoinComponent.snackbarController
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.reports.domain.repository.ProfitReportRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfitReportViewModel(
    private val userRepository: UserRepository,
    private val reportRepository: ProfitReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator
) : BaseViewModel<ProfitReportContract.State, ProfitReportContract.Event, ProfitReportContract.Effect>(
    ProfitReportReducer(),
    ProfitReportContract.State()
) {
    private var job: Job? = null

    init {
        processEvent(ProfitReportContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: ProfitReportContract.Event) {
        when (event) {
            is ProfitReportContract.Event.LoadInitialData -> loadEmployees()
            is ProfitReportContract.Event.ApplyFilters -> {
                setState(event)
                loadProfitSummary()
            }

            is ProfitReportContract.Event.GeneratePdf -> generatePdf()

            is ProfitReportContract.Event.PdfGenerationFinished -> {
                viewModelScope.launch {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.Dynamic(event.message)))
                }
                setState(event)
            }

            else -> setState(event)
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            val employees = userRepository.getEmployeesFlow().first()
            setState(ProfitReportContract.Event.ShowInitialData(employees))
        }
    }

    private fun loadProfitSummary() {
        job?.cancel()
        val currentState = state.value
        val employeeId = currentState.selectedEmployeeId ?: return
        job = viewModelScope.launch {
            reportRepository.getEmployeeProfitSummary(
                employeeId,
                currentState.startDate,
                currentState.endDate
            )
                .collect { summary ->
                    setState(ProfitReportContract.Event.ShowProfits(summary))
                }
        }
    }

    private fun generatePdf() {
        val currentState = state.value
        currentState.selectedEmployee?.let { employee ->
            val html = htmlReportGenerator.createProfitReportHtml(
                employee = employee,
                summary = currentState.profitSummary,
                startDate = currentState.startDate,
                endDate = currentState.endDate
            )
            setState(ProfitReportContract.Event.PdfGenerationSuccess(html))
        }
    }
}