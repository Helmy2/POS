package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import com.wael.astimal.pos.features.reports.domain.model.CurrentStockInfo
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface ReportRepository {
    suspend fun getClientsWithDebit(responsibleEmployeeId: String?): Flow<List<ClientDebitInfo>>
    fun getCurrentStock(productId: String?, storeId: String?): Flow<List<CurrentStockInfo>>

    fun getTransactionsForPartner(
        partnerId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<DetailedTransaction>>

    fun getEmployeeLedger(
        employeeId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<EmployeeLedgerEntry>>

    fun getEmployeeActivityForDateRange(
        employeeId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<EmployeeActivity>>

    fun getProductMovement(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        productId: String?,
        storeId: String?
    ): Flow<List<ProductMovementGroup>>

    fun getStockTransfers(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        fromStoreId: String?,
        toStoreId: String?
    ): Flow<List<StockTransfer>>
}