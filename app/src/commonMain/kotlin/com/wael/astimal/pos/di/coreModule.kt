package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.data.SyncManager
import com.wael.astimal.pos.core.data.SyncManagerImpl
import com.wael.astimal.pos.core.data.SyncService
import com.wael.astimal.pos.core.data.SyncServiceImpl
import com.wael.astimal.pos.core.data.remote.SyncApiService
import com.wael.astimal.pos.core.data.remote.SyncApiServiceImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    single<SnackbarController> { SnackbarController }
    single<NavigationController> { NavigationController }

    singleOf(::SyncApiServiceImpl) { bind<SyncApiService>() }
    singleOf(::SyncManagerImpl) { bind<SyncManager>() }
    singleOf(::SyncServiceImpl) { bind<SyncService>() }
}