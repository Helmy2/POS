package com.wael.astimal.pos.features.reports.presentation.employee_report

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.datetime.LocalDateTime


class EmployeeReportReducer :
    Reducer<EmployeeReportReducer.State, EmployeeReportReducer.Event, EmployeeReportReducer.Effect> {
    data class State(
        val employees: List<User> = emptyList(),
        val activities: List<EmployeeActivity> = emptyList(),
        val selectedEmployee: User? = null,
        val startDate: LocalDateTime = Clock.getStartOfToday(),
        val endDate: LocalDateTime = Clock.getEndOfToday(),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val employees: List<User>) : Event
        data class SelectEmployee(val employee: User?) : Event
        data class SetStartDate(val date: LocalDateTime) : Event
        data class SetEndDate(val date: LocalDateTime) : Event
        data object ApplyFilters : Event
        data class ShowActivities(val activities: List<EmployeeActivity>) : Event
        data object GeneratePdf : Event
        data class PdfGenerationFinished(val message: String) : Event
        data class PdfGenerationSuccess(val pdfUri: String) : Event
        data class SelectActivity(val activity: EmployeeActivity) : Event
    }

    sealed interface Effect : Reducer.ViewEffect

    override fun reduce(
        previousState: State,
        event: Event
    ): Pair<State, Effect?> {
        return when (event) {
            is Event.ShowInitialData -> previousState.copy(employees = event.employees) to null
            is Event.SelectEmployee -> previousState.copy(selectedEmployee = event.employee) to null
            is Event.SetStartDate -> previousState.copy(startDate = event.date) to null
            is Event.SetEndDate -> previousState.copy(endDate = event.date) to null
            is Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                activities = emptyList()
            ) to null

            is Event.ShowActivities -> previousState.copy(
                isLoading = false,
                activities = event.activities
            ) to null

            is Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is Event.PdfGenerationSuccess -> {
                previousState.copy(pdfHtmlToGenerate = event.pdfUri) to null
            }

            else -> previousState to null
        }
    }
}