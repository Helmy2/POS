package com.wael.astimal.pos.core.presentation

import android.app.Application
import com.wael.astimal.pos.di.appModule
import com.wael.astimal.pos.core.data.DummyDataSeeder
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApp : Application() {
    private val dataSeeder: DummyDataSeeder by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApp)
            modules(appModule)
        }

        dataSeeder.seedInitialDataIfNeeded()
    }
}