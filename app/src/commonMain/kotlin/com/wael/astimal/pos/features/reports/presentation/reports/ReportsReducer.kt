package com.wael.astimal.pos.features.reports.presentation.reports

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import pos.app.generated.resources.Res
import pos.app.generated.resources.client_debit_report
import pos.app.generated.resources.current_stock_report
import pos.app.generated.resources.customer_statement
import pos.app.generated.resources.employee_daily_report
import pos.app.generated.resources.employee_ledger_report
import pos.app.generated.resources.product_movement_report
import pos.app.generated.resources.stock_transfer_report

class ReportsReducer : Reducer<ReportsContract.State, ReportsContract.Event, Nothing> {
    override fun reduce(
        previousState: ReportsContract.State, event: ReportsContract.Event
    ): Pair<ReportsContract.State, Nothing?> {
        return when (event) {
            is ReportsContract.Event.LoadReports -> {
                val reportItems = listOf(
                    ReportsContract.ReportsItem(
                        Destination.CustomerStatement, Res.string.customer_statement
                    ),
                    ReportsContract.ReportsItem(
                        Destination.EmployeeReport, Res.string.employee_daily_report
                    ),
                    ReportsContract.ReportsItem(
                        Destination.EmployeeLedger, Res.string.employee_ledger_report
                    ),
                    ReportsContract.ReportsItem(
                        Destination.ProductMovement, Res.string.product_movement_report
                    ),
                    ReportsContract.ReportsItem(
                        Destination.CurrentStock, Res.string.current_stock_report
                    ),
                    ReportsContract.ReportsItem(
                        Destination.ClientDebit, Res.string.client_debit_report
                    ),
                    ReportsContract.ReportsItem(
                        Destination.StockTransferReport, Res.string.stock_transfer_report
                    ),
                )
                previousState.copy(items = reportItems) to null
            }

            is ReportsContract.Event.ReportClicked -> previousState to null
        }
    }
}
