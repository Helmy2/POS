package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.reports.data.repository.AccountStatementRepositoryImpl
import com.wael.astimal.pos.features.reports.data.repository.CustomerStatementRepositoryImpl
import com.wael.astimal.pos.features.reports.domain.repository.AccountStatementRepository
import com.wael.astimal.pos.features.reports.domain.repository.CustomerStatementRepository
import com.wael.astimal.pos.features.reports.presentation.account_statement.AccountStatementViewModel
import com.wael.astimal.pos.features.reports.presentation.customer_statement.CustomerStatementViewModel
import com.wael.astimal.pos.features.reports.presentation.reports.ReportsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reportsModule = module {
    singleOf(::AccountStatementRepositoryImpl) { bind<AccountStatementRepository>() }
    singleOf(::HtmlReportGenerator)

    singleOf(::CustomerStatementRepositoryImpl) { bind<CustomerStatementRepository>() }


    viewModelOf(::AccountStatementViewModel)
    viewModelOf(::ReportsViewModel)
    viewModelOf(::CustomerStatementViewModel)
}