package com.wael.astimal.pos.features.reports.presentation.employee_report

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.navigation.AppKoinComponent.snackbarController
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EmployeeReportViewModel(
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val htmlReportGenerator: HtmlReportGenerator,
    private val navigationController: NavigationController,
) : BaseViewModel<EmployeeReportReducer.State, EmployeeReportReducer.Event, EmployeeReportReducer.Effect>(
    EmployeeReportReducer(),
    EmployeeReportReducer.State()
) {
    private var activityJob: Job? = null

    init {
        processEvent(EmployeeReportReducer.Event.LoadInitialData)
    }

    override fun handleEvent(event: EmployeeReportReducer.Event) {
        when (event) {
            is EmployeeReportReducer.Event.LoadInitialData -> loadEmployees()
            is EmployeeReportReducer.Event.ApplyFilters -> {
                setState(event)
                loadActivities()
            }

            is EmployeeReportReducer.Event.GeneratePdf -> generatePdf()

            is EmployeeReportReducer.Event.PdfGenerationFinished -> {
                viewModelScope.launch {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.Dynamic(event.message)))
                }
                setState(event)
            }

            is EmployeeReportReducer.Event.SelectActivity -> navigateToTransaction(event.activity)

            else -> setState(event)
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            val employees = userRepository.getEmployeesFlow().first()
            setState(EmployeeReportReducer.Event.ShowInitialData(employees))
        }
    }

    private fun loadActivities() {
        activityJob?.cancel()
        val currentState = state.value
        val employeeId = currentState.selectedEmployee?.id ?: return
        activityJob = viewModelScope.launch {
            reportRepository.getEmployeeActivityForDateRange(
                employeeId,
                currentState.startDate,
                currentState.endDate
            )
                .collect { activities ->
                    setState(EmployeeReportReducer.Event.ShowActivities(activities))
                }
        }
    }

    private fun generatePdf() {
        viewModelScope.launch {
            val currentState = state.value
            currentState.selectedEmployee?.let { employee ->
                val html = htmlReportGenerator.createEmployeeActivityReportHtml(
                    employee = employee,
                    activities = currentState.activities,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate
                )
                setState(EmployeeReportReducer.Event.PdfGenerationSuccess(html))
            }
        }
    }

    private fun navigateToTransaction(transaction: EmployeeActivity) {
        if (transaction !is EmployeeActivity.InvoiceActivity)
            return

        val destination = when (transaction.invoice.invoiceType) {
            InvoiceType.SALES -> Destination.SalesOrders(transaction.id)
            InvoiceType.PURCHASE -> Destination.PurchaseOrders(transaction.id)
            InvoiceType.SALES_RETURN -> Destination.SalesReturns(transaction.id)
            InvoiceType.PURCHASE_RETURN -> Destination.PurchaseReturns(transaction.id)
        }
        viewModelScope.launch {
            destination.let { navigationController.navigate(it) }
        }
    }
}