package com.wael.astimal.pos.features.reports.presentation.reports

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.client_debit_report
import pos.app.generated.resources.current_stock_report
import pos.app.generated.resources.customer_statement
import pos.app.generated.resources.employee_daily_report
import pos.app.generated.resources.employee_ledger_report
import pos.app.generated.resources.product_movement_report
import pos.app.generated.resources.stock_transfer_report

class ReportsReducer : Reducer<ReportsReducer.State, ReportsReducer.Event, Nothing> {

    data class ReportsItem(val destination: Destination, val label: StringResource)

    data class State(
        val items: List<ReportsItem> = emptyList()
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadReports : Event
        data class ReportClicked(val destination: Destination) : Event
    }
    
    override fun reduce(
        previousState: State, event: Event
    ): Pair<State, Nothing?> {
        return when (event) {
            is Event.LoadReports -> {
                val reportItems = listOf(
                    ReportsItem(
                        Destination.ClientDebit, Res.string.client_debit_report
                    ),
                    ReportsItem(
                        Destination.CurrentStock, Res.string.current_stock_report
                    ),

                    ReportsItem(
                        Destination.CustomerStatement, Res.string.customer_statement
                    ),
                    ReportsItem(
                        Destination.EmployeeLedger, Res.string.employee_ledger_report
                    ),
                    ReportsItem(
                        Destination.EmployeeReport, Res.string.employee_daily_report
                    ),

                    ReportsItem(
                        Destination.ProductMovement, Res.string.product_movement_report
                    ),
                    ReportsItem(
                        Destination.StockTransferReport, Res.string.stock_transfer_report
                    ),
                )
                previousState.copy(items = reportItems) to null
            }

            is Event.ReportClicked -> previousState to null
        }
    }
}
