package com.wael.astimal.pos.di

import com.wael.astimal.pos.BuildKonfig
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.user.data.local.SettingsManager
import com.wael.astimal.pos.features.user.data.local.SettingsManagerImpl
import com.wael.astimal.pos.features.user.data.remote.ProfileApiService
import com.wael.astimal.pos.features.user.data.remote.ProfileApiServiceImpl
import com.wael.astimal.pos.features.user.data.repository.NotificationRepositoryImpl
import com.wael.astimal.pos.features.user.data.repository.UserRepositoryImpl
import com.wael.astimal.pos.features.user.domain.repository.NotificationRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import com.wael.astimal.pos.features.user.presentation.employee.EmployeeViewModel
import com.wael.astimal.pos.features.user.presentation.login.LoginViewModel
import com.wael.astimal.pos.features.user.presentation.setting.SettingsViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.createSupabaseClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val userModule = module {
    single { get<AppDatabase>().userDao() }

    singleOf(::ProfileApiServiceImpl) { bind<ProfileApiService>() }
    singleOf(::SettingsManagerImpl) { bind<SettingsManager>() }
    singleOf(::NotificationRepositoryImpl) { bind<NotificationRepository>() }

    single<UserRepository> {
        UserRepositoryImpl(
            userDao = get(),
            supabaseClient = get(),
            settingsManager = get(),
            profileApiService = get(),
            syncManager = get(),
            adminClient = {
                createSupabaseClient(
                    supabaseKey = BuildKonfig.supabaseKey,
                    supabaseUrl = BuildKonfig.supabaseUrl
                ) {
                    install(Auth) {
                        minimalConfig()
                    }
                }.apply {
                    auth.importAuthToken(BuildKonfig.serviceRole)
                }
            }
        )
    }

    viewModelOf(::SettingsViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::EmployeeViewModel)
}