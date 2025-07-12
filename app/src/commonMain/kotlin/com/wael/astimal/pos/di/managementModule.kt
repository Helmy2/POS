package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.repository.BusinessPartnerRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.EmployeeTransactionRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.InvoiceRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.PartnerTransactionRepositoryImpl
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.EmployeeTransactionRepository
import com.wael.astimal.pos.features.management.domain.repository.InvoiceRepository
import com.wael.astimal.pos.features.management.domain.repository.PartnerTransactionRepository
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
    single { get<AppDatabase>().invoiceDao() }
    single { get<AppDatabase>().employeeFinancesDao() }
    single { get<AppDatabase>().partnerTransactionDao() }

    singleOf(::EmployeeTransactionRepositoryImpl) { bind<EmployeeTransactionRepository>() }
    singleOf(::PartnerTransactionRepositoryImpl) { bind<PartnerTransactionRepository>() }
    singleOf(::BusinessPartnerRepositoryImpl) { bind<BusinessPartnerRepository>() }
    singleOf(::InvoiceRepositoryImpl) { bind<InvoiceRepository>() }

    viewModelOf(::ManagementViewModel)
    viewModelOf(::BusinessPartnerViewModel)
    viewModelOf(::SalesReturnViewModel)
    viewModelOf(::SalesViewModel)
    viewModelOf(::PurchaseViewModel)
    viewModelOf(::PurchaseReturnViewModel)
    viewModelOf(::EmployeeAccountViewModel)
    viewModelOf(::ReceivePayVoucherViewModel)
}