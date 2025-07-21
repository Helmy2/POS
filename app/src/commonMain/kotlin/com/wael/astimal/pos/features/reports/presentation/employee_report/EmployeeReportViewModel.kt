package com.wael.astimal.pos.features.reports.presentation.employee_report

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.presentation.navigation.AppKoinComponent.snackbarController
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
    private var activityJob: Job? = null

    init {
        processEvent(EmployeeReportContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: EmployeeReportContract.Event) {
        when (event) {
            is EmployeeReportContract.Event.LoadInitialData -> loadEmployees()
            is EmployeeReportContract.Event.ApplyFilters -> {
                setState(event)
                loadActivities()
            }
            is EmployeeReportContract.Event.GeneratePdf -> generatePdf()

            is EmployeeReportContract.Event.PdfGenerationFinished -> {
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
            setState(EmployeeReportContract.Event.ShowInitialData(employees))
        }
    }

    private fun loadActivities() {
        activityJob?.cancel()
        val currentState = state.value
        val employeeId = currentState.selectedEmployeeId ?: return
        activityJob = viewModelScope.launch {
            reportRepository.getEmployeeActivityForDateRange(
                employeeId,
                currentState.startDate,
                currentState.endDate
            )
                .collect { activities ->
                    setState(EmployeeReportContract.Event.ShowActivities(activities))
                }
        }
    }

    private fun generatePdf() {
        val currentState = state.value
        currentState.selectedEmployee?.let { employee ->
            val html = htmlReportGenerator.createEmployeeActivityReportHtml(
                employee = employee,
                activities = currentState.activities,
                startDate = currentState.startDate,
                endDate = currentState.endDate
            )
            setState(EmployeeReportContract.Event.PdfGenerationSuccess(html))
        }
    }
}