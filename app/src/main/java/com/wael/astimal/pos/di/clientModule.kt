package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.client_management.data.repository.ClientRepositoryImpl
import com.wael.astimal.pos.features.client_management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.client_management.presentaion.client.ClientViewModel
import com.wael.astimal.pos.features.client_management.presentaion.clinet_info.ClientInfoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val clientModule = module {
    single { get<AppDatabase>().clientDao() }

    single<ClientRepository> {
        ClientRepositoryImpl(get())
    }

    viewModel { ClientViewModel() }
    viewModel { ClientInfoViewModel(get()) }

}