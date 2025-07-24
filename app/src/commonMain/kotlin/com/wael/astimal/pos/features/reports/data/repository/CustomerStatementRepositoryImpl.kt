package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import com.wael.astimal.pos.features.reports.domain.repository.CustomerStatementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime

class CustomerStatementRepositoryImpl(
    private val db: AppDatabase
) : CustomerStatementRepository {
    override fun getTransactionsForPartner(
        partnerId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DetailedTransaction>> {
        val startEpochMilli = startDate.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        val endEpochMilli =
            endDate.atTime(23, 59, 59).toInstant(TimeZone.UTC).toJavaInstant().toEpochMilli()

        return db.partnerTransactionDao()
            .getTransactionsForPartnerInRange(partnerId, startEpochMilli, endEpochMilli)
            .map { list ->
                list.map { entity -> entity.toDomain() }.map { entity ->
                    DetailedTransaction(
                        id = entity.id,
                        date = Instant.fromEpochMilliseconds(entity.createdAt)
                            .toLocalDateTime(TimeZone.UTC).date,
                        transactionType = entity.transactionType,
                        invoiceId = entity.invoiceId.toString(),
                        totalAmount = entity.amount,
                        partnerName = entity.partner.name
                    )
                }.sortedByDescending { it.date }
            }
    }
}