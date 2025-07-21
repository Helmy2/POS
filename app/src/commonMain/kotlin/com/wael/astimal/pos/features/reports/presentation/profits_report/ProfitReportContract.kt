package com.wael.astimal.pos.features.reports.presentation.profits_report

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.reports.domain.model.EmployeeProfitSummary
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

object ProfitReportContract {
    data class State(
        val employees: List<User> = emptyList(),
        val profitSummary: List<EmployeeProfitSummary> = emptyList(),
        val selectedEmployeeId: String? = null,
        val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState {
        val selectedEmployee: User?
            get() = employees.find { it.id == selectedEmployeeId }
    }

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val employees: List<User>) : Event
        data class SelectEmployee(val employeeId: String) : Event
        data class SetStartDate(val date: LocalDate) : Event
        data class SetEndDate(val date: LocalDate) : Event
        data object ApplyFilters : Event
        data class ShowProfits(val summary: List<EmployeeProfitSummary>) : Event
        data object GeneratePdf : Event
        data class PdfGenerationFinished(val message: String) : Event
        data class PdfGenerationSuccess(val pdfUri: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}

