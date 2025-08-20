package com.wael.astimal.pos.features.reports.presentation.employee_ledger

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object EmployeeLedgerContract {
    data class State(
        val employees: List<User> = emptyList(),
        val ledgerEntries: List<EmployeeLedgerEntry> = emptyList(),
        val selectedEmployeeId: String? = null,
        val startDate: LocalDateTime = Clock.currentLocalDateTime(),
        val endDate: LocalDateTime = Clock.currentLocalDateTime(),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0,
    ) : Reducer.ViewState {
        val selectedEmployee: User?
            get() = employees.find { it.id == selectedEmployeeId }
    }

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val employees: List<User>) : Event
        data class SelectEmployee(val employeeId: String) : Event
        data class SetStartDate(val date: LocalDateTime) : Event
        data class SetEndDate(val date: LocalDateTime) : Event
        data object ApplyFilters : Event
        data class ShowLedger(val entries: List<EmployeeLedgerEntry>) : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val pdfUri: String) : Event
        data class SelectEntry(val activity: EmployeeLedgerEntry) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}