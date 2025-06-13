package com.wael.astimal.pos.di

import com.wael.astimal.pos.features.dashboard.presentation.DashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    viewModel { DashboardViewModel(get()) }
}