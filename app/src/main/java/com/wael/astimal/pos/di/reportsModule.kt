package com.wael.astimal.pos.di

import com.wael.astimal.pos.features.reports.data.repository.AccountStatementRepositoryImpl
import com.wael.astimal.pos.features.reports.domain.repository.AccountStatementRepository
import com.wael.astimal.pos.features.reports.presentation.account_statement.AccountStatementViewModel
import com.wael.astimal.pos.features.reports.presentation.pdf.PdfGenerator
import com.wael.astimal.pos.features.reports.presentation.reports.ReportsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val reportsModule = module {

    single {
        PdfGenerator(get())
    }
    single<AccountStatementRepository> {
        AccountStatementRepositoryImpl(
            get(), get(), get(), get(), get()
        )
    }

    viewModel { AccountStatementViewModel(get(), get(), get()) }
    viewModel { ReportsViewModel() }
}