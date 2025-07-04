package com.wael.astimal.pos

import android.app.Application
import com.wael.astimal.pos.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApp)
        }
    }
}