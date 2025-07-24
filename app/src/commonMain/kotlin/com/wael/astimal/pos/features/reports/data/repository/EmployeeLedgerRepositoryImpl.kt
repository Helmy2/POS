package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import com.wael.astimal.pos.features.reports.domain.repository.EmployeeLedgerRepository
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
import kotlin.math.abs

class EmployeeLedgerRepositoryImpl(
    private val db: AppDatabase,
) : EmployeeLedgerRepository {

    override fun getEmployeeLedger(
        employeeId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<EmployeeLedgerEntry>> {
        val startEpochMilli = startDate.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        val endEpochMilli =
            endDate.atTime(23, 59, 59).toInstant(TimeZone.UTC).toJavaInstant().toEpochMilli()

        return db.employeeFinancesDao()
            .getTransactionsForEmployeeInRange(employeeId, startEpochMilli, endEpochMilli)
            .map { it.map { it.toDomain() } }.map { allTransactions ->

                val transactionsInRange = allTransactions
                    .sortedBy { it.createdAt }

                val ledgerEntries = mutableListOf<EmployeeLedgerEntry>()
                var currentBalance = 0.0

                transactionsInRange.forEach { trx ->
                    currentBalance += trx.amount
                    ledgerEntries.add(
                        EmployeeLedgerEntry(
                            date = Instant.fromEpochMilliseconds(trx.createdAt)
                                .toLocalDateTime(TimeZone.UTC).date,
                            transactionType = trx.type,
                            notes = trx.notes ?: "",
                            debit = if (trx.amount < 0) abs(trx.amount) else 0.0,
                            credit = if (trx.amount > 0) trx.amount else 0.0,
                            balance = currentBalance
                        )
                    )
                }
                ledgerEntries
            }
    }
}