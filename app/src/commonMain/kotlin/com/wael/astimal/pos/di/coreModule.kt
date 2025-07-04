package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.data.DummyDataSeeder
import com.wael.astimal.pos.core.data.SyncManager
import com.wael.astimal.pos.core.data.SyncManagerImpl
import com.wael.astimal.pos.core.data.SyncService
import com.wael.astimal.pos.core.data.SyncServiceImpl
import com.wael.astimal.pos.core.data.remote.SyncApiService
import com.wael.astimal.pos.core.data.remote.SyncApiServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    single<SnackbarController> { SnackbarController }
    single<NavigationController> { NavigationController }

    singleOf(::SyncApiServiceImpl) { bind<SyncApiService>() }
    singleOf(::SyncManagerImpl) { bind<SyncManager>() }
    singleOf(::SyncServiceImpl) { bind<SyncService>() }

    single {
        DummyDataSeeder(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
    }
}