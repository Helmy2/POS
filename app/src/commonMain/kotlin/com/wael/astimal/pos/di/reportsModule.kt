package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.reports.data.repository.CurrentStockRepositoryImpl
import com.wael.astimal.pos.features.reports.data.repository.CustomerStatementRepositoryImpl
import com.wael.astimal.pos.features.reports.data.repository.EmployeeLedgerRepositoryImpl
import com.wael.astimal.pos.features.reports.data.repository.EmployeeReportRepositoryImpl
import com.wael.astimal.pos.features.reports.data.repository.ProductMovementRepositoryImpl
import com.wael.astimal.pos.features.reports.domain.repository.CurrentStockRepository
import com.wael.astimal.pos.features.reports.domain.repository.CustomerStatementRepository
import com.wael.astimal.pos.features.reports.domain.repository.EmployeeLedgerRepository
import com.wael.astimal.pos.features.reports.domain.repository.EmployeeReportRepository
import com.wael.astimal.pos.features.reports.domain.repository.ProductMovementRepository
import com.wael.astimal.pos.features.reports.presentation.current_stock.CurrentStockViewModel
import com.wael.astimal.pos.features.reports.presentation.customer_statement.CustomerStatementViewModel
import com.wael.astimal.pos.features.reports.presentation.employee_ledger.EmployeeLedgerViewModel
import com.wael.astimal.pos.features.reports.presentation.employee_report.EmployeeReportViewModel
import com.wael.astimal.pos.features.reports.presentation.product_movement.ProductMovementViewModel
import com.wael.astimal.pos.features.reports.presentation.reports.ReportsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reportsModule = module {
    singleOf(::HtmlReportGenerator)

    singleOf(::CustomerStatementRepositoryImpl) { bind<CustomerStatementRepository>() }
    singleOf(::EmployeeReportRepositoryImpl) { bind<EmployeeReportRepository>() }
    singleOf(::EmployeeLedgerRepositoryImpl) { bind<EmployeeLedgerRepository>() }
    singleOf(::ProductMovementRepositoryImpl) { bind<ProductMovementRepository>() }
    singleOf(::CurrentStockRepositoryImpl) { bind<CurrentStockRepository>() }

    viewModelOf(::ReportsViewModel)
    viewModelOf(::CustomerStatementViewModel)
    viewModelOf(::EmployeeReportViewModel)
    viewModelOf(::EmployeeLedgerViewModel)
    viewModelOf(::ProductMovementViewModel)
    viewModelOf(::CurrentStockViewModel)
}