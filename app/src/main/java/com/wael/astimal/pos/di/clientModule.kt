package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.client_management.data.repository.ClientRepositoryImpl
import com.wael.astimal.pos.features.client_management.data.repository.PurchaseRepositoryImpl
import com.wael.astimal.pos.features.client_management.data.repository.SalesOrderRepositoryImpl
import com.wael.astimal.pos.features.client_management.data.repository.SalesReturnRepositoryImpl
import com.wael.astimal.pos.features.client_management.data.repository.SupplierRepositoryImpl
import com.wael.astimal.pos.features.client_management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.client_management.domain.repository.PurchaseRepository
import com.wael.astimal.pos.features.client_management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.client_management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.client_management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.client_management.presentaion.client.ClientViewModel
import com.wael.astimal.pos.features.client_management.presentaion.clinet_info.ClientInfoViewModel
import com.wael.astimal.pos.features.client_management.presentaion.purchase.PurchaseViewModel
import com.wael.astimal.pos.features.client_management.presentaion.sales_order.SalesOrderViewModel
import com.wael.astimal.pos.features.client_management.presentaion.sales_return.SalesReturnViewModel
import com.wael.astimal.pos.features.client_management.presentaion.supplier_info.SupplierViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val clientModule = module {
    single { get<AppDatabase>().clientDao() }
    single { get<AppDatabase>().supplierDao() }
    single { get<AppDatabase>().orderReturnDao() }
    single { get<AppDatabase>().purchaseOrderDao() }

    single<ClientRepository> {
        ClientRepositoryImpl(get())
    }
    single<SalesOrderRepository> {
        SalesOrderRepositoryImpl(get())
    }
    single<SalesReturnRepository> {
        SalesReturnRepositoryImpl(get())
    }

    single<SupplierRepository> {
        SupplierRepositoryImpl(get())
    }
    single<PurchaseRepository> {
        PurchaseRepositoryImpl(get())
    }

    viewModel { ClientViewModel() }

    viewModel { ClientInfoViewModel(get()) }
    viewModel { SalesReturnViewModel(get(), get(), get(), get()) }
    viewModel { SalesOrderViewModel(get(), get(),get(),  get(),get()) }

    viewModel { SupplierViewModel(get()) }
    viewModel { PurchaseViewModel(get(), get(), get(), get()) }

}