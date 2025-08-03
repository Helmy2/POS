package com.wael.astimal.pos.features.reports.data.repository


import StockTransfer
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.reports.domain.repository.StockTransferReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant

class StockTransferReportRepositoryImpl(
    private val db: AppDatabase
) : StockTransferReportRepository {

    override fun getStockTransfers(
        startDate: LocalDate,
        endDate: LocalDate,
        fromStoreId: String?,
        toStoreId: String?
    ): Flow<List<StockTransfer>> {
        val startEpochMilli = startDate.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        val endEpochMilli =
            endDate.atTime(23, 59, 59).toInstant(TimeZone.UTC).toJavaInstant().toEpochMilli()

        return db.stockTransferDao().getAllStockTransfersWithDetailsFlow()
            .map { allTransfers ->
                allTransfers
                    .filter { it.transfer.createdAt in startEpochMilli..endEpochMilli }
                    .filter { fromStoreId == null || it.transfer.fromStoreId == fromStoreId }
                    .filter { toStoreId == null || it.transfer.toStoreId == toStoreId }
                    .map { it.toDomain() }
                    .sortedByDescending { it.createdAt }
            }
    }
}