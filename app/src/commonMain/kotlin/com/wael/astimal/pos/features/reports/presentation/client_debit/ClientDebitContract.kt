package com.wael.astimal.pos.features.reports.presentation.client_debit

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import com.wael.astimal.pos.features.user.domain.entity.User

object ClientDebitContract {
    data class State(
        val employees: List<User> = emptyList(),
        val debitList: List<ClientDebitInfo> = emptyList(),
        val selectedEmployeeId: String? = null,
        val isLoading: Boolean = false,
        val pdfHtmlToGenerate: String? = null,
        val pdfGenerationTrigger: Int = 0
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInitialData : Event
        data class EmployeesInitialData(val employees: List<User>) : Event
        data class SelectEmployee(val employeeId: String?) : Event
        data class ShowDebitList(val debitList: List<ClientDebitInfo>) : Event
        data object ApplyFilters : Event
        data object GeneratePdf : Event
        data object PdfGenerationFinished : Event
        data class PdfGenerationSuccess(val pdfUri: String) : Event
    }

    sealed interface Effect : Reducer.ViewEffect
}

