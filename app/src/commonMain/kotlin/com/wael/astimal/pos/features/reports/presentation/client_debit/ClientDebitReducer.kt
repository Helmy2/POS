package com.wael.astimal.pos.features.reports.presentation.client_debit

import com.wael.astimal.pos.core.base.mvi.Reducer

class ClientDebitReducer :
    Reducer<ClientDebitContract.State, ClientDebitContract.Event, ClientDebitContract.Effect> {
    override fun reduce(
        previousState: ClientDebitContract.State,
        event: ClientDebitContract.Event
    ): Pair<ClientDebitContract.State, ClientDebitContract.Effect?> {
        return when (event) {
            is ClientDebitContract.Event.EmployeesInitialData -> previousState.copy(employees = event.employees) to null
            is ClientDebitContract.Event.SelectEmployee -> previousState.copy(selectedEmployeeId = event.employeeId) to null
            is ClientDebitContract.Event.ShowDebitList -> previousState.copy(
                isLoading = false,
                debitList = event.debitList
            ) to null

            is ClientDebitContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is ClientDebitContract.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.pdfUri
            ) to null

            is ClientDebitContract.Event.ApplyFilters -> previousState.copy(isLoading = true) to null
            else -> previousState to null
        }
    }
}