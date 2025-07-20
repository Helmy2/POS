package com.wael.astimal.pos.features.reports.presentation.profits_report

import com.wael.astimal.pos.core.base.mvi.Reducer

class ProfitReportReducer :
    Reducer<ProfitReportContract.State, ProfitReportContract.Event, ProfitReportContract.Effect> {
    override fun reduce(
        previousState: ProfitReportContract.State,
        event: ProfitReportContract.Event
    ): Pair<ProfitReportContract.State, ProfitReportContract.Effect?> {
        return when (event) {
            is ProfitReportContract.Event.ShowInitialData -> previousState.copy(employees = event.employees) to null
            is ProfitReportContract.Event.SelectEmployee -> previousState.copy(selectedEmployeeId = event.employeeId) to null
            is ProfitReportContract.Event.SetStartDate -> previousState.copy(startDate = event.date) to null
            is ProfitReportContract.Event.SetEndDate -> previousState.copy(endDate = event.date) to null
            is ProfitReportContract.Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                profitSummary = emptyList()
            ) to null

            is ProfitReportContract.Event.ShowProfits -> previousState.copy(
                isLoading = false,
                profitSummary = event.summary
            ) to null

            is ProfitReportContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is ProfitReportContract.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.pdfUri
            ) to null

            else -> previousState to null
        }
    }
}