package com.wael.astimal.pos.features.reports.presentation.reports

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import pos.app.generated.resources.Res
import pos.app.generated.resources.customer_statement
import pos.app.generated.resources.employee_daily_report

class ReportsReducer : Reducer<ReportsContract.State, ReportsContract.Event, Nothing> {
    override fun reduce(
        previousState: ReportsContract.State, event: ReportsContract.Event
    ): Pair<ReportsContract.State, Nothing?> {
        return when (event) {
            is ReportsContract.Event.LoadReports -> {
                val reportItems = listOf(
//                    ReportsContract.ReportsItem(
//                        Destination.AccountStatement,
//                        Res.string.account_statement
//                    ),
                    ReportsContract.ReportsItem(
                        Destination.CustomerStatement, Res.string.customer_statement
                    ),
                    ReportsContract.ReportsItem(
                        Destination.EmployeeReport, Res.string.employee_daily_report
                    ),
                )
                previousState.copy(items = reportItems) to null
            }

            is ReportsContract.Event.ReportClicked -> previousState to null
        }
    }
}
