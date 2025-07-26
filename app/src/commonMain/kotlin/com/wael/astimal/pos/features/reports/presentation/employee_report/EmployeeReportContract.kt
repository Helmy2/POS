package com.wael.astimal.pos.features.reports.presentation.employee_report

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// --- CONTRACT ---
object EmployeeReportContract {
    data class State(
        val employees: List<User> = emptyList(),
        val activities: List<EmployeeActivity> = emptyList(),
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
        data class ShowActivities(val activities: List<EmployeeActivity>) : Event
        data object GeneratePdf : Event
        data class PdfGenerationFinished(val message: String) : Event
        data class PdfGenerationSuccess(val pdfUri: String) : Event
        data class SelectActivity(val activity: EmployeeActivity) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}