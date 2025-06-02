package com.wael.astimal.pos.di

import org.koin.dsl.module


val appModule = module {
    includes(coreModule)
}
