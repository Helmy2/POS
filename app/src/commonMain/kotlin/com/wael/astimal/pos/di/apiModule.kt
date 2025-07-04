package com.wael.astimal.pos.di

import com.wael.astimal.pos.features.user.data.remote.AuthApiService
import com.wael.astimal.pos.features.user.data.remote.AuthApiServiceImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val apiModule = module {
    singleOf(::AuthApiServiceImpl) { bind<AuthApiService>() }
}
