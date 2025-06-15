package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.logic.OrderAmountLogic
import com.wael.astimal.pos.features.management.data.logic.ReturnAmountLogic
import com.wael.astimal.pos.features.management.data.repository.AccountStatementRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.BusinessPartnerRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.ClientRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.EmployeeAccountRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.PurchaseRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.PurchaseReturnRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.ReceivePayVoucherRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.SalesOrderRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.SalesReturnRepositoryImpl
import com.wael.astimal.pos.features.management.data.repository.SupplierRepositoryImpl
import com.wael.astimal.pos.features.management.domain.repository.AccountStatementRepository
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.EmployeeAccountRepository
import com.wael.astimal.pos.features.management.domain.repository.PurchaseRepository
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import com.wael.astimal.pos.features.management.domain.repository.ReceivePayVoucherRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.management.presentation.account_statement.AccountStatementViewModel
import com.wael.astimal.pos.features.management.presentation.business_partner.BusinessPartnerViewModel
import com.wael.astimal.pos.features.management.presentation.employee_account.EmployeeAccountViewModel
import com.wael.astimal.pos.features.management.presentation.management.ManagementViewModel
import com.wael.astimal.pos.features.management.presentation.purchase.PurchaseViewModel
import com.wael.astimal.pos.features.management.presentation.purchase_return.PurchaseReturnViewModel
import com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers.ReceivePayVoucherViewModel
import com.wael.astimal.pos.features.management.presentation.sales.SalesViewModel
import com.wael.astimal.pos.features.management.presentation.sales_return.SalesReturnViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val managementModule = module {
    single { get<AppDatabase>().clientDao() }
    single { get<AppDatabase>().supplierDao() }
    single { get<AppDatabase>().orderReturnDao() }
    single { get<AppDatabase>().purchaseOrderDao() }
    single { get<AppDatabase>().purchaseReturnDao() }
    single { get<AppDatabase>().employeeFinancesDao() }
    single { get<AppDatabase>().receivePayVoucherDao() }

    single { OrderAmountLogic(get(), get(), get(), get()) }
    single { ReturnAmountLogic(get(), get(), get(), get()) }

    single<ClientRepository> { ClientRepositoryImpl(get()) }
    single<SalesOrderRepository> { SalesOrderRepositoryImpl(get(), get(), get(), get()) }
    single<SalesReturnRepository> { SalesReturnRepositoryImpl(get(), get(), get(), get()) }
    single<SupplierRepository> { SupplierRepositoryImpl(get()) }
    single<PurchaseRepository> { PurchaseRepositoryImpl(get(), get(), get(), get(), get()) }
    single<PurchaseReturnRepository> {
        PurchaseReturnRepositoryImpl(get(), get(), get(), get(), get())
    }
    single<EmployeeAccountRepository> { EmployeeAccountRepositoryImpl(get(), get()) }
    single<ReceivePayVoucherRepository> {
        ReceivePayVoucherRepositoryImpl(get(), get(), get(), get())
    }
    single<BusinessPartnerRepository> { BusinessPartnerRepositoryImpl(get(),get(),get(),get(),get()) }
    single<AccountStatementRepository> {
        AccountStatementRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { ManagementViewModel() }

    viewModel { BusinessPartnerViewModel(get(),get()) }
    viewModel { SalesReturnViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SalesViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PurchaseViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PurchaseReturnViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { EmployeeAccountViewModel(get(), get(),get()) }
    viewModel { ReceivePayVoucherViewModel(get(), get(), get(), get()) }
    viewModel { AccountStatementViewModel(get(), get()) }
}