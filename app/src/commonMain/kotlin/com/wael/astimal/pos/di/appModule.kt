package com.wael.astimal.pos.di

import org.koin.core.module.Module
import org.koin.dsl.module


expect val platformModule: Module

val appModule = module {
    includes(
        coreModule,
        supabaseModule,
        userModule,
        inventoryModule,
        managementModule,
        dashboardModule,
        reportsModule,
    )
}
