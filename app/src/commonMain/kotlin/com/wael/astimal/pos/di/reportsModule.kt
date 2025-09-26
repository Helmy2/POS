package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.reports.data.repository.ReportRepositoryImpl
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import com.wael.astimal.pos.features.reports.presentation.client_debit.ClientDebitViewModel
import com.wael.astimal.pos.features.reports.presentation.current_stock.CurrentStockViewModel
import com.wael.astimal.pos.features.reports.presentation.customer_statement.CustomerStatementViewModel
import com.wael.astimal.pos.features.reports.presentation.employee_ledger.EmployeeLedgerViewModel
import com.wael.astimal.pos.features.reports.presentation.employee_activity_report.EmployeeReportViewModel
import com.wael.astimal.pos.features.reports.presentation.product_movement.ProductMovementViewModel
import com.wael.astimal.pos.features.reports.presentation.reports.ReportsViewModel
import com.wael.astimal.pos.features.reports.presentation.stock_transfer.StockTransferReportViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reportsModule = module {
    singleOf(::HtmlReportGenerator)

    singleOf(::ReportRepositoryImpl) { bind<ReportRepository>() }

    viewModelOf(::ReportsViewModel)
    viewModelOf(::CustomerStatementViewModel)
    viewModelOf(::EmployeeReportViewModel)
    viewModelOf(::EmployeeLedgerViewModel)
    viewModelOf(::ProductMovementViewModel)
    viewModelOf(::CurrentStockViewModel)
    viewModelOf(::ClientDebitViewModel)
    viewModelOf(::StockTransferReportViewModel)
}