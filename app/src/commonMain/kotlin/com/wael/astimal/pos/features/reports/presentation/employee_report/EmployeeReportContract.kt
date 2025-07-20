package com.wael.astimal.pos.features.reports.presentation.employee_report

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

object EmployeeReportContract {
    data class State(
        val employees: List<User> = emptyList(),
        val transactions: List<EmployeeTransaction> = emptyList(),
        val selectedEmployee: User? = null,
        val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val employees: List<User>) : Event
        data class SelectEmployee(val employee: User) : Event
        data class SetStartDate(val date: LocalDate) : Event
        data class SetEndDate(val date: LocalDate) : Event
        data object ApplyFilters : Event
        data class ShowTransactions(val transactions: List<EmployeeTransaction>) : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val html: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}