package com.wael.astimal.pos.features.reports.presentation.employee_ledger

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.datetime.LocalDateTime

class EmployeeLedgerReducer :
    Reducer<EmployeeLedgerReducer.State, EmployeeLedgerReducer.Event, EmployeeLedgerReducer.Effect> {

    data class State(
        val employees: List<User> = emptyList(),
        val ledgerEntries: List<EmployeeLedgerEntry> = emptyList(),
        val selectedEmployee: User? = null,
        val startDate: LocalDateTime = Clock.getStartOfToday(),
        val endDate: LocalDateTime = Clock.getEndOfToday(),
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0,
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class ShowInitialData(val employees: List<User>) : Event
        data class SelectEmployee(val employee: User?) : Event
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
    override fun reduce(
        previousState: State,
        event: Event,
    ): Pair<State, Effect?> {
        return when (event) {
            is Event.ShowInitialData -> previousState.copy(employees = event.employees) to null
            is Event.SelectEmployee -> previousState.copy(selectedEmployee = event.employee) to null
            is Event.SetStartDate -> previousState.copy(startDate = event.date) to null
            is Event.SetEndDate -> previousState.copy(endDate = event.date) to null
            is Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                ledgerEntries = emptyList()
            ) to null

            is Event.ShowLedger -> previousState.copy(
                isLoading = false,
                ledgerEntries = event.entries
            ) to null

            is Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.pdfUri
            ) to null

            else -> previousState to null
        }
    }
}