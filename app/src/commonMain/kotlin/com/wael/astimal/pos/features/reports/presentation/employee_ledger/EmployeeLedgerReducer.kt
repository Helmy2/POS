package com.wael.astimal.pos.features.reports.presentation.employee_ledger

import com.wael.astimal.pos.core.base.mvi.Reducer

class EmployeeLedgerReducer :
    Reducer<EmployeeLedgerContract.State, EmployeeLedgerContract.Event, EmployeeLedgerContract.Effect> {
    override fun reduce(
        previousState: EmployeeLedgerContract.State,
        event: EmployeeLedgerContract.Event,
    ): Pair<EmployeeLedgerContract.State, EmployeeLedgerContract.Effect?> {
        return when (event) {
            is EmployeeLedgerContract.Event.ShowInitialData -> previousState.copy(employees = event.employees) to null
            is EmployeeLedgerContract.Event.SelectEmployee -> previousState.copy(selectedEmployeeId = event.employeeId) to null
            is EmployeeLedgerContract.Event.SetStartDate -> previousState.copy(startDate = event.date) to null
            is EmployeeLedgerContract.Event.SetEndDate -> previousState.copy(endDate = event.date) to null
            is EmployeeLedgerContract.Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                ledgerEntries = emptyList()
            ) to null

            is EmployeeLedgerContract.Event.ShowLedger -> previousState.copy(
                isLoading = false,
                ledgerEntries = event.entries
            ) to null

            is EmployeeLedgerContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is EmployeeLedgerContract.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.pdfUri
            ) to null

            else -> previousState to null
        }
    }
}