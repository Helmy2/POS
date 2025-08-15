package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface StockTransferReportRepository {
    fun getStockTransfers(
        startDate: LocalDate,
        endDate: LocalDate,
        fromStoreId: String?, // Nullable for "All"
        toStoreId: String?    // Nullable for "All"
    ): Flow<List<StockTransfer>>
}