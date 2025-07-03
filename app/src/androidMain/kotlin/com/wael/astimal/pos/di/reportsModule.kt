package com.wael.astimal.pos.di

import com.wael.astimal.pos.features.reports.data.repository.AccountStatementRepositoryImpl
import com.wael.astimal.pos.features.reports.domain.repository.AccountStatementRepository
import com.wael.astimal.pos.features.reports.presentation.account_statement.AccountStatementViewModel
import com.wael.astimal.pos.features.reports.presentation.pdf.PdfGenerator
import com.wael.astimal.pos.features.reports.presentation.reports.ReportsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reportsModule = module {
    singleOf(::PdfGenerator)
    singleOf(::AccountStatementRepositoryImpl) { bind<AccountStatementRepository>() }

    viewModelOf(::AccountStatementViewModel)
    viewModelOf(::ReportsViewModel)
}