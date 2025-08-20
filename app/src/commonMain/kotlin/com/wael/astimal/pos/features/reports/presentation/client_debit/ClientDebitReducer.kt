package com.wael.astimal.pos.features.reports.presentation.client_debit

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import com.wael.astimal.pos.features.user.domain.entity.User

class ClientDebitReducer :
    Reducer<ClientDebitReducer.State, ClientDebitReducer.Event, ClientDebitReducer.Effect> {

    data class State(
        val employees: List<User> = emptyList(),
        val debitList: List<ClientDebitInfo> = emptyList(),
        val selectedEmployee: User? = null,
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class EmployeesInitialData(val employees: List<User>) : Event
        data class SelectEmployee(val employee: User?) : Event
        data class ShowDebitList(val debitList: List<ClientDebitInfo>) : Event
        data object ApplyFilters : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val pdfUri: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
    override fun reduce(
        previousState: ClientDebitReducer.State,
        event: ClientDebitReducer.Event
    ): Pair<ClientDebitReducer.State, ClientDebitReducer.Effect?> {
        return when (event) {
            is ClientDebitReducer.Event.EmployeesInitialData -> previousState.copy(employees = event.employees) to null
            is ClientDebitReducer.Event.SelectEmployee -> previousState.copy(selectedEmployee = event.employee) to null
            is ClientDebitReducer.Event.ShowDebitList -> previousState.copy(
                isLoading = false,
                debitList = event.debitList
            ) to null

            is ClientDebitReducer.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is ClientDebitReducer.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.pdfUri
            ) to null

            is ClientDebitReducer.Event.ApplyFilters -> previousState.copy(isLoading = true) to null
            else -> previousState to null
        }
    }
}