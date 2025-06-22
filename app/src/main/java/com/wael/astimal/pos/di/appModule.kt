package com.wael.astimal.pos.di

import org.koin.dsl.module


val appModule = module {
    includes(
        coreModule,
        apiModule,
        userModule,
        inventoryModule,
        managementModule,
        dashboardModule,
        reportsModule
    )
}
