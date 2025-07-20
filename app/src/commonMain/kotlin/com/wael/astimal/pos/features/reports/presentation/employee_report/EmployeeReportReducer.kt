package com.wael.astimal.pos.features.reports.presentation.employee_report

import com.wael.astimal.pos.core.base.mvi.Reducer


class EmployeeReportReducer :
    Reducer<EmployeeReportContract.State, EmployeeReportContract.Event, EmployeeReportContract.Effect> {
    override fun reduce(
        previousState: EmployeeReportContract.State,
        event: EmployeeReportContract.Event
    ): Pair<EmployeeReportContract.State, EmployeeReportContract.Effect?> {
        return when (event) {
            is EmployeeReportContract.Event.ShowInitialData -> previousState.copy(employees = event.employees) to null
            is EmployeeReportContract.Event.SelectEmployee -> previousState.copy(selectedEmployeeId = event.employeeId) to null
            is EmployeeReportContract.Event.SetStartDate -> previousState.copy(startDate = event.date) to null
            is EmployeeReportContract.Event.SetEndDate -> previousState.copy(endDate = event.date) to null
            is EmployeeReportContract.Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                activities = emptyList()
            ) to null

            is EmployeeReportContract.Event.ShowActivities -> previousState.copy(
                isLoading = false,
                activities = event.activities
            ) to null

            is EmployeeReportContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null
            is EmployeeReportContract.Event.PdfGenerationSuccess -> {
                previousState.copy(pdfHtmlToGenerate = event.pdfUri) to null
            }
            else -> previousState to null
        }
    }
}