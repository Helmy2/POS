package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.logic.OrderAmountLogic
import com.wael.astimal.pos.features.management.data.logic.ReturnAmountLogic
import com.wael.astimal.pos.features.management.data.repository.BusinessPartnerRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.EmployeeAccountRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.PartnerTransactionRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.PurchaseRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.PurchaseReturnRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.SalesOrderRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.SalesReturnRepositoryImpl
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.EmployeeAccountRepository
import com.wael.astimal.pos.features.management.domain.repository.PartnerTransactionRepository
import com.wael.astimal.pos.features.management.domain.repository.PurchaseRepository
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.management.presentation.business_partner.BusinessPartnerViewModel
import com.wael.astimal.pos.features.management.presentation.employee_account.EmployeeAccountViewModel
import com.wael.astimal.pos.features.management.presentation.management.ManagementViewModel
import com.wael.astimal.pos.features.management.presentation.purchase.PurchaseViewModel
import com.wael.astimal.pos.features.management.presentation.purchase_return.PurchaseReturnViewModel
import com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers.ReceivePayVoucherViewModel
import com.wael.astimal.pos.features.management.presentation.sales.SalesViewModel
import com.wael.astimal.pos.features.management.presentation.sales_return.SalesReturnViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val managementModule = module {
    single { get<AppDatabase>().businessPartnerDao() }
    single { get<AppDatabase>().orderReturnDao() }
    single { get<AppDatabase>().purchaseOrderDao() }
    single { get<AppDatabase>().purchaseReturnDao() }
    single { get<AppDatabase>().employeeFinancesDao() }
    single { get<AppDatabase>().partnerTransactionDao() }

    singleOf(::OrderAmountLogic)
    singleOf(::ReturnAmountLogic)


    singleOf(::SalesOrderRepositoryImpl) { bind<SalesOrderRepository>() }
    singleOf(::SalesReturnRepositoryImpl) { bind<SalesReturnRepository>() }
    singleOf(::PurchaseRepositoryImpl) { bind<PurchaseRepository>() }
    singleOf(::PurchaseReturnRepositoryImpl) { bind<PurchaseReturnRepository>() }
    singleOf(::EmployeeAccountRepositoryImpl) { bind<EmployeeAccountRepository>() }
    singleOf(::PartnerTransactionRepositoryImpl) { bind<PartnerTransactionRepository>() }
    singleOf(::BusinessPartnerRepositoryImpl) { bind<BusinessPartnerRepository>() }


    viewModelOf(::ManagementViewModel)
    viewModelOf(::BusinessPartnerViewModel)
    viewModelOf(::SalesReturnViewModel)
    viewModelOf(::SalesViewModel)
    viewModelOf(::PurchaseViewModel)
    viewModelOf(::PurchaseReturnViewModel)
    viewModelOf(::EmployeeAccountViewModel)
    viewModelOf(::ReceivePayVoucherViewModel)
}