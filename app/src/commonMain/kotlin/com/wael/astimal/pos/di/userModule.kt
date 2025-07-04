package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.user.data.remote.ProfileApiService
import com.wael.astimal.pos.features.user.data.remote.ProfileApiServiceImpl
import com.wael.astimal.pos.features.user.data.repository.SettingsManagerImpl
import com.wael.astimal.pos.features.user.data.repository.UserRepositoryImpl
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import com.wael.astimal.pos.features.user.presentation.login.LoginViewModel
import com.wael.astimal.pos.features.user.presentation.setting.SettingsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val userModule = module {
    single { get<AppDatabase>().userDao() }

    singleOf(::ProfileApiServiceImpl) { bind<ProfileApiService>() }
    singleOf(::SettingsManagerImpl) { bind<SettingsManager>() }
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }

    viewModelOf(::SettingsViewModel)
    viewModelOf(::LoginViewModel)
}