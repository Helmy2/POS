package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.user.data.local.SessionManager
import com.wael.astimal.pos.features.user.data.local.SessionManagerImpl
import com.wael.astimal.pos.features.user.data.repository.SettingsManagerImpl
import com.wael.astimal.pos.features.user.data.repository.UserRepositoryImpl
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import com.wael.astimal.pos.features.user.presentation.login.LoginViewModel
import com.wael.astimal.pos.features.user.presentation.setting.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val userModule = module {
    single { get<AppDatabase>().userDao() }

    single<SettingsManager> {
        SettingsManagerImpl(get())
    }
    single<SessionManager> {
        SessionManagerImpl(get())
    }
    single<UserRepository> {
        UserRepositoryImpl(get(), get(), get(), get())
    }

    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
}