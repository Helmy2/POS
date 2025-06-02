package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.core.util.ConnectivityImp
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val coreModule = module {
    single<Connectivity> {
        ConnectivityImp(context = androidApplication())
    }
}