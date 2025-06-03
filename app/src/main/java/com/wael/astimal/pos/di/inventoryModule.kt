package com.wael.astimal.pos.di

import com.wael.astimal.pos.features.inventory.presentation.inventory.InventoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val inventoryModule = module {
    viewModel { InventoryViewModel() }
}