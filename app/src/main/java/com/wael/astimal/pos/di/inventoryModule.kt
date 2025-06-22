package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.data.repository.CategoryRepositoryImpl
import com.wael.astimal.pos.features.inventory.data.repository.ProductRepositoryImpl
import com.wael.astimal.pos.features.inventory.data.repository.StockRepositoryImpl
import com.wael.astimal.pos.features.inventory.data.repository.StockTransferRepositoryImpl
import com.wael.astimal.pos.features.inventory.data.repository.StoreRepositoryImpl
import com.wael.astimal.pos.features.inventory.data.repository.UnitRepositoryImpl
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.inventory.presentation.category.CategoryViewModel
import com.wael.astimal.pos.features.inventory.presentation.inventory.InventoryViewModel
import com.wael.astimal.pos.features.inventory.presentation.product.ProductViewModel
import com.wael.astimal.pos.features.inventory.presentation.stock_management.StockManagementViewModel
import com.wael.astimal.pos.features.inventory.presentation.stock_transfer.StockTransferViewModel
import com.wael.astimal.pos.features.inventory.presentation.store.StoreViewModel
import com.wael.astimal.pos.features.inventory.presentation.unit.UnitViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val inventoryModule = module {
    single { get<AppDatabase>().unitDao() }
    single { get<AppDatabase>().storeDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().productDao() }
    single { get<AppDatabase>().stockTransferDao() }
    single { get<AppDatabase>().salesOrderDao() }
    single { get<AppDatabase>().storeProductStockDao() }
    single { get<AppDatabase>().stockAdjustmentDao() }

    singleOf(::UnitRepositoryImpl) { bind<UnitRepository>() }
    singleOf(::StoreRepositoryImpl) { bind<StoreRepository>() }
    singleOf(::CategoryRepositoryImpl) { bind<CategoryRepository>() }
    singleOf(::ProductRepositoryImpl) { bind<ProductRepository>() }
    singleOf(::StockTransferRepositoryImpl) { bind<StockTransferRepository>() }
    singleOf(::StockRepositoryImpl) { bind<StockRepository>() }


    viewModelOf(::InventoryViewModel)
    viewModelOf(::UnitViewModel)
    viewModelOf(::StoreViewModel)
    viewModelOf(::CategoryViewModel)
    viewModelOf(::ProductViewModel)
    viewModelOf(::StockTransferViewModel)
    viewModelOf(::StockManagementViewModel)
}