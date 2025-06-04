package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.data.repository.UnitRepositoryImpl
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.inventory.presentation.inventory.InventoryViewModel
import com.wael.astimal.pos.features.inventory.presentation.unit.UnitViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val inventoryModule = module {
    single { get<AppDatabase>().unitDao() }

    single<UnitRepository> {
        UnitRepositoryImpl(get())
    }

    viewModel { InventoryViewModel() }
    viewModel { UnitViewModel(get()) }
}